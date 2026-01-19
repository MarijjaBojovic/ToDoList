package model;
import java.time.LocalDate;
public class Task {
    private String name;
    private String description;
    private LocalDate deadline;
    private boolean completed;

    public Task( String name, String description, LocalDate deadline, boolean completed) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
    }

    public String getName(){return name;}
    public String getDescription() {
        return description;
    }
    public LocalDate getDeadline(){
        return deadline;
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
