package javapolis.service;

import javapolis.model.User;
import javapolis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RegistrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    public RegistrationResult registerUser(RegistrationRequest request) {
        // Проверяем, что username и email не заняты
        if (userRepository.existsByUsername(request.getUsername())) {
            return RegistrationResult.error("Имя пользователя уже занято");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            return RegistrationResult.error("Email уже зарегистрирован");
        }
        
        // Создаем нового пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(false);
        
        // Генерируем токен верификации
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Токен действителен 24 часа
        
        // Сохраняем пользователя
        user = userRepository.save(user);
        
        // Отправляем email для верификации
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);
            return RegistrationResult.success("Регистрация успешна! Проверьте email для подтверждения аккаунта.");
        } catch (Exception e) {
            // Если не удалось отправить email, не удаляем пользователя
            System.err.println("Ошибка отправки email: " + e.getMessage());
            return RegistrationResult.success("Регистрация успешна! Email для подтверждения будет отправлен позже.");
        }
    }
    
    public VerificationResult verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElse(null);
        
        if (user == null) {
            return VerificationResult.error("Недействительный токен верификации");
        }
        
        if (user.isEmailVerified()) {
            return VerificationResult.error("Email уже подтвержден");
        }
        
        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return VerificationResult.error("Токен верификации истек");
        }
        
        // Подтверждаем email
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        
        // Отправляем приветственное письмо
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем процесс
            System.err.println("Ошибка отправки приветственного email: " + e.getMessage());
        }
        
        return VerificationResult.success("Email успешно подтвержден! Теперь вы можете войти в систему.");
    }
    
    public static class RegistrationRequest {
        private String username;
        private String password;
        private String email;
        private String firstName;
        private String lastName;
        
        // Геттеры и сеттеры
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
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
    
    public static class RegistrationResult {
        private final boolean success;
        private final String message;
        
        private RegistrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static RegistrationResult success(String message) {
            return new RegistrationResult(true, message);
        }
        
        public static RegistrationResult error(String message) {
            return new RegistrationResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public static class VerificationResult {
        private final boolean success;
        private final String message;
        
        private VerificationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static VerificationResult success(String message) {
            return new VerificationResult(true, message);
        }
        
        public static VerificationResult error(String message) {
            return new VerificationResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
