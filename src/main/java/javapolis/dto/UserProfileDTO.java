package javapolis.dto;

import java.time.LocalDateTime;

public class UserProfileDTO {
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private long totalTopics;
    private long completedTopics;
    private int completionPercentage;

    public UserProfileDTO() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getTotalTopics() { return totalTopics; }
    public void setTotalTopics(long totalTopics) { this.totalTopics = totalTopics; }

    public long getCompletedTopics() { return completedTopics; }
    public void setCompletedTopics(long completedTopics) { this.completedTopics = completedTopics; }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
}
