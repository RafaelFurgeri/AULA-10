package br.edu.iqs.aula10;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repositorio JDBC PostgreSQL testado unitariamente e em integracao real via TestContainers.
 */
public final class PostgresOrderRepository implements OrderRepository {

    /**
     * Provedor funcional de conexoes JDBC.
     */
    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    private final String jdbcUrl;
    private final ConnectionSupplier connectionSupplier;

    public PostgresOrderRepository(String jdbcUrl, String username, String password) {
        this(
                validateJdbcUrl(jdbcUrl),
                createDriverSupplier(
                        validateJdbcUrl(jdbcUrl),
                        Objects.requireNonNull(username, "username nao pode ser nulo"),
                        Objects.requireNonNull(password, "password nao pode ser nulo")));
    }

    PostgresOrderRepository(String jdbcUrl, ConnectionSupplier connectionSupplier) {
        this.jdbcUrl = validateJdbcUrl(jdbcUrl);
        this.connectionSupplier = Objects.requireNonNull(
                connectionSupplier,
                "connectionSupplier nao pode ser nulo");
    }

    private static String validateJdbcUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("jdbcUrl nao pode ser vazio");
        }
        return url.trim();
    }

    private static ConnectionSupplier createDriverSupplier(
            String url,
            String username,
            String password) {
        return () -> DriverManager.getConnection(url, username, password);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void initSchema() {
        String ddl = "CREATE TABLE IF NOT EXISTS order_summaries ("
                + "order_id VARCHAR(64) PRIMARY KEY, "
                + "customer_id VARCHAR(64) NOT NULL, "
                + "subtotal NUMERIC(12, 2) NOT NULL, "
                + "discount NUMERIC(12, 2) NOT NULL, "
                + "tax NUMERIC(12, 2) NOT NULL, "
                + "final_total NUMERIC(12, 2) NOT NULL, "
                + "status VARCHAR(32) NOT NULL"
                + ")";
        try (Connection conn = connectionSupplier.get(); Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha ao inicializar schema no PostgreSQL", ex);
        }
    }

    @Override
    public void saveSummary(OrderSummary summary) {
        Objects.requireNonNull(summary, "summary nao pode ser nulo");
        String upsert = "INSERT INTO order_summaries "
                + "(order_id, customer_id, subtotal, discount, tax, final_total, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (order_id) DO UPDATE SET "
                + "customer_id = EXCLUDED.customer_id, "
                + "subtotal = EXCLUDED.subtotal, "
                + "discount = EXCLUDED.discount, "
                + "tax = EXCLUDED.tax, "
                + "final_total = EXCLUDED.final_total, "
                + "status = EXCLUDED.status";
        try (Connection conn = connectionSupplier.get();
                PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setString(1, summary.getOrderId());
            ps.setString(2, summary.getCustomerId());
            ps.setBigDecimal(3, summary.getSubtotal());
            ps.setBigDecimal(4, summary.getDiscount());
            ps.setBigDecimal(5, summary.getTax());
            ps.setBigDecimal(6, summary.getFinalTotal());
            ps.setString(7, summary.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha ao salvar resumo do pedido", ex);
        }
    }

    @Override
    public Optional<OrderSummary> findByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT order_id, customer_id, subtotal, discount, tax, final_total, status "
                + "FROM order_summaries WHERE order_id = ?";
        try (Connection conn = connectionSupplier.get();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha ao buscar pedido por id", ex);
        }
    }

    @Override
    public List<OrderSummary> findAll() {
        String sql = "SELECT order_id, customer_id, subtotal, discount, tax, final_total, status "
                + "FROM order_summaries ORDER BY order_id";
        List<OrderSummary> results = new ArrayList<>();
        try (Connection conn = connectionSupplier.get();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha ao listar pedidos", ex);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM order_summaries";
        try (Connection conn = connectionSupplier.get();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha ao contar pedidos", ex);
        }
    }

    private OrderSummary mapRow(ResultSet rs) throws SQLException {
        return new OrderSummary(
                rs.getString("order_id"),
                rs.getString("customer_id"),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("discount"),
                rs.getBigDecimal("tax"),
                rs.getBigDecimal("final_total"),
                OrderStatus.valueOf(rs.getString("status")));
    }
}
