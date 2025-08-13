package ru.common.manager;

import ru.common.model.EpicTask;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TaskManager {
    private static int nextId = 1;
    private final Map<Integer, EpicTask> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();

    public static int getNextId() {
        return nextId++;
    }

    public EpicTask createEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        epics.put(epic.getId(), epic);
        return epic;
    }

    public List<EpicTask> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeAllEpics() {
        subTasks.clear();
        epics.clear();
    }

    public EpicTask getEpicById(int id) {
        return epics.get(id);
    }

    public boolean updateEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        if (!epics.containsKey(epic.getId())) return false;
        epics.put(epic.getId(), epic);
        return true;
    }

    public boolean removeEpic(EpicTask epic) {
        if (epic == null) return false;
        subTasks.values().stream()
                .filter(subtask -> subtask.getParentId() == epic.getId())
                .forEach(this::removeTask);
        return epics.remove(epic.getId()) != null;
    }

    public Task createTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        tasks.put(task.getId(), task);
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public boolean updateTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        if (!tasks.containsKey(task.getId())) return false;
        tasks.put(task.getId(), task);
        return true;
    }

    public boolean removeTask(Task task) {
        if (task == null) return false;

        if (task instanceof SubTask subTask) {
            removeSubTaskAndUpdateEpic(subTask);
        }

        return tasks.remove(task.getId()) != null;
    }

    private void removeSubTaskAndUpdateEpic(SubTask subTask) {
        subTasks.remove(subTask.getId());
        EpicTask epicTask = epics.get(subTask.getParentId());
        if (epicTask != null) {
            epicTask.removeSubTaskId(subTask.getId());
            updateEpicStatus(epicTask);
        }
    }

    private void updateEpicStatus(EpicTask epic) {
        epic.setStatus(calculateEpicStatus(epic));
    }

    public SubTask createSubTask(SubTask subTask) {
        Objects.requireNonNull(subTask, "Подзадача не может быть null");
        EpicTask parentTask = epics.get(subTask.getParentId());
        if (parentTask == null) {
            throw new IllegalArgumentException("Родительская задача не найдена");
        }
        subTasks.put(subTask.getId(), subTask);
        tasks.put(subTask.getId(), subTask);
        parentTask.addSubTaskId(subTask.getId());
        updateEpicStatus(parentTask);
        return subTask;
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void removeAllSubTasks() {
        epics.values().forEach(epic -> {
            epic.getSubTaskIds().clear();
            updateEpicStatus(epic);
        });
        subTasks.clear();
    }

    public List<SubTask> getSubTasksByEpicId(int epicId) {
        return subTasks.values().stream()
                .filter(subTask -> subTask.getParentId() == epicId)
                .toList();
    }

    private TaskStatus calculateEpicStatus(EpicTask epic) {
        List<SubTask> epicSubTasks = subTasks.values().stream()
                .filter(subTask -> subTask.getParentId() == epic.getId())
                .toList();

        if (epicSubTasks.isEmpty()) {
            return TaskStatus.NEW;
        }

        boolean hasNewTasks = false;
        boolean hasInProgressTasks = false;
        boolean hasDoneTasks = false;

        for (Task task : epicSubTasks) {
            switch (task.getStatus()) {
                case NEW -> hasNewTasks = true;
                case IN_PROGRESS -> hasInProgressTasks = true;
                case DONE -> hasDoneTasks = true;
            }
        }

        if (hasDoneTasks && !hasNewTasks && !hasInProgressTasks) {
            return TaskStatus.DONE;
        } else if (hasInProgressTasks || (hasNewTasks && hasDoneTasks)) {
            return TaskStatus.IN_PROGRESS;
        }
        return TaskStatus.NEW;
    }
}