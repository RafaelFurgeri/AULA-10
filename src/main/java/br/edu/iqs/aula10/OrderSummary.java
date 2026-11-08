package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Resumo financeiro consolidado de um pedido apos aplicacao de descontos e impostos.
 */
public final class OrderSummary {

    private final String orderId;
    private final String customerId;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal tax;
    private final BigDecimal finalTotal;
    private final OrderStatus status;

    public OrderSummary(
            String orderId,
            String customerId,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal finalTotal,
            OrderStatus status) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId nao pode ser vazio");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId nao pode ser vazio");
        }
        Objects.requireNonNull(subtotal, "subtotal nao pode ser nulo");
        Objects.requireNonNull(discount, "discount nao pode ser nulo");
        Objects.requireNonNull(tax, "tax nao pode ser nulo");
        Objects.requireNonNull(finalTotal, "finalTotal nao pode ser nulo");
        Objects.requireNonNull(status, "status nao pode ser nulo");

        this.orderId = orderId.trim();
        this.customerId = customerId.trim();
        this.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        this.discount = discount.setScale(2, RoundingMode.HALF_UP);
        this.tax = tax.setScale(2, RoundingMode.HALF_UP);
        this.finalTotal = finalTotal.setScale(2, RoundingMode.HALF_UP);
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
