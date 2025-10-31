package ru.common.manager.task;

import ru.common.manager.exceptions.NotFoundException;
import ru.common.manager.history.HistoryManager;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;
import ru.common.model.task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static int nextId = 1;
    private final Map<Integer, EpicTask> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
        LocalDateTime startTime1 = task1.getStartTime();
        LocalDateTime startTime2 = task2.getStartTime();
        if (startTime1 != null && startTime2 != null) {
            int timeComparison = startTime1.compareTo(startTime2);
            return timeComparison != 0 ? timeComparison : Integer.compare(task1.getId(), task2.getId());
        }
        return Integer.compare(task1.getId(), task2.getId());
    });

    public static int getNextId() {
        return nextId++;
    }

    @Override
    public EpicTask createEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        epics.put(epic.getId(), epic);
        updateEpicTimeFields(epic);
        return epic;
    }

    @Override
    public List<EpicTask> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllEpics() {
        new ArrayList<>(subTasks.values()).forEach(subTask ->
                historyManager.removeById(subTask.getId())
        );
        new ArrayList<>(epics.values()).forEach(epic ->
                historyManager.removeById(epic.getId())
        );
        subTasks.clear();
        epics.clear();
    }

    @Override
    public EpicTask getEpicById(int id) {
        EpicTask epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public boolean updateEpic(EpicTask epic) {
        Objects.requireNonNull(epic, "Эпик не может быть null");
        if (!epics.containsKey(epic.getId())) return false;
        epics.put(epic.getId(), epic);
        updateEpicTimeFields(epic);
        return true;
    }

    @Override
    public boolean removeEpic(EpicTask epic) {
        if (epic == null || !epics.containsKey(epic.getId()))
            throw new NotFoundException("Epic with id " + (epic != null ? epic.getId() : null) + " not found");
        EpicTask storedEpic = epics.get(epic.getId());
        storedEpic.getSubTaskIds().stream()
                .map(subTasks::get)
                .filter(Objects::nonNull)
                .forEach(this::removeTask);
        boolean removed = epics.remove(epic.getId()) != null;
        if (removed) {
            historyManager.removeById(epic.getId());
        }
        return removed;
    }

    @Override
    public Task createTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        if (task instanceof SubTask) {
            throw new IllegalArgumentException("Для подзадач используйте createSubTask");
        }
        Task conflictingTask = findConflictingTask(task);
        if (conflictingTask != null) {
            throw new IllegalArgumentException(String.format(
                    "Задача '%s' (ID: %d, время: %s) пересекается по времени с задачей '%s' (ID: %d, время: %s)",
                    task.getName(), task.getId(), task.getStartTime(),
                    conflictingTask.getName(), conflictingTask.getId(), conflictingTask.getStartTime()
            ));
        }
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        new ArrayList<>(tasks.values()).stream()
                .filter(task -> !(task instanceof SubTask))
                .forEach(task -> {
                    historyManager.removeById(task.getId());
                    removeFromPrioritizedTasks(task);
                });
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public boolean updateTask(Task task) {
        Objects.requireNonNull(task, "Задача не может быть null");
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if (!subTasks.containsKey(subTask.getId())) return false;
            removeFromPrioritizedTasks(subTask);
            Task conflictingTask = findConflictingTask(subTask);
            if (conflictingTask != null) {
                addToPrioritizedTasks(subTask);
                throw new IllegalArgumentException(String.format(
                        "Подзадача '%s' (ID: %d, время: %s) пересекается по времени с задачей '%s' (ID: %d, время: %s)",
                        subTask.getName(), subTask.getId(), subTask.getStartTime(),
                        conflictingTask.getName(), conflictingTask.getId(), conflictingTask.getStartTime()
                ));
            }
            subTasks.put(subTask.getId(), subTask);
            updateInPrioritizedTasks(subTask);
            EpicTask epicTask = epics.get(subTask.getParentId());
            if (epicTask != null) {
                updateEpicStatus(epicTask);
            }
            return true;
        }
        if (!tasks.containsKey(task.getId())) return false;
        removeFromPrioritizedTasks(task);
        Task conflictingTask = findConflictingTask(task);
        if (conflictingTask != null) {
            addToPrioritizedTasks(task);
            throw new IllegalArgumentException(String.format(
                    "Задача '%s' (ID: %d, время: %s) пересекается по времени с задачей '%s' (ID: %d, время: %s)",
                    task.getName(), task.getId(), task.getStartTime(),
                    conflictingTask.getName(), conflictingTask.getId(), conflictingTask.getStartTime()
            ));
        }
        tasks.put(task.getId(), task);
        updateInPrioritizedTasks(task);
        return true;
    }

    @Override
    public boolean removeTask(Task task) {
        if (task == null) throw new NotFoundException("Task is null");
        boolean removed;
        if (task instanceof SubTask subTask) {
            if (!subTasks.containsKey(subTask.getId())) {
                throw new NotFoundException("SubTask with id " + subTask.getId() + " not found");
            }
            removed = subTasks.remove(subTask.getId()) != null;
            if (removed) {
                removeFromPrioritizedTasks(subTask);
                EpicTask epicTask = epics.get(subTask.getParentId());
                if (epicTask != null) {
                    epicTask.removeSubTaskId(subTask.getId());
                    updateEpicStatus(epicTask);
                }
            }
        } else {
            if (!tasks.containsKey(task.getId())) {
                throw new NotFoundException("Task with id " + task.getId() + " not found");
            }
            removed = tasks.remove(task.getId()) != null;
            if (removed) {
                removeFromPrioritizedTasks(task);
            }
        }
        if (removed) {
            historyManager.removeById(task.getId());
        }
        return removed;
    }

    private void updateEpicStatus(EpicTask epic) {
        epic.setStatus(calculateEpicStatus(epic));
        updateEpicTimeFields(epic);
    }

    private void updateEpicTimeFields(EpicTask epic) {
        List<SubTask> epicSubTasks = getSubTasksByEpicId(epic.getId());
        LocalDateTime startTime = epic.calculateStartTime(epicSubTasks);
        epic.setCalculatedStartTime(startTime);
        Duration duration = epic.calculateDuration(epicSubTasks);
        epic.setCalculatedDuration(duration);
        LocalDateTime endTime = epic.calculateEndTime(epicSubTasks);
        epic.setCalculatedEndTime(endTime);
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        Objects.requireNonNull(subTask, "Подзадача не может быть null");
        EpicTask parentTask = epics.get(subTask.getParentId());
        if (parentTask == null) {
            throw new IllegalArgumentException("Родительская задача не найдена");
        }
        Task conflictingTask = findConflictingTask(subTask);
        if (conflictingTask != null) {
            throw new IllegalArgumentException(String.format(
                    "Подзадача '%s' (ID: %d, время: %s) пересекается по времени с задачей '%s' (ID: %d, время: %s)",
                    subTask.getName(), subTask.getId(), subTask.getStartTime(),
                    conflictingTask.getName(), conflictingTask.getId(), conflictingTask.getStartTime()
            ));
        }
        subTasks.put(subTask.getId(), subTask);
        addToPrioritizedTasks(subTask);
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
        new ArrayList<>(subTasks.values()).forEach(subTask -> {
            historyManager.removeById(subTask.getId());
            removeFromPrioritizedTasks(subTask);
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
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        Objects.requireNonNull(subTask, "Подзадача не может быть null");
        if (!subTasks.containsKey(subTask.getId())) return false;
        removeFromPrioritizedTasks(subTask);
        Task conflictingTask = findConflictingTask(subTask);
        if (conflictingTask != null) {
            addToPrioritizedTasks(subTask);
            throw new IllegalArgumentException(String.format(
                    "Подзадача '%s' (ID: %d, время: %s) пересекается по времени с задачей '%s' (ID: %d, время: %s)",
                    subTask.getName(), subTask.getId(), subTask.getStartTime(),
                    conflictingTask.getName(), conflictingTask.getId(), conflictingTask.getStartTime()
            ));
        }
        subTasks.put(subTask.getId(), subTask);
        addToPrioritizedTasks(subTask);
        EpicTask parentEpic = epics.get(subTask.getParentId());
        if (parentEpic != null) {
            updateEpicStatus(parentEpic);
            updateEpicTimeFields(parentEpic);
        }
        return true;
    }

    @Override
    public boolean removeSubTask(int subTaskId) {
        if (!subTasks.containsKey(subTaskId))
            throw new NotFoundException("SubTask with id " + subTaskId + " not found");
        SubTask subTask = subTasks.get(subTaskId);
        historyManager.removeById(subTaskId);
        removeFromPrioritizedTasks(subTask);
        EpicTask parentEpic = epics.get(subTask.getParentId());
        if (parentEpic != null) {
            parentEpic.removeSubTaskId(subTaskId);
        }
        subTasks.remove(subTaskId);
        if (parentEpic != null) {
            updateEpicStatus(parentEpic);
            updateEpicTimeFields(parentEpic);
        }
        return true;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public String getHistoryAsString() {
        return historyManager.getHistoryAsString();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean hasTimeOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime start2 = task2.getStartTime();
        Duration duration1 = task1.getDuration();
        Duration duration2 = task2.getDuration();
        if (start1 == null || start2 == null || duration1 == null || duration2 == null) {
            return false;
        }
        if (task1.getId() == task2.getId()) {
            return false;
        }
        if (duration1.isZero() || duration2.isZero()) {
            return false;
        }
        LocalDateTime end1 = start1.plus(duration1);
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private Task findConflictingTask(Task newTask) {
        LocalDateTime newStart = newTask.getStartTime();
        Duration newDuration = newTask.getDuration();
        if (newStart == null || newDuration == null) {
            return null;
        }
        return prioritizedTasks.stream()
                .filter(existingTask -> hasTimeOverlap(newTask, existingTask))
                .findFirst()
                .orElse(null);
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task);
    }

    private void updateInPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task);
        addToPrioritizedTasks(task);
    }

    private TaskStatus calculateEpicStatus(EpicTask epic) {
        List<SubTask> epicSubTasks = subTasks.values().stream()
                .filter(subTask -> subTask.getParentId() == epic.getId())
                .toList();
        if (epicSubTasks.isEmpty()) {
            return TaskStatus.NEW;
        }
        boolean hasNewTasks = epicSubTasks.stream()
                .anyMatch(task -> task.getStatus() == TaskStatus.NEW);
        boolean hasInProgressTasks = epicSubTasks.stream()
                .anyMatch(task -> task.getStatus() == TaskStatus.IN_PROGRESS);
        boolean hasDoneTasks = epicSubTasks.stream()
                .anyMatch(task -> task.getStatus() == TaskStatus.DONE);
        if (hasDoneTasks && !hasNewTasks && !hasInProgressTasks) {
            return TaskStatus.DONE;
        } else if (hasInProgressTasks || (hasNewTasks && hasDoneTasks)) {
            return TaskStatus.IN_PROGRESS;
        }
        return TaskStatus.NEW;
    }
}