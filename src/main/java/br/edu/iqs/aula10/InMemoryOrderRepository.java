package br.edu.iqs.aula10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementacao thread-safe em memoria do repositorio de pedidos para testes rapidos e execucao local.
 */
public final class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, OrderSummary> storage = new ConcurrentHashMap<>();

    @Override
    public void saveSummary(OrderSummary summary) {
        Objects.requireNonNull(summary, "summary nao pode ser nulo");
        storage.put(summary.getOrderId(), summary);
    }

    @Override
    public Optional<OrderSummary> findByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(orderId.trim()));
    }

    @Override
    public List<OrderSummary> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public int count() {
        return storage.size();
    }
}
