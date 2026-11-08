package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class OrderApplicationTest {

    @Test
    void shouldRunSampleWorkflowAndRespondHealthEndpoint() throws Exception {
        OrderApplication app = new OrderApplication();
        OrderSummary demo = app.runSampleWorkflow();

        // Subtotal = 300.00; GOLD (10%) + CI5 (5%) = 45.00; Base = 255.00; SP (12%) = 30.60; Total = 285.60
        assertEquals(new BigDecimal("285.60"), demo.getFinalTotal());
        assertEquals(OrderStatus.PAID, demo.getStatus());

        int port = app.startServer(0);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/health"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("\"status\":\"UP\""));
        } finally {
            app.stopServer();
            app.stopServer();
        }
    }
}
