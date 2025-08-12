package ru.common.model;

import ru.common.manager.EpicTask;
import ru.common.manager.SubTask;
import ru.common.manager.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskManager {
    private static int nextId = 1;
    private final List<EpicTask> epics = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<SubTask> subTasks = new ArrayList<>();

    public static int getNextId() {
        return nextId++;
    }

    public EpicTask createEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        epics.add(epic);
        return epic;
    }

    public List<EpicTask> getAllEpics() {
        return new ArrayList<>(epics);
    }

    public void removeAllEpics() {
        epics.forEach(epic ->
                tasks.stream()
                        .filter(task -> epic.getId() == task.getParentId())
                        .forEach(this::removeTask)
        );
        epics.clear();
    }

    public EpicTask getEpicById(int id) {
        return epics.stream()
                .filter(epic -> epic.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean updateEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        int index = getIndexById(epics, epic.getId());
        if (index == -1) return false;
        epics.set(index, epic);
        return true;
    }

    public boolean removeEpic(EpicTask epic) {
        if (epic == null) return false;
        tasks.removeIf(task -> epic.getId() == task.getParentId());
        return epics.remove(epic);
    }

    public Task createTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        tasks.add(task);
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public void removeAllTasks() {
        tasks.forEach(task ->
                subTasks.removeIf(subTask -> task.getId() == subTask.getParentId())
        );
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean updateTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        int index = getIndexById(tasks, task.getId());
        if (index == -1) return false;

        Task existingTask = tasks.get(index);
        task.setParentId(existingTask.getParentId());
        tasks.set(index, task);
        return true;
    }

    public boolean removeTask(Task task) {
        if (task == null) return false;
        subTasks.removeIf(subTask -> task.getId() == subTask.getParentId());
        return tasks.remove(task);
    }

    // Методы для работы с подзадачами
    public SubTask createSubTask(SubTask subTask) {
        Objects.requireNonNull(subTask, "Подзадача не может быть null");
        if (getTaskById(subTask.getParentId()) == null) {
            throw new IllegalArgumentException("Родительская задача не найдена");
        }
        subTasks.add(subTask);
        Task parentTask = getTaskById(subTask.getParentId());
        parentTask.addSubTaskId(subTask.getId());
        return subTask;
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks);
    }

    public void removeAllSubTasks() {
        tasks.forEach(task -> task.getSubTaskIds().clear());
        subTasks.clear();
    }

    public List<SubTask> getSubTasksByEpicId(int epicId) {
        List<SubTask> result = new ArrayList<>();
        List<Task> epicTasks = tasks.stream()
                .filter(task -> task.getParentId() != null && task.getParentId() == epicId)
                .toList();

        for (Task task : epicTasks) {
            subTasks.stream()
                    .filter(subTask -> subTask.getParentId() == task.getId())
                    .forEach(result::add);
        }
        return result;
    }

    private <T extends Task> int getIndexById(List<T> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
}