package ru.common.model.task;

import ru.common.manager.task.InMemoryTaskManager;

import java.util.Objects;

public class Task {
    private final int id;
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

    public Task copy() {
        return new Task(id, name, description, status);
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
}