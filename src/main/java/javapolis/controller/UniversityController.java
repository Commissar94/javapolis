package javapolis.controller;

import javapolis.model.TopicStructure;
import javapolis.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/university")
public class UniversityController {

    @Autowired
    private TopicService topicService;

    @GetMapping("/structure")
    public ResponseEntity<?> getStructure() {
        List<TopicStructure> wrapped = wrapIntoBasicCourse(topicService.getTopicStructure());
        return ResponseEntity.ok(wrapped);
    }

    @GetMapping("/topic/**")
    public ResponseEntity<?> getTopic(HttpServletRequest request, @RequestParam(defaultValue = "0") int page) {
        String requestURI = request.getRequestURI();
        String topicPath = requestURI.substring("/api/university/topic/".length());
        try {
            String decodedPath = URLDecoder.decode(topicPath, StandardCharsets.UTF_8);
            Map<String, Object> topicData = topicService.getTopicContent(decodedPath, page);
            List<TopicStructure> topicStructure = wrapIntoBasicCourse(topicService.getTopicStructure());
            TopicStructure currentTopic = findTopicInStructure(topicStructure, decodedPath);

            Map<String, Object> response = new HashMap<>();
            response.put("currentTopic", currentTopic);
            response.put("content", topicData.get("content"));
            response.put("currentPage", topicData.get("currentPage"));
            response.put("totalPages", topicData.get("totalPages"));
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(404).body("Лекция не найдена: " + topicPath);
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
