package ru.common.model.task;

import ru.common.manager.task.InMemoryTaskManager;
import ru.common.util.CustomDateTimeFormatter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;
    private LocalDateTime startTime;
    private Duration duration;

    Task(int id, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.description = description;
        this.status = Objects.requireNonNull(status, "Статус не может быть null");
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, Integer parentId, LocalDateTime startTime, Duration duration) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, int parentId, LocalDateTime startTime, Duration duration) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, LocalDateTime startTime, Duration duration) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Описание не может быть null");
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "Статус не может быть null");
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Задача №%d: %s\n" +
                        "Статус: %s\n" +
                        "Описание: %s\n" +
                        "Начало: %s\n" +
                        "Длительность: %d ч. %d мин.\n",
                this.id, this.name, this.status, this.description,
                startTime != null ? startTime.format(CustomDateTimeFormatter.DATE_TIME_FORMATTER) : "Не задано",
                duration != null ? duration.toHours() : 0,
                duration != null ? duration.toMinutesPart() : 0
        );
    }

    private TaskType getTaskType() {
        if (this instanceof EpicTask) {
            return TaskType.EPIC;
        } else if (this instanceof SubTask) {
            return TaskType.SUBTASK;
        } else {
            return TaskType.TASK;
        }
    }

    public String toCSVString() {
        String startTimeStr = (getStartTime() != null) ? getStartTime().toString() : "";
        String durationStr = (getDuration() != null) ? String.valueOf(getDuration().toMinutes()) : "";
        return String.format("%d,%s,%s,%s,%s,,%s,%s", this.id, getTaskType(), this.name, this.status, this.description, startTimeStr, durationStr);
    }
}