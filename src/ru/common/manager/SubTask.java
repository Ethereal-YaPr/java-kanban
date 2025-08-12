package ru.common.manager;

import java.util.Objects;

public class SubTask extends Task {

    public SubTask(String name, String description, int parentId) {
        super(name, description, parentId);
    }

    public SubTask(String name, int parentId) {
        super(name, parentId);
    }

    public SubTask(String name) {
        super(name);
    }
    public SubTask(String name, String description) {
        super(name, description);
    }
}