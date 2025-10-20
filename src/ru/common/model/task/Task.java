package ru.common.model.task;

import ru.common.manager.task.InMemoryTaskManager;

import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private TaskStatus status;


    Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.description = description;
        this.status = Objects.requireNonNull(status, "Статус не может быть null");
    }

    public Task(String name, String description, Integer parentId) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.description = description;
    }

    public Task(String name, String description) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.description = description;
    }

    public Task(String name, int parentId) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
    }

    public Task(String name) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null").trim();
        this.id = InMemoryTaskManager.getNextId();
        this.status = TaskStatus.NEW;
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
        return "ru.common.model.task.Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
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
        // CSV: id,type,name,status,description,epic
        return String.format("%d,%s,%s,%s,%s,", this.id, getTaskType(), this.name, this.status, this.description);
    }
}