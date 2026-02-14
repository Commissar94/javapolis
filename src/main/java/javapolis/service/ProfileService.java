package javapolis.service;

import javapolis.dto.UserProfileDTO;
import javapolis.model.User;
import javapolis.model.UserProgress;
import javapolis.repository.UserRepository;
import javapolis.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProgressRepository progressRepository;

    @Autowired
    private TopicService topicService;

    public Optional<UserProfileDTO> getUserProfile(String username) {
        return userRepository.findByUsername(username).map(this::convertToDTO);
    }

    private UserProfileDTO convertToDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setCoins(user.getCoins());

        long totalTopics = topicService.countTotalTopics();
        List<UserProgress> progress = progressRepository.findByUser(user);
        long completedTopics = progress.stream().filter(UserProgress::isCompleted).count();

        dto.setTotalTopics(totalTopics);
        dto.setCompletedTopics(completedTopics);
        
        if (totalTopics > 0) {
            dto.setCompletionPercentage((int) ((completedTopics * 100) / totalTopics));
        } else {
            dto.setCompletionPercentage(0);
        }

        return dto;
    }
}
