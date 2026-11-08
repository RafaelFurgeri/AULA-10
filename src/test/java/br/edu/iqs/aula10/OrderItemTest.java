package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void shouldCalculateSubtotalAndNormalizeFields() {
        OrderItem item = new OrderItem(" SKU-10 ", " Mouse Gamer ", 3, new BigDecimal("99.90"));

        assertEquals("SKU-10", item.getProductId());
        assertEquals("Mouse Gamer", item.getDescription());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("99.90"), item.getUnitPrice());
        assertEquals(new BigDecimal("299.70"), item.subtotal());
    }

    @Test
    void shouldRejectInvalidConstructorArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(null, "Desc", 1, BigDecimal.TEN));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem("   ", "Desc", 1, BigDecimal.TEN));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem("SKU-1", null, 1, BigDecimal.TEN));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem("SKU-1", "  ", 1, BigDecimal.TEN));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem("SKU-1", "Desc", 0, BigDecimal.TEN));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem("SKU-1", "Desc", 1, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem("SKU-1", "Desc", 1, BigDecimal.ZERO));
    }

    @Test
    void shouldSupportEqualityAndHashCode() {
        OrderItem first = new OrderItem("SKU-1", "Teclado", 2, new BigDecimal("120.0"));
        OrderItem second = new OrderItem("SKU-1", "Teclado", 2, new BigDecimal("120.00"));
        OrderItem differentQty = new OrderItem("SKU-1", "Teclado", 3, new BigDecimal("120.00"));
        OrderItem differentSku = new OrderItem("SKU-2", "Teclado", 2, new BigDecimal("120.00"));
        OrderItem differentDesc = new OrderItem("SKU-1", "Monitor", 2, new BigDecimal("120.00"));
        OrderItem differentPrice = new OrderItem("SKU-1", "Teclado", 2, new BigDecimal("150.00"));

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, differentQty);
        assertNotEquals(first, differentSku);
        assertNotEquals(first, differentDesc);
        assertNotEquals(first, differentPrice);
        assertNotEquals(first, "not-an-item");
    }
}
