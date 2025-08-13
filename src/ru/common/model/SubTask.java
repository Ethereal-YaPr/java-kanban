package ru.common.model;

public class SubTask extends Task {
    private Integer parentId;

    public SubTask(String name, String description, int parentId) {
        super(name, description);
        this.parentId = parentId;
    }

    public SubTask(String name, int parentId) {
        super(name);
        this.parentId = parentId;
    }

    public SubTask(String name) {
        super(name);
    }
    public SubTask(String name, String description) {
        super(name, description);
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
}