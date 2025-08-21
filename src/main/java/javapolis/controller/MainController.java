package javapolis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Transactional
public class MainController {

    // Убираем перенаправления - пусть Spring Boot служит статические файлы напрямую
    // index.html будет автоматически сервиться как главная страница
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/dashboard.html";
    }

    @GetMapping("/city")
    public String city() {
        return "redirect:/city.html";
    }
}
