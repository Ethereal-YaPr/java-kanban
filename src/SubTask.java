class SubTask extends AbstractTask {
    private Task parentTask;

    public SubTask(String name, String description, Task parentTask) {
        super(name, description);
        this.parentTask = parentTask;
        parentTask.addSubTask(this);
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        if (parentTask != null) {
            updateParentTaskStatus();
        }
    }

    private void updateParentTaskStatus() {
        boolean allDone = true;
        boolean atLeastOneInProgress = false;

        for (SubTask subTask : parentTask.getSubTasks().values()) {
            if (subTask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (subTask.getStatus() == TaskStatus.IN_PROGRESS) {
                atLeastOneInProgress = true;
            }
        }

        if (allDone) {
            parentTask.setStatus(TaskStatus.DONE);
        } else if (atLeastOneInProgress) {
            parentTask.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }
}
