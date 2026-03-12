package learningtracker;

import java.time.LocalDateTime;

public class Lesson {
    private String subject;
    private LocalDateTime dateTime;
    private int duration;
    private String notes;

    public Lesson(String subject, LocalDateTime dateTime, int duration, String notes) {
        this.subject = subject;
        this.dateTime = dateTime;
        this.duration = duration;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}