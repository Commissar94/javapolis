package javapolis.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateService templateService;
    
    @Value("${spring.mail.username:test@example.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        try {
            // Проверяем, что email настроен правильно
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com") || fromEmail.equals("test@example.com")) {
                System.out.println("⚠️ Email не настроен. Пропускаем отправку письма для: " + to);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Подтверждение регистрации в Javapolis");
            
            // Создаем HTML содержимое из шаблона
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("verificationUrl", baseUrl + "/verify?token=" + verificationToken);
            
            String htmlContent = templateService.processTemplate("verification-email", variables);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Email отправлен для: " + to);
            
        } catch (MessagingException e) {
            System.err.println("❌ Ошибка отправки email: " + e.getMessage());
            // Не выбрасываем исключение, чтобы не прерывать регистрацию
        }
    }
    
    public void sendWelcomeEmail(String to, String username) {
        try {
            // Проверяем, что email настроен правильно
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com") || fromEmail.equals("test@example.com")) {
                System.out.println("⚠️ Email не настроен. Пропускаем отправку приветственного письма для: " + to);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Добро пожаловать в Javapolis!");
            
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("baseUrl", baseUrl);
            
            String htmlContent = templateService.processTemplate("welcome-email", variables);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Приветственное письмо отправлено для: " + to);
            
        } catch (MessagingException e) {
            System.err.println("❌ Ошибка отправки приветственного письма: " + e.getMessage());
        }
    }
    
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            // Проверяем, что email настроен правильно
            if (fromEmail == null || fromEmail.equals("your-email@gmail.com") || fromEmail.equals("test@example.com")) {
                System.out.println("⚠️ Email не настроен. Пропускаем отправку письма сброса пароля для: " + to);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Сброс пароля в Javapolis");
            
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("resetUrl", baseUrl + "/reset-password?token=" + resetToken);
            
            String htmlContent = templateService.processTemplate("password-reset-email", variables);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Письмо сброса пароля отправлено для: " + to);
            
        } catch (MessagingException e) {
            System.err.println("❌ Ошибка отправки письма сброса пароля: " + e.getMessage());
        }
    }
}
