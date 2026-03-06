package javapolis.controller;

import javapolis.model.User;
import javapolis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/museum")
public class MuseumController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/top-players")
    public ResponseEntity<List<Map<String, Object>>> getTopPlayers() {
        // Получаем топ-10 игроков по количеству монет (полисов)
        List<User> topUsers = userRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "coins"))
        ).getContent();

        List<Map<String, Object>> result = topUsers.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("username", user.getUsername());
            map.put("coins", user.getCoins());
            map.put("avatar", "https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getUsername());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
