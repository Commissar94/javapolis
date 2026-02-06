package javapolis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@RestController
@Transactional
public class MainController {

    // Убираем перенаправления - пусть Spring Boot служит статические файлы напрямую
    // index.html будет автоматически сервиться как главная страница
    
    @GetMapping("/api/dashboard")
    public ResponseEntity<?> dashboard() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to dashboard");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/city")
    public ResponseEntity<?> city() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the city");
        return ResponseEntity.ok(response);
    }
}
