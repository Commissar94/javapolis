package javapolis.controller;

import javapolis.model.User;
import javapolis.model.dto.CommentDto;
import javapolis.repository.UserRepository;
import javapolis.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(
            @RequestParam String topicPath,
            @RequestParam int page) {
        return ResponseEntity.ok(commentService.getComments(topicPath, page));
    }

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Нужно авторизоваться, чтобы оставлять комментарии");
        }

        String topicPath = (String) payload.get("topicPath");
        Integer page = (Integer) payload.get("page");
        String content = (String) payload.get("content");
        String imageUrl = (String) payload.get("imageUrl");
        Number parentIdNum = (Number) payload.get("parentId");
        Long parentId = parentIdNum != null ? parentIdNum.longValue() : null;

        if ((content == null || content.isBlank()) && (imageUrl == null || imageUrl.isBlank())) {
            return ResponseEntity.badRequest().body("Комментарий не может быть пустым");
        }

        CommentDto comment = commentService.addComment(topicPath, page != null ? page : 0, content, user, parentId, imageUrl);
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Нужно авторизоваться, чтобы лайкать");
        }

        try {
            CommentDto comment = commentService.likeComment(id, user);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
