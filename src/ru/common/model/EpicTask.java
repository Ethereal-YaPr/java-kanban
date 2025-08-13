package ru.common.model;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private final List<Integer> subTaskIds = new ArrayList<>();

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
        if (!subTaskIds.contains(subTaskId)) {
            subTaskIds.add(subTaskId);
        }
    }

    public void removeSubTaskId(int subTaskId) {
        subTaskIds.remove(Integer.valueOf(subTaskId));
    }
}

