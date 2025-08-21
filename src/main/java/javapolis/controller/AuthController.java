package javapolis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Transactional
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            System.out.println("Попытка входа для пользователя: " + loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );
            
            // Сохраняем аутентификацию в сессии
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            // Также сохраняем в SecurityContextHolder для текущего запроса
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            System.out.println("Аутентификация успешна для: " + loginRequest.getUsername());
            System.out.println("Роли: " + authentication.getAuthorities());
            System.out.println("ID сессии: " + session.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Успешная аутентификация");
            response.put("username", loginRequest.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("Ошибка аутентификации: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Неверное имя пользователя или пароль");
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Успешный выход из системы");
        
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(HttpServletRequest request) {
        System.out.println("🔍 Проверка статуса аутентификации...");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Текущий контекст: " + (auth != null ? auth.getName() + " (аутентифицирован: " + auth.isAuthenticated() + ")" : "null"));
        
        // Если нет аутентификации в текущем контексте, проверяем сессию
        if (auth == null || !auth.isAuthenticated()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                System.out.println("Сессия найдена: " + session.getId());
                Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (securityContext != null) {
                    System.out.println("✅ Найден контекст безопасности в сессии");
                    if (securityContext instanceof org.springframework.security.core.context.SecurityContext) {
                        org.springframework.security.core.context.SecurityContext context = 
                            (org.springframework.security.core.context.SecurityContext) securityContext;
                        auth = context.getAuthentication();
                        System.out.println("Аутентификация из сессии: " + (auth != null ? auth.getName() + " (аутентифицирован: " + auth.isAuthenticated() + ")" : "null"));
                    }
                } else {
                    System.out.println("❌ Контекст безопасности не найден в сессии");
                }
            } else {
                System.out.println("❌ Сессия не найдена");
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        
        if (auth != null && auth.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities().stream()
                .map(Object::toString)
                .toArray());
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
