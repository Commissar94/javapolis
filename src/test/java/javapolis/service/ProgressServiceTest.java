package javapolis.service;

import javapolis.model.User;
import javapolis.model.UserProgress;
import javapolis.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceTest {

    @Mock
    private UserProgressRepository progressRepository;

    @InjectMocks
    private ProgressService progressService;

    @Test
    public void testMarkPageAsCompleted_LastPage() {
        User user = new User();
        String topicPath = "test/topic";
        int page = 2;
        int totalPages = 3;

        UserProgress progress = new UserProgress(user, topicPath);
        when(progressRepository.findByUserAndTopicPath(user, topicPath)).thenReturn(Optional.of(progress));

        progressService.markPageAsCompleted(user, topicPath, page, totalPages);

        assertTrue(progress.isCompleted(), "Topic should be completed when the last page is reached");
        verify(progressRepository).save(progress);
    }

    @Test
    public void testMarkPageAsCompleted_NotLastPage() {
        User user = new User();
        String topicPath = "test/topic";
        int page = 0;
        int totalPages = 3;

        UserProgress progress = new UserProgress(user, topicPath);
        when(progressRepository.findByUserAndTopicPath(user, topicPath)).thenReturn(Optional.of(progress));

        progressService.markPageAsCompleted(user, topicPath, page, totalPages);

        assertFalse(progress.isCompleted(), "Topic should not be completed when not on the last page");
        verify(progressRepository).save(progress);
    }
}
