package br.edu.iqs.aula10;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de resumos de pedidos.
 */
public interface OrderRepository {

    void saveSummary(OrderSummary summary);

    Optional<OrderSummary> findByOrderId(String orderId);

    List<OrderSummary> findAll();

    int count();
}
