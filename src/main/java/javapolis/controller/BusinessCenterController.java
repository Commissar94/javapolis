package javapolis.controller;

import javapolis.model.BusinessTask;
import javapolis.model.User;
import javapolis.repository.BusinessTaskRepository;
import javapolis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
@Transactional
public class BusinessCenterController {

    @Autowired
    private BusinessTaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        if (taskRepository.count() == 0) {
            taskRepository.save(new BusinessTask("Первый запуск", "Успешно запустите свою первую программу на Java", 50, "fas fa-rocket"));
            taskRepository.save(new BusinessTask("Исследователь", "Прочитайте 5 лекций", 30, "fas fa-search"));
            taskRepository.save(new BusinessTask("Общительный", "Напишите 3 сообщения на форуме", 20, "fas fa-comments"));
            taskRepository.save(new BusinessTask("Профи", "Решите задачу повышенной сложности", 100, "fas fa-star"));
            taskRepository.save(new BusinessTask("Ранняя пташка", "Зайдите в систему до 8 утра", 15, "fas fa-sun"));
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<BusinessTask>> getTasks() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id, Authentication authentication) {
        Authentication auth = authentication;
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        BusinessTask task = taskRepository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        // В реальности здесь должна быть проверка, выполнил ли пользователь условия
        // Но для этого этапа мы просто начисляем монеты
        user.setCoins(user.getCoins() + task.getReward());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("newCoins", user.getCoins()));
    }
}
