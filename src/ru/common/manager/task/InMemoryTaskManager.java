package ru.common.manager.task;

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
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    public static int getNextId() {
        return nextId++;
    }

    @Override
    public EpicTask createEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public List<EpicTask> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        for (SubTask st : new ArrayList<>(subTasks.values())) {
            historyManager.removeById(st.getId());
        }
        for (EpicTask e : new ArrayList<>(epics.values())) {
            historyManager.removeById(e.getId());
        }
        subTasks.clear();
        epics.clear();
    }

    @Override
    public EpicTask getEpicById(int id) {
        EpicTask epic = epics.get(id);
        historyManager.add(epic);
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
        EpicTask storedEpic = epics.get(epic.getId());
        for (Integer subId : storedEpic.getSubTaskIds()) { // уже копия
            SubTask st = subTasks.get(subId);
            if (st != null) {
                removeTask(st);
            }
        }
        boolean removed = epics.remove(epic.getId()) != null;
        if (removed) {
            historyManager.removeById(epic.getId());
        }
        return removed;
    }

    @Override
    public Task createTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        tasks.put(task.getId(), task);
        if (task instanceof SubTask) {
            subTasks.put(task.getId(), (SubTask) task);
        }
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        for (Task t : new ArrayList<>(tasks.values())) {
            if (!(t instanceof SubTask)) {
                historyManager.removeById(t.getId());
            }
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public boolean updateTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        if (!tasks.containsKey(task.getId())) return false;
        tasks.put(task.getId(), task);
        if (task instanceof SubTask) {
            subTasks.put(task.getId(), (SubTask) task);
        }
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

        boolean removed = tasks.remove(task.getId()) != null;
        if (removed) {
            historyManager.removeById(task.getId());
        }
        return removed;
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
        subTasks.put(subTask.getId(), subTask);
        tasks.put(subTask.getId(), subTask);
        parentTask.addSubTaskId(subTask.getId());
        updateEpicStatus(parentTask);
        return subTask;
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
        for (SubTask st : new ArrayList<>(subTasks.values())) {
            historyManager.removeById(st.getId());
        }
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
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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

