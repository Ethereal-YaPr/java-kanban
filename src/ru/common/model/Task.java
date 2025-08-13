package ru.common.model;

import ru.common.manager.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task {
    private final int id;
    private String name;
    private String description;
    private TaskStatus status;


    public Task(String name, String description, Integer parentId) {
        this.name = Objects.requireNonNull(name.trim(), "Имя не может быть null");
        this.id = TaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.description = description;
    }

    public Task(String name, String description) {
        this.name = Objects.requireNonNull(name.trim(), "Имя не может быть null");
        this.id = TaskManager.getNextId();
        this.status = TaskStatus.NEW;
        this.description = description;
    }

    public Task(String name, int parentId) {
        this.name = Objects.requireNonNull(name.trim(), "Имя не может быть null");
        this.id = TaskManager.getNextId();
        this.status = TaskStatus.NEW;
    }

    public Task(String name) {
        this.name = Objects.requireNonNull(name.trim(), "Имя не может быть null");
        this.id = TaskManager.getNextId();
        this.status = TaskStatus.NEW;
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name.trim(), "Имя не может быть null");
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
        return "ru.common.model.Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}