package javapolis.service;

import javapolis.model.User;
import javapolis.model.UserProgress;
import javapolis.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    @Autowired
    private UserProgressRepository progressRepository;

    @Transactional
    public void markPageAsCompleted(User user, String topicPath, int page, int totalPages) {
        UserProgress progress = progressRepository.findByUserAndTopicPath(user, topicPath)
                .orElse(new UserProgress(user, topicPath));

        Set<Integer> completedPages = parsePages(progress.getCompletedPages());
        completedPages.add(page);
        
        String pagesStr = completedPages.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        
        progress.setCompletedPages(pagesStr);
        
        // Лекция считается пройденной, если отмечена последняя страница
        if (page == totalPages - 1) {
            progress.setCompleted(true);
        }

        progressRepository.save(progress);
    }

    public List<UserProgress> getUserProgress(User user) {
        return progressRepository.findByUser(user);
    }

    public Optional<UserProgress> getTopicProgress(User user, String topicPath) {
        return progressRepository.findByUserAndTopicPath(user, topicPath);
    }

    private Set<Integer> parsePages(String pagesStr) {
        if (pagesStr == null || pagesStr.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(pagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }
}
