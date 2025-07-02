import java.util.HashMap;
import java.util.Map;

class Task extends AbstractTask {
    private EpicTask epic;
    private final Map<Integer, SubTask> subTasks = new HashMap<>();

    public Task(String name, String description, EpicTask epic) {
        super(name, description);
        this.epic = epic;
        epic.addTask(this);
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        if (epic != null) {
            epic.updateStatus();
        }
    }

    public EpicTask getEpic() {
        return epic;
    }

    public void setEpic(EpicTask epic) {
        this.epic = epic;
    }

    public void addSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
    }

    public Map<Integer, SubTask> getSubTasks() {
        return new HashMap<>(subTasks);
    }

    public boolean removeSubTask(int subTaskId) {
        return subTasks.remove(subTaskId) != null;
    }
}