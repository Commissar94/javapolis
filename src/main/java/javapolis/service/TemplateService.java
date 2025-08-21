package javapolis.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TemplateService {
    
    public String processTemplate(String templateName, Map<String, String> variables) {
        try {
            // Читаем HTML файл из ресурсов
            ClassPathResource resource = new ClassPathResource("email-templates/" + templateName + ".html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // Заменяем переменные в шаблоне
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            
            return template;
            
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения шаблона: " + templateName, e);
        }
    }
}
