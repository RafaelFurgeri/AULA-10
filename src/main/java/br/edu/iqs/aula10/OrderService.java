package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servico de aplicacao responsavel por calcular valores, confirmar pagamentos e persistir pedidos.
 */
public final class OrderService {

    private final OrderRepository repository;
    private final DiscountPolicy discountPolicy;
    private final TaxCalculator taxCalculator;

    public OrderService(
            OrderRepository repository,
            DiscountPolicy discountPolicy,
            TaxCalculator taxCalculator) {
        this.repository = Objects.requireNonNull(repository, "repository nao pode ser nulo");
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy nao pode ser nulo");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "taxCalculator nao pode ser nulo");
    }

    public OrderSummary processOrder(Order order, String couponCode) {
        Objects.requireNonNull(order, "order nao pode ser nulo");
        BigDecimal subtotal = order.rawSubtotal();
        BigDecimal discount = discountPolicy.calculateDiscount(
                subtotal,
                order.getCustomerTier(),
                order.totalUnits(),
                couponCode);
        BigDecimal taxableBase = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = taxCalculator.calculateTax(taxableBase, order.getStateCode());
        BigDecimal finalTotal = taxableBase.add(tax).setScale(2, RoundingMode.HALF_UP);

        OrderSummary summary = new OrderSummary(
                order.getId(),
                order.getCustomerId(),
                subtotal,
                discount,
                tax,
                finalTotal,
                order.getStatus());
        repository.saveSummary(summary);
        return summary;
    }

    public OrderSummary payAndPersist(Order order, String couponCode) {
        Objects.requireNonNull(order, "order nao pode ser nulo");
        order.markPaid();
        return processOrder(order, couponCode);
    }

    public OrderSummary cancelAndPersist(Order order) {
        Objects.requireNonNull(order, "order nao pode ser nulo");
        order.cancel();
        OrderSummary cancelledSummary = new OrderSummary(
                order.getId(),
                order.getCustomerId(),
                order.rawSubtotal(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                order.getStatus());
        repository.saveSummary(cancelledSummary);
        return cancelledSummary;
    }

    public Optional<OrderSummary> findOrderSummary(String orderId) {
        return repository.findByOrderId(orderId);
    }

    public List<OrderSummary> listAllSummaries() {
        return repository.findAll();
    }

    public int countOrders() {
        return repository.count();
    }
}
