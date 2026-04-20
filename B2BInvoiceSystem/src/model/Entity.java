package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// base class for all model objects
public abstract class Entity {

    private int id;
    private LocalDateTime createdAt;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Entity() {
        this.createdAt = LocalDateTime.now();
    }

    // constructor for loading from db
    public Entity(int id) {
        this.id        = id;
        this.createdAt = LocalDateTime.now();
    }

    // display info
    public abstract void displayInfo();

    public abstract String getEntityType();

    // getters and setters
    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public LocalDateTime getCreatedAt()   { return createdAt; }
    public void setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }

    public String getFormattedDate() {
        return createdAt != null ? createdAt.format(FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        return String.format("[%s] ID: %d | Created: %s",
                getEntityType(), id, getFormattedDate());
    }
}
