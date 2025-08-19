package ru.common.manager.task;

import ru.common.config.Config;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;
import ru.common.model.task.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static int nextId = 1;
    private final Map<Integer, EpicTask> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final List<Task> history = new LinkedList<>();

    public static int getNextId() {
        return nextId++;
    }

    @Override
    public EpicTask createEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        EpicTask stored = epic.copy();
        epics.put(stored.getId(), stored);
        return stored.copy();
    }

    @Override
    public List<EpicTask> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        subTasks.clear();
        epics.clear();
    }

    @Override
    public EpicTask getEpicById(int id) {
        EpicTask epic = epics.get(id);
        addToHistory(epic);
        return epic;
    }

    @Override
    public boolean updateEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        if (!epics.containsKey(epic.getId())) return false;
        epics.put(epic.getId(), epic);
        return true;
    }

    @Override
    public boolean removeEpic(EpicTask epic) {
        if (epic == null) return false;
        subTasks.values().stream()
                .filter(subtask -> subtask.getParentId() == epic.getId())
                .forEach(this::removeTask);
        return epics.remove(epic.getId()) != null;
    }

    @Override
    public Task createTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        Task stored = (task instanceof SubTask st) ? st.copy() : task.copy();
        tasks.put(stored.getId(), stored);
        if (stored instanceof SubTask) {
            subTasks.put(stored.getId(), (SubTask) stored);
        }
        return stored.copy();
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        addToHistory(task);
        return task == null ? null : task.copy();
    }

    @Override
    public boolean updateTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        if (!tasks.containsKey(task.getId())) return false;
        Task target = tasks.get(task.getId());
        target.setName(task.getName());
        target.setDescription(task.getDescription());
        target.setStatus(task.getStatus());
        if (task instanceof SubTask subTask) {
            EpicTask epicTask = epics.get(subTask.getParentId());
            if (epicTask != null) {
                updateEpicStatus(epicTask);
            }
        }
        return true;
    }

    @Override
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

    @Override
    public SubTask createSubTask(SubTask subTask) {
        Objects.requireNonNull(subTask, "Подзадача не может быть null");
        EpicTask parentTask = epics.get(subTask.getParentId());
        if (parentTask == null) {
            throw new IllegalArgumentException("Родительская задача не найдена");
        }
        SubTask stored = subTask.copy();
        subTasks.put(stored.getId(), stored);
        tasks.put(stored.getId(), stored);
        parentTask.addSubTaskId(stored.getId());
        updateEpicStatus(parentTask);
        return stored.copy();
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void removeAllSubTasks() {
        epics.values().forEach(epic -> {
            epic.clearSubTaskIds();
            updateEpicStatus(epic);
        });
        subTasks.clear();
        tasks.entrySet().removeIf(entry -> entry.getValue() instanceof SubTask);
    }

    @Override
    public List<SubTask> getSubTasksByEpicId(int epicId) {
        return subTasks.values().stream()
                .filter(subTask -> subTask.getParentId() == epicId)
                .toList();
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        addToHistory(subTask);
        return subTask == null ? null : subTask.copy();
    }

    private <T extends Task> void addToHistory(T task) {
        if (task == null) return;
        history.add(task.copy());
        if (history.size() > Config.HISTORY_COLLECTION_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
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

