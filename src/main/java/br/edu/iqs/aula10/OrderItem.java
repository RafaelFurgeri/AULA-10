package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Representa um item individual dentro de um pedido.
 */
public final class OrderItem {

    private final String productId;
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String productId, String description, int quantity, BigDecimal unitPrice) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId nao pode ser vazio");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description nao pode ser vazia");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity deve ser maior que zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice deve ser maior que zero");
        }
        this.productId = productId.trim();
        this.description = description.trim();
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public String getProductId() {
        return productId;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal subtotal() {
        return unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrderItem that)) {
            return false;
        }
        return quantity == that.quantity
                && productId.equals(that.productId)
                && description.equals(that.description)
                && unitPrice.compareTo(that.unitPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, description, quantity, unitPrice.stripTrailingZeros());
    }
}
