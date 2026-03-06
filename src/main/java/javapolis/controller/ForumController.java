package javapolis.controller;

import javapolis.model.ForumMessage;
import javapolis.model.User;
import javapolis.repository.ForumMessageRepository;
import javapolis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forum")
@Transactional
public class ForumController {

    @Autowired
    private ForumMessageRepository forumMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages() {
        List<ForumMessage> messages = forumMessageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50));
        
        // Переворачиваем, чтобы старые были сверху
        Collections.reverse(messages);

        List<Map<String, Object>> result = messages.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("content", msg.getContent());
            map.put("username", msg.getUser().getUsername());
            map.put("createdAt", msg.getCreatedAt());
            map.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=" + msg.getUser().getUsername());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> payload, Authentication authentication) {
        Authentication auth = authentication;
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        ForumMessage message = new ForumMessage(user, content);
        forumMessageRepository.save(message);

        return ResponseEntity.ok().build();
    }
}
