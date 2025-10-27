package ru.common.model.task;

public class SubTask extends Task {
    private Integer parentId;

    SubTask(int id, String name, String description, TaskStatus status, Integer parentId) {
        super(id, name, description, status, null, null);
        this.parentId = parentId;
    }

    public SubTask(String name, String description, int parentId) {
        super(name, description, null, null);
        this.parentId = parentId;
        if (this.parentId != null && this.parentId.equals(getId())) {
            this.parentId = null;
        }
    }

    public SubTask(String name, int parentId) {
        super(name, null, null);
        this.parentId = parentId;
        if (this.parentId != null && this.parentId.equals(getId())) {
            this.parentId = null;
        }
    }

    public SubTask(String name) {
        super(name, null, null);
    }

    public SubTask(String name, String description) {
        super(name, description, null, null);
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

    @Override
    public String toCSVString() {
        Integer epicId = parentId;
        String startTimeStr = (getStartTime() != null) ? getStartTime().toString() : "";
        String durationStr = (getDuration() != null) ? String.valueOf(getDuration().toMinutes()) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                this.getId(),
                TaskType.SUBTASK,
                this.getName(),
                this.getStatus(),
                this.getDescription(),
                epicId == null ? "" : epicId.toString(),
                startTimeStr,
                durationStr);
    }
}