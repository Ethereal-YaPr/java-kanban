package ru.common.manager;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    //private final List<Integer> tasks = new ArrayList<>();

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
}

