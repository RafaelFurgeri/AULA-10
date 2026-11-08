package br.edu.iqs.aula10;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ponto de entrada da aplicacao com endpoint HTTP de healthcheck para Docker/Kubernetes.
 */
public final class OrderApplication {

    private HttpServer server;

    public int startServer(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/health", this::handleHealth);
        httpServer.start();
        this.server = httpServer;
        return httpServer.getAddress().getPort();
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public OrderSummary runSampleWorkflow() {
        OrderService service = new OrderService(
                new InMemoryOrderRepository(),
                new DiscountPolicy(),
                new TaxCalculator());
        Order sampleOrder = new Order(
                "ORD-DEMO-01",
                "CUST-100",
                CustomerTier.GOLD,
                "SP",
                List.of(new OrderItem("SKU-01", "Licenca CI/CD Pro", 2, new BigDecimal("150.00"))));
        return service.payAndPersist(sampleOrder, "CI5");
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        byte[] payload = "{\"status\":\"UP\",\"service\":\"aula10-ci-cd\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        OrderApplication app = new OrderApplication();
        OrderSummary demo = app.runSampleWorkflow();
        int boundPort = app.startServer(port);
        System.out.println("Aula 10 CI/CD iniciada na porta " + boundPort
                + " | Pedido demo: " + demo.getFinalTotal());
    }
}
