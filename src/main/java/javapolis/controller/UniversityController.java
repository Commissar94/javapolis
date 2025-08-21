package javapolis.controller;

import javapolis.model.TopicStructure;
import javapolis.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/university")
public class UniversityController {

    @Autowired
    private TopicService topicService;

    @GetMapping
    public String universityHome(Model model) {
        List<TopicStructure> topicStructure = topicService.getTopicStructure();
        model.addAttribute("topicStructure", topicStructure);
        return "university/index";
    }

    @GetMapping("/test")
    public String testEndpoint(Model model) {
        System.out.println("=== TEST ENDPOINT CALLED ===");
        return "university/index";
    }

    @GetMapping("/{topicPath:.+}")
    public String getTopic(@PathVariable String topicPath, Model model) {
        System.out.println("=== GET TOPIC CALLED ===");
        System.out.println("Topic path: " + topicPath);
        
        try {
            // Декодируем URL для правильной обработки кириллицы и пробелов
            String decodedPath = URLDecoder.decode(topicPath, StandardCharsets.UTF_8);
            System.out.println("Decoded path: " + decodedPath);
            
            String content = topicService.getTopicContent(decodedPath);
            List<TopicStructure> topicStructure = topicService.getTopicStructure();
            
            // Находим текущий топик в структуре для подсветки в меню
            TopicStructure currentTopic = findTopicInStructure(topicStructure, decodedPath);
            System.out.println("Current topic found: " + (currentTopic != null ? currentTopic.getName() : "null"));
            
            model.addAttribute("currentTopic", currentTopic);
            model.addAttribute("topicContent", content);
            model.addAttribute("topicStructure", topicStructure);
            return "university/topic";
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            model.addAttribute("errorMessage", "Лекция не найдена: " + topicPath);
            List<TopicStructure> topicStructure = topicService.getTopicStructure();
            model.addAttribute("topicStructure", topicStructure);
            return "university/error";
        }
    }

    private TopicStructure findTopicInStructure(List<TopicStructure> structure, String topicPath) {
        System.out.println("=== SEARCHING FOR TOPIC ===");
        System.out.println("Searching for topic: " + topicPath);
        System.out.println("Structure size: " + structure.size());
        
        for (TopicStructure item : structure) {
            System.out.println("Checking item: " + item.getName() + " (type: " + item.getType() + ", path: " + item.getPath() + ")");
            
            if (item.isFile()) {
                // Сравниваем пути - убираем .md из обоих для сравнения
                String itemPath = item.getPath().replace(".md", "");
                String cleanTopicPath = topicPath;
                
                System.out.println("Comparing file: '" + itemPath + "' with '" + cleanTopicPath + "'");
                
                if (itemPath.equals(cleanTopicPath)) {
                    System.out.println("✅ FOUND FILE: " + item.getName());
                    return item;
                }
            }
            if (item.isFolder()) {
                System.out.println("Searching in folder: " + item.getName() + " (children: " + item.getChildren().size() + ")");
                TopicStructure found = findTopicInStructure(item.getChildren(), topicPath);
                if (found != null) {
                    return found;
                }
            }
        }
        System.out.println("❌ Topic not found: " + topicPath);
        return null;
    }
}
