import java.util.HashMap;
import java.util.Map;

class EpicTask extends AbstractTask {
    private final Map<Integer, Task> tasks = new HashMap<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        updateStatus();
    }

    public boolean removeTask(int taskId) {
        if (tasks.remove(taskId) != null) {
            updateStatus();
            return true;
        }
        return false;
    }

    void updateStatus() {
        if (tasks.isEmpty()) {
            super.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Task task : tasks.values()) {
            if (task.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (task.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                super.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }

        if (allDone) {
            super.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            super.setStatus(TaskStatus.NEW);
        } else {
            super.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public Map<Integer, Task> getTasks() {
        return new HashMap<>(tasks);
    }
}
