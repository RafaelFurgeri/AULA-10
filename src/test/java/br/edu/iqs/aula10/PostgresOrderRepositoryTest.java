package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PostgresOrderRepositoryTest {

    @Test
    void shouldValidateConstructorArguments() {
        PostgresOrderRepository repo = new PostgresOrderRepository(
                " jdbc:postgresql://localhost:5432/test ",
                "user",
                "pass");
        assertEquals("jdbc:postgresql://localhost:5432/test", repo.getJdbcUrl());

        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgresOrderRepository(null, "u", "p"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new PostgresOrderRepository("  ", "u", "p"));
        assertThrows(
                NullPointerException.class,
                () -> new PostgresOrderRepository("jdbc:postgresql://localhost/db", null, "p"));
        assertThrows(
                NullPointerException.class,
                () -> new PostgresOrderRepository("jdbc:postgresql://localhost/db", "u", null));
        assertThrows(
                NullPointerException.class,
                () -> new PostgresOrderRepository("jdbc:postgresql://localhost/db", null));
    }

    @Test
    void shouldExecuteCrudOperationsAgainstStubbedJdbcConnection() {
        Map<String, OrderSummary> fakeTable = new LinkedHashMap<>();
        PostgresOrderRepository repo = new PostgresOrderRepository(
                "jdbc:postgresql://stub/db",
                () -> createFakeConnection(fakeTable));

        repo.initSchema();
        assertEquals(0, repo.count());
        assertFalse(repo.findByOrderId(null).isPresent());
        assertFalse(repo.findByOrderId("  ").isPresent());
        assertFalse(repo.findByOrderId("MISSING").isPresent());

        OrderSummary summary = new OrderSummary(
                "ORD-200",
                "CUST-200",
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                new BigDecimal("54.00"),
                new BigDecimal("504.00"),
                OrderStatus.PAID);
        repo.saveSummary(summary);

        assertEquals(1, repo.count());
        Optional<OrderSummary> loaded = repo.findByOrderId("ORD-200");
        assertTrue(loaded.isPresent());
        assertEquals(new BigDecimal("504.00"), loaded.get().getFinalTotal());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void shouldTranslateSqlExceptionsIntoIllegalStateException() {
        PostgresOrderRepository brokenRepo = new PostgresOrderRepository(
                "jdbc:postgresql://broken/db",
                () -> {
                    throw new SQLException("Simulated database outage");
                });
        OrderSummary sample = new OrderSummary(
                "ORD-ERR",
                "CUST-ERR",
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                new BigDecimal("11.00"),
                OrderStatus.PENDING);

        assertThrows(IllegalStateException.class, brokenRepo::initSchema);
        assertThrows(IllegalStateException.class, () -> brokenRepo.saveSummary(sample));
        assertThrows(IllegalStateException.class, () -> brokenRepo.findByOrderId("ORD-ERR"));
        assertThrows(IllegalStateException.class, brokenRepo::findAll);
        assertThrows(IllegalStateException.class, brokenRepo::count);
    }

    private Connection createFakeConnection(Map<String, OrderSummary> table) {
        ClassLoader cl = PostgresOrderRepositoryTest.class.getClassLoader();
        Statement stmt = (Statement) Proxy.newProxyInstance(
                cl,
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> Boolean.TRUE);

        return (Connection) Proxy.newProxyInstance(
                cl,
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("createStatement".equals(name)) {
                        return stmt;
                    }
                    if ("prepareStatement".equals(name)) {
                        String sql = (String) args[0];
                        return createFakePreparedStatement(cl, sql, table);
                    }
                    return null;
                });
    }

    private PreparedStatement createFakePreparedStatement(
            ClassLoader cl,
            String sql,
            Map<String, OrderSummary> table) {
        Object[] params = new Object[8];
        return (PreparedStatement) Proxy.newProxyInstance(
                cl,
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    if ("setString".equals(m) || "setBigDecimal".equals(m)) {
                        params[(Integer) args[0]] = args[1];
                        return null;
                    }
                    if ("executeUpdate".equals(m)) {
                        OrderSummary saved = new OrderSummary(
                                (String) params[1],
                                (String) params[2],
                                (BigDecimal) params[3],
                                (BigDecimal) params[4],
                                (BigDecimal) params[5],
                                (BigDecimal) params[6],
                                OrderStatus.valueOf((String) params[7]));
                        table.put(saved.getOrderId(), saved);
                        return 1;
                    }
                    if ("executeQuery".equals(m)) {
                        if (sql.startsWith("SELECT COUNT")) {
                            return createCountResultSet(cl, table.size());
                        }
                        if (sql.contains("WHERE order_id = ?")) {
                            OrderSummary found = table.get((String) params[1]);
                            List<OrderSummary> rows = found == null ? List.of() : List.of(found);
                            return createRowsResultSet(cl, rows);
                        }
                        return createRowsResultSet(cl, new ArrayList<>(table.values()));
                    }
                    return null;
                });
    }

    private ResultSet createCountResultSet(ClassLoader cl, int size) {
        int[] cursor = {-1};
        return (ResultSet) Proxy.newProxyInstance(
                cl,
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    if ("next".equals(method.getName())) {
                        cursor[0]++;
                        return cursor[0] == 0;
                    }
                    if ("getInt".equals(method.getName())) {
                        return size;
                    }
                    return null;
                });
    }

    private ResultSet createRowsResultSet(ClassLoader cl, List<OrderSummary> rows) {
        int[] cursor = {-1};
        return (ResultSet) Proxy.newProxyInstance(
                cl,
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    if ("next".equals(m)) {
                        cursor[0]++;
                        return cursor[0] < rows.size();
                    }
                    if (!"getString".equals(m) && !"getBigDecimal".equals(m)) {
                        return null;
                    }
                    OrderSummary current = rows.get(cursor[0]);
                    if ("getString".equals(m)) {
                        String col = (String) args[0];
                        if ("order_id".equals(col)) {
                            return current.getOrderId();
                        }
                        if ("customer_id".equals(col)) {
                            return current.getCustomerId();
                        }
                        return current.getStatus().name();
                    }
                    if ("getBigDecimal".equals(m)) {
                        String col = (String) args[0];
                        if ("subtotal".equals(col)) {
                            return current.getSubtotal();
                        }
                        if ("discount".equals(col)) {
                            return current.getDiscount();
                        }
                        if ("tax".equals(col)) {
                            return current.getTax();
                        }
                        return current.getFinalTotal();
                    }
                    return null;
                });
    }
}
