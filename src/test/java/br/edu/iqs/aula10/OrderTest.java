package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void shouldComputeTotalsAndManageLifecycleTransitions() {
        List<OrderItem> items = List.of(
                new OrderItem("SKU-1", "Notebook", 1, new BigDecimal("3500.00")),
                new OrderItem("SKU-2", "Cabo HDMI", 2, new BigDecimal("50.00")));
        Order order = new Order(" ORD-01 ", " CUST-01 ", CustomerTier.SILVER, " sp ", items);

        assertEquals("ORD-01", order.getId());
        assertEquals("CUST-01", order.getCustomerId());
        assertEquals(CustomerTier.SILVER, order.getCustomerTier());
        assertEquals("SP", order.getStateCode());
        assertEquals(2, order.getItems().size());
        assertEquals(3, order.totalUnits());
        assertEquals(new BigDecimal("3600.00"), order.rawSubtotal());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        assertThrows(IllegalStateException.class, order::markShipped);

        order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertThrows(IllegalStateException.class, order::markPaid);

        order.markShipped();
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    void shouldAllowCancellingPendingOrPaidOrder() {
        Order order = new Order(
                "ORD-02",
                "CUST-02",
                CustomerTier.STANDARD,
                "RJ",
                List.of(new OrderItem("SKU-1", "Livro", 1, new BigDecimal("80.00"))));

        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldValidateOrderConstructorParameters() {
        List<OrderItem> validItems = List.of(
                new OrderItem("SKU-1", "Item", 1, new BigDecimal("10.00")));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(null, "C1", CustomerTier.STANDARD, "SP", validItems));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("  ", "C1", CustomerTier.STANDARD, "SP", validItems));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O1", null, CustomerTier.STANDARD, "SP", validItems));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O1", "  ", CustomerTier.STANDARD, "SP", validItems));
        assertThrows(
                NullPointerException.class,
                () -> new Order("O1", "C1", null, "SP", validItems));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O1", "C1", CustomerTier.STANDARD, null, validItems));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O1", "C1", CustomerTier.STANDARD, "  ", validItems));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O1", "C1", CustomerTier.STANDARD, "SP", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("O1", "C1", CustomerTier.STANDARD, "SP", Collections.emptyList()));
    }
}
