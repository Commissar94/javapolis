package javapolis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "topic_path"})
})
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "topic_path", nullable = false)
    private String topicPath;

    @Column(name = "completed_pages", nullable = false)
    private String completedPages; // Сохраняем как строку через запятую "0,1,2"

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserProgress() {}

    public UserProgress(User user, String topicPath) {
        this.user = user;
        this.topicPath = topicPath;
        this.completedPages = "";
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    @PrePersist
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTopicPath() { return topicPath; }
    public void setTopicPath(String topicPath) { this.topicPath = topicPath; }

    public String getCompletedPages() { return completedPages; }
    public void setCompletedPages(String completedPages) { this.completedPages = completedPages; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
