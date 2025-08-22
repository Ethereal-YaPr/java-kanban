package ru.common.model.task;

public class SubTask extends Task {
    private Integer parentId;

    SubTask(int id, String name, String description, TaskStatus status, Integer parentId) {
        super(id, name, description, status);
        this.parentId = parentId;
    }

    public SubTask(String name, String description, int parentId) {
        super(name, description);
        this.parentId = parentId;
        if (this.parentId != null && this.parentId.equals(getId())) {
            this.parentId = null;
        }
    }

    public SubTask(String name, int parentId) {
        super(name);
        this.parentId = parentId;
        if (this.parentId != null && this.parentId.equals(getId())) {
            this.parentId = null;
        }
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
        if (parentId != null && parentId.equals(getId())) {
            return;
        }
        this.parentId = parentId;
    }

}