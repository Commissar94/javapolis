package javapolis.controller;

import javapolis.model.TopicStructure;
import javapolis.model.User;
import javapolis.model.UserProgress;
import javapolis.repository.UserRepository;
import javapolis.service.ProgressService;
import javapolis.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/university")
public class UniversityController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/structure")
    public ResponseEntity<?> getStructure() {
        List<TopicStructure> structure = topicService.getTopicStructure();
        
        // Применяем прогресс текущего пользователя
        User user = getCurrentUser();
        if (user != null) {
            List<UserProgress> userProgress = progressService.getUserProgress(user);
            Map<String, UserProgress> progressMap = userProgress.stream()
                    .collect(Collectors.toMap(UserProgress::getTopicPath, p -> p));
            applyProgressToStructure(structure, progressMap);
        }

        List<TopicStructure> wrapped = wrapIntoBasicCourse(structure);
        return ResponseEntity.ok(wrapped);
    }

    @GetMapping("/topic/**")
    public ResponseEntity<?> getTopic(HttpServletRequest request, @RequestParam(defaultValue = "0") int page) {
        String requestURI = request.getRequestURI();
        String topicPath = requestURI.substring("/api/university/topic/".length());
        try {
            String decodedPath = URLDecoder.decode(topicPath, StandardCharsets.UTF_8);
            Map<String, Object> topicData = topicService.getTopicContent(decodedPath, page);
            
            List<TopicStructure> fullStructure = topicService.getTopicStructure();
            User user = getCurrentUser();
            
            // Информация о пройденных страницах для текущей темы
            List<Integer> completedPages = new java.util.ArrayList<>();
            if (user != null) {
                progressService.getTopicProgress(user, decodedPath)
                        .ifPresent(p -> {
                            String[] pages = p.getCompletedPages().split(",");
                            for (String s : pages) {
                                if (!s.isBlank()) completedPages.add(Integer.parseInt(s.trim()));
                            }
                        });
                
                // Также обновим структуру для навигации
                List<UserProgress> userProgress = progressService.getUserProgress(user);
                Map<String, UserProgress> progressMap = userProgress.stream()
                        .collect(Collectors.toMap(UserProgress::getTopicPath, p -> p));
                applyProgressToStructure(fullStructure, progressMap);
            }

            List<TopicStructure> wrappedStructure = wrapIntoBasicCourse(fullStructure);
            TopicStructure currentTopic = findTopicInStructure(wrappedStructure, decodedPath);

            Map<String, Object> response = new HashMap<>();
            response.put("currentTopic", currentTopic);
            response.put("content", topicData.get("content"));
            response.put("currentPage", topicData.get("currentPage"));
            response.put("totalPages", topicData.get("totalPages"));
            response.put("completedPages", completedPages);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(404).body("Лекция не найдена: " + topicPath);
        }
    }

    @PostMapping("/progress")
    public ResponseEntity<?> updateProgress(@RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String topicPath = (String) payload.get("topicPath");
        int page = (int) payload.get("page");
        int totalPages = (int) payload.get("totalPages");

        progressService.markPageAsCompleted(user, topicPath, page, totalPages);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private void applyProgressToStructure(List<TopicStructure> structure, Map<String, UserProgress> progressMap) {
        for (TopicStructure item : structure) {
            if (item.isFile()) {
                String path = item.getPath().replace(".md", "");
                if (progressMap.containsKey(path)) {
                    item.setCompleted(progressMap.get(path).isCompleted());
                }
            } else if (item.isFolder()) {
                applyProgressToStructure(item.getChildren(), progressMap);
                // Папка считается завершенной, если все ее дети завершены
                boolean allCompleted = !item.getChildren().isEmpty() && 
                        item.getChildren().stream().allMatch(TopicStructure::isCompleted);
                item.setCompleted(allCompleted);
            }
        }
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getImage(@RequestParam String path) {
        try {
            // Декодируем путь, так как он может содержать кириллицу или пробелы
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            Resource resource = topicService.getImageResource(decodedPath);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "image/png";
            String lowerPath = decodedPath.toLowerCase();
            if (lowerPath.endsWith(".webp")) contentType = "image/webp";
            else if (lowerPath.endsWith(".gif")) contentType = "image/gif";
            else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (lowerPath.endsWith(".svg")) contentType = "image/svg+xml";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private TopicStructure findTopicInStructure(List<TopicStructure> structure, String topicPath) {
        for (TopicStructure item : structure) {
            if (item.isFile()) {
                String itemPath = item.getPath().replace(".md", "");
                if (itemPath.equals(topicPath)) {
                    return item;
                }
            }
            if (item.isFolder()) {
                TopicStructure found = findTopicInStructure(item.getChildren(), topicPath);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private List<TopicStructure> wrapIntoBasicCourse(List<TopicStructure> original) {
        TopicStructure root = new TopicStructure("Базовый курс", "folder", "basic");
        root.setExpanded(true);
        for (TopicStructure item : original) {
            root.addChild(item);
        }
        return java.util.Arrays.asList(root);
    }
}
