package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private InMemoryOrderRepository repository;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        service = new OrderService(repository, new DiscountPolicy(), new TaxCalculator());
    }

    @Test
    void shouldProcessPayAndPersistOrdersEndToEnd() {
        Order order = new Order(
                "ORD-100",
                "CUST-50",
                CustomerTier.GOLD,
                "SP",
                List.of(new OrderItem("SKU-9", "Monitor 4K", 2, new BigDecimal("1000.00"))));

        // Subtotal = 2000.00; Desconto GOLD (10%) + IQS10 (10%) = 400.00;
        // Base = 1600.00; Imposto SP (12%) = 192.00; Total = 1792.00
        OrderSummary summary = service.payAndPersist(order, "IQS10");

        assertEquals("ORD-100", summary.getOrderId());
        assertEquals("CUST-50", summary.getCustomerId());
        assertEquals(new BigDecimal("2000.00"), summary.getSubtotal());
        assertEquals(new BigDecimal("400.00"), summary.getDiscount());
        assertEquals(new BigDecimal("192.00"), summary.getTax());
        assertEquals(new BigDecimal("1792.00"), summary.getFinalTotal());
        assertEquals(OrderStatus.PAID, summary.getStatus());

        Optional<OrderSummary> stored = service.findOrderSummary("ORD-100");
        assertTrue(stored.isPresent());
        assertEquals(new BigDecimal("1792.00"), stored.get().getFinalTotal());
        assertEquals(1, service.countOrders());
        assertEquals(1, service.listAllSummaries().size());
        assertFalse(service.findOrderSummary("   ").isPresent());
        assertFalse(service.findOrderSummary(null).isPresent());
    }

    @Test
    void shouldCancelAndZeroFinancialTotals() {
        Order order = new Order(
                "ORD-101",
                "CUST-51",
                CustomerTier.STANDARD,
                "MG",
                List.of(new OrderItem("SKU-1", "Webcam", 1, new BigDecimal("250.00"))));

        OrderSummary cancelled = service.cancelAndPersist(order);

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(new BigDecimal("250.00"), cancelled.getSubtotal());
        assertEquals(new BigDecimal("0.00"), cancelled.getDiscount());
        assertEquals(new BigDecimal("0.00"), cancelled.getTax());
        assertEquals(new BigDecimal("0.00"), cancelled.getFinalTotal());
    }

    @Test
    void shouldValidateSummaryAndServiceNullContracts() {
        assertThrows(
                NullPointerException.class,
                () -> new OrderService(null, new DiscountPolicy(), new TaxCalculator()));
        assertThrows(
                NullPointerException.class,
                () -> new OrderService(repository, null, new TaxCalculator()));
        assertThrows(
                NullPointerException.class,
                () -> new OrderService(repository, new DiscountPolicy(), null));
        assertThrows(
                NullPointerException.class,
                () -> service.processOrder(null, null));
        assertThrows(
                NullPointerException.class,
                () -> service.payAndPersist(null, null));
        assertThrows(
                NullPointerException.class,
                () -> service.cancelAndPersist(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderSummary(
                        " ",
                        "C1",
                        BigDecimal.TEN,
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        OrderStatus.PAID));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderSummary(
                        "O1",
                        " ",
                        BigDecimal.TEN,
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        OrderStatus.PAID));
    }
}
