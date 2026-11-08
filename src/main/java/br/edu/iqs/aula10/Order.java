package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidade de dominio que representa um pedido de compra e suas transicoes de estado.
 */
public final class Order {

    private final String id;
    private final String customerId;
    private final CustomerTier customerTier;
    private final String stateCode;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(
            String id,
            String customerId,
            CustomerTier customerTier,
            String stateCode,
            List<OrderItem> items) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id do pedido nao pode ser vazio");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId nao pode ser vazio");
        }
        Objects.requireNonNull(customerTier, "customerTier nao pode ser nulo");
        if (stateCode == null || stateCode.isBlank()) {
            throw new IllegalArgumentException("stateCode nao pode ser vazio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("pedido deve possuir ao menos um item");
        }
        this.id = id.trim();
        this.customerId = customerId.trim();
        this.customerTier = customerTier;
        this.stateCode = stateCode.trim().toUpperCase();
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CustomerTier getCustomerTier() {
        return customerTier;
    }

    public String getStateCode() {
        return stateCode;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int totalUnits() {
        int units = 0;
        for (OrderItem item : items) {
            units += item.getQuantity();
        }
        return units;
    }

    public BigDecimal rawSubtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(item.subtotal());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Somente pedidos PENDING podem ser pagos");
        }
        status = OrderStatus.PAID;
    }

    public void markShipped() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Somente pedidos PAID podem ser enviados");
        }
        status = OrderStatus.SHIPPED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Pedido ja enviado nao pode ser cancelado");
        }
        status = OrderStatus.CANCELLED;
    }
}
