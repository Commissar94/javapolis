package javapolis.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.address=127.0.0.1"})
class FactoryHttpTest {
    @Autowired TestRestTemplate http;

    @Test void servesTheLocalGameAndItsAssets() {
        var page = http.getForEntity("/", String.class);
        assertEquals(HttpStatus.OK, page.getStatusCode());
        assertTrue(page.getBody().contains("factory-map"));
        assertEquals(HttpStatus.OK, http.getForEntity("/factory.js", String.class).getStatusCode());
        assertEquals(HttpStatus.OK, http.getForEntity("/factory.css", String.class).getStatusCode());
    }

    @Test void acceptsJsonCommandsAndReturnsState() {
        var reset = http.postForEntity("/api/factory", Map.of("action", "reset", "scenario", "sandbox"), Map.class);
        assertEquals(HttpStatus.OK, reset.getStatusCode());
        var step = http.postForEntity("/api/factory", Map.of("action", "step", "amount", 1), Map.class);
        assertEquals(HttpStatus.OK, step.getStatusCode());
        assertEquals(1, step.getBody().get("tick"));
    }

    @Test void malformedCommandsAreClientErrors() {
        assertEquals(HttpStatus.BAD_REQUEST,
                http.postForEntity("/api/factory", Map.of("action", "unknown"), Map.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                http.postForEntity("/api/factory", Map.of("action", "step", "amount", -1), Map.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                http.postForEntity("/api/factory", Map.of("action", "build", "type", "NOT_A_MACHINE"), Map.class).getStatusCode());
    }
}
