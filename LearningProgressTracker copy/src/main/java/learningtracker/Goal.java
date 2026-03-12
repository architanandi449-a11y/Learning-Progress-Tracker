package learningtracker;

import java.time.LocalDateTime;

public class Goal {
    private String subject;
    private String description;
    private LocalDateTime deadline;
    private boolean completed;
    private String notes;

    public Goal(String subject, String description, LocalDateTime deadline, String notes) {
        this.subject = subject;
        this.description = description;
        this.deadline = deadline;
        this.completed = false;
        this.notes = notes;
    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}