package ru.common.model.task;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<Integer> subTaskIds = new ArrayList<>();

    EpicTask(int id, String name, String description, TaskStatus status, List<Integer> subTaskIds) {
        super(id, name, description, status);
        if (subTaskIds != null) {
            for (Integer st : subTaskIds) {
                if (st != null && !this.subTaskIds.contains(st) && st != id) {
                    this.subTaskIds.add(st);
                }
            }
        }
    }

    public EpicTask(String name, String description) {
        super(name, description);
    }
    public EpicTask(String name) {
        super(name);
    }
    public EpicTask(String name, int parentId) {
        super(name, parentId);
    }
    public EpicTask(String name, String description, int parentId) {
        super(name, description, parentId);
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

    public EpicTask copy() {
        return new EpicTask(getId(), getName(), getDescription(), getStatus(), new ArrayList<>(subTaskIds));
    }
}

