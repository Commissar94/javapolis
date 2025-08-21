package javapolis.controller;

import javapolis.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
@CrossOrigin(origins = "*")
@Transactional
public class RegistrationController {
    
    @Autowired
    private RegistrationService registrationService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationService.RegistrationRequest request) {
        try {
            RegistrationService.RegistrationResult result = registrationService.registerUser(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка регистрации: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            RegistrationService.VerificationResult result = registrationService.verifyEmail(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка верификации: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        // Этот метод можно использовать для проверки доступности username в реальном времени
        Map<String, Object> response = new HashMap<>();
        response.put("available", true); // Пока заглушка
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        // Этот метод можно использовать для проверки доступности email в реальном времени
        Map<String, Object> response = new HashMap<>();
        response.put("available", true); // Пока заглушка
        return ResponseEntity.ok(response);
    }
}
