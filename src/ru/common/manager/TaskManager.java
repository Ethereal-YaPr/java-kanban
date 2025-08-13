package ru.common.manager;

import ru.common.model.EpicTask;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.manager.TaskManager;
import ru.common.model.TaskStatus;

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
        List<EpicTask> epicsToRemove = new ArrayList<>(epics);
        for (EpicTask epic : epicsToRemove) {
            List<Task> tasksToRemove = tasks.stream()
                    .filter(task -> task instanceof SubTask && ((SubTask) task).getParentId() == epic.getId())
                    .toList();
            for (Task task : tasksToRemove) {
                removeTask(task);
            }
        }
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
        List<Task> tasksToRemove = tasks.stream()
                .filter(task -> epic.getId() == task.getId())
                .toList();
        tasksToRemove.forEach(this::removeTask);
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
        List<Task> tasksToRemove = new ArrayList<>(tasks);
        tasksToRemove.forEach(this::removeTask);
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
        tasks.set(index, task);
        return true;
    }

    public boolean removeTask(Task task) {
        if (task == null) return false;

        List<SubTask> subtasksToRemove = subTasks.stream()
                .filter(st -> task.getId() == st.getId())
                .toList();
        subtasksToRemove.forEach(st -> subTasks.remove(st));

        if (task instanceof SubTask subTask) {
            Task parentTask = getTaskById(subTask.getParentId());
            if (parentTask instanceof EpicTask epicTask) {
                epicTask.removeSubTaskId(task.getId());
                epicTask.setStatus(calculateEpicStatus(epicTask));
            }
        }

        return tasks.remove(task);
    }

    public SubTask createSubTask(SubTask subTask) {
        Objects.requireNonNull(subTask, "Подзадача не может быть null");
        Task parentTask = getEpicById(subTask.getParentId());
        if (parentTask == null) {
            throw new IllegalArgumentException("Родительская задача не найдена");
        }
        subTasks.add(subTask);
        tasks.add(subTask);
        if (parentTask instanceof EpicTask epicTask) {
            epicTask.addSubTaskId(subTask.getId());
        }
        return subTask;
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks);
    }

    public void removeAllSubTasks() {
        epics.forEach(epic -> {
            epic.getSubTaskIds().clear();
            epic.setStatus(calculateEpicStatus(epic));
        });

        subTasks.clear();
    }

    public List<SubTask> getSubTasksByEpicId(int epicId) {
        return subTasks.stream()
                .filter(subTask -> subTask.getParentId() == epicId)
                .toList();
    }


    private TaskStatus calculateEpicStatus(EpicTask epic) {
        List<Task> epicTasks = tasks.stream()
                .filter(task -> task instanceof SubTask && ((SubTask) task).getParentId() == epic.getId())
                .toList();

        if (epicTasks.isEmpty()) {
            return TaskStatus.NEW;
        }

        boolean hasNewTasks = false;
        boolean hasInProgressTasks = false;
        boolean hasDoneTasks = false;

        for (Task task : epicTasks) {
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

    private <T extends Task> int getIndexById(List<T> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
}