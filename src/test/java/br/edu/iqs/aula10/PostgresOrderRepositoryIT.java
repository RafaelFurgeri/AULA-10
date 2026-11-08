package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Teste de Integracao real utilizando TestContainers + PostgreSQL 16 (Desafio Integrado).
 */
@Testcontainers
class PostgresOrderRepositoryIT {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("aula10_iqs")
            .withUsername("iqs_user")
            .withPassword("iqs_pass");

    private static PostgresOrderRepository repository;
    private static OrderService orderService;

    @BeforeAll
    static void setUpDatabase() {
        assertTrue(POSTGRES.isRunning());
        repository = new PostgresOrderRepository(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
        repository.initSchema();
        orderService = new OrderService(repository, new DiscountPolicy(), new TaxCalculator());
    }

    @Test
    void shouldPersistUpdateAndQueryOrdersInRealPostgreSqlContainer() {
        Order firstOrder = new Order(
                "IT-ORD-01",
                "IT-CUST-01",
                CustomerTier.PLATINUM,
                "PR",
                List.of(
                        new OrderItem("SKU-DB-1", "Servidor Cloud", 2, new BigDecimal("1000.00")),
                        new OrderItem("SKU-DB-2", "Suporte DevSecOps", 8, new BigDecimal("100.00"))));

        // Subtotal = 2800.00; PLATINUM (15%) + Volume 10 un (5%) + IQS10 (10%) = 30% -> 840.00
        // Base = 1960.00; Imposto PR (9%) = 176.40; Final = 2136.40
        OrderSummary paidSummary = orderService.payAndPersist(firstOrder, "IQS10");
        assertEquals(new BigDecimal("2136.40"), paidSummary.getFinalTotal());

        Optional<OrderSummary> fetched = orderService.findOrderSummary("IT-ORD-01");
        assertTrue(fetched.isPresent());
        assertEquals("IT-CUST-01", fetched.get().getCustomerId());
        assertEquals(OrderStatus.PAID, fetched.get().getStatus());
        assertEquals(new BigDecimal("2136.40"), fetched.get().getFinalTotal());

        // Testa UPSERT (ON CONFLICT DO UPDATE) ao cancelar o pedido no PostgreSQL
        OrderSummary cancelled = orderService.cancelAndPersist(firstOrder);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        Optional<OrderSummary> updated = orderService.findOrderSummary("IT-ORD-01");
        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.CANCELLED, updated.get().getStatus());
        assertEquals(new BigDecimal("0.00"), updated.get().getFinalTotal());
        assertTrue(orderService.countOrders() >= 1);
        assertTrue(orderService.listAllSummaries().size() >= 1);
    }
}
