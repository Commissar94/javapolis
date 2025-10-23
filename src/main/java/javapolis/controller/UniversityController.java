package javapolis.controller;

import javapolis.model.TopicStructure;
import javapolis.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/university")
public class UniversityController {

    @Autowired
    private TopicService topicService;

    @GetMapping
    public String universityHome() {
        return "university/courses";
    }

    @GetMapping("/basic")
    public String basicCourse(Model model) {
        List<TopicStructure> wrapped = wrapIntoBasicCourse(topicService.getTopicStructure());
        model.addAttribute("topicStructure", wrapped);
        return "university/index";
    }

    @GetMapping("/advanced")
    public String advancedCourse() {
        return "university/advanced";
    }

    @GetMapping("/**")
    public String getTopic(HttpServletRequest request, Model model) {
        String requestURI = request.getRequestURI();
        String topicPath = requestURI.substring("/university/".length());
        return getTopicInternal(topicPath, model);
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

    private String getTopicInternal(String topicPath, Model model) {
        try {
            String decodedPath = URLDecoder.decode(topicPath, StandardCharsets.UTF_8);
            String content = topicService.getTopicContent(decodedPath);
            List<TopicStructure> topicStructure = wrapIntoBasicCourse(topicService.getTopicStructure());
            TopicStructure currentTopic = findTopicInStructure(topicStructure, decodedPath);

            model.addAttribute("currentTopic", currentTopic);
            model.addAttribute("topicContent", content);
            model.addAttribute("topicStructure", topicStructure);
            return "university/topic";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Лекция не найдена: " + topicPath);
            List<TopicStructure> topicStructure = wrapIntoBasicCourse(topicService.getTopicStructure());
            model.addAttribute("topicStructure", topicStructure);
            return "university/error";
        }
    }
}
