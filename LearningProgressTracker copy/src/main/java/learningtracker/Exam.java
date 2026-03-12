package learningtracker;

import java.time.LocalDateTime;

public class Exam {
    private String subject;
    private LocalDateTime dateTime;
    private int score;
    private String notes;

    public Exam(String subject, LocalDateTime dateTime, int score, String notes) {
        this.subject = subject;
        this.dateTime = dateTime;
        this.score = score;
        this.notes = notes;
    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}