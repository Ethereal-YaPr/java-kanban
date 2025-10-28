package ru.common.model.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<Integer> subTaskIds = new ArrayList<>();
    private LocalDateTime calculatedEndTime;

    EpicTask(int id, String name, String description, TaskStatus status, List<Integer> subTaskIds) {
        super(id, name, description, status, null, null);
        if (subTaskIds != null) {
            for (Integer st : subTaskIds) {
                if (st != null && !this.subTaskIds.contains(st) && st != id) {
                    this.subTaskIds.add(st);
                }
            }
        }
    }

    public EpicTask(String name, String description) {
        super(name, description, null, null);
    }

    public EpicTask(String name) {
        super(name, null, null);
    }

    public EpicTask(String name, int parentId) {
        super(name, parentId, null, null);
    }

    public EpicTask(String name, String description, int parentId) {
        super(name, description, parentId, null, null);
    }

    public List<Integer> getSubTaskIds() {
        return new ArrayList<>(subTaskIds);
    }

    public void addSubTaskId(int subTaskId) {
        if (subTaskId == getId()) return;
        if (!subTaskIds.contains(subTaskId)) {
            subTaskIds.add(subTaskId);
        }
    }

    public void removeSubTaskId(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }

    public void clearSubTaskIds() {
        subTaskIds.clear();
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override
    public LocalDateTime getEndTime() {
        return calculatedEndTime;
    }

    public LocalDateTime calculateStartTime(List<SubTask> subTasks) {
        if (subTasks.isEmpty()) {
            return null;
        }
        LocalDateTime earliestStart = null;
        for (SubTask subTask : subTasks) {
            if (subTask.getStartTime() != null) {
                if (earliestStart == null || subTask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subTask.getStartTime();
                }
            }
        }
        return earliestStart;
    }

    public Duration calculateDuration(List<SubTask> subTasks) {
        if (subTasks.isEmpty()) {
            return Duration.ZERO;
        }
        Duration totalDuration = Duration.ZERO;
        for (SubTask subTask : subTasks) {
            if (subTask.getDuration() != null) {
                totalDuration = totalDuration.plus(subTask.getDuration());
            }
        }
        return totalDuration;
    }

    public LocalDateTime calculateEndTime(List<SubTask> subTasks) {
        if (subTasks.isEmpty()) {
            return null;
        }
        LocalDateTime latestEnd = null;
        for (SubTask subTask : subTasks) {
            LocalDateTime subTaskEndTime = subTask.getEndTime();
            if (subTaskEndTime != null) {
                if (latestEnd == null || subTaskEndTime.isAfter(latestEnd)) {
                    latestEnd = subTaskEndTime;
                }
            }
        }
        return latestEnd;
    }

    public void setCalculatedStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
    }

    public void setCalculatedDuration(Duration duration) {
        super.setDuration(duration);
    }

    public void setCalculatedEndTime(LocalDateTime endTime) {
        this.calculatedEndTime = endTime;
    }

    @Override
    public String toCSVString() {
        String startTimeStr = (getStartTime() != null) ? getStartTime().toString() : "";
        String durationStr = (getDuration() != null) ? String.valueOf(getDuration().toMinutes()) : "";
        return String.format("%d,%s,%s,%s,%s,,%s,%s",
                this.getId(),
                TaskType.EPIC,
                this.getName(),
                this.getStatus(),
                this.getDescription(),
                startTimeStr,
                durationStr);
    }
}