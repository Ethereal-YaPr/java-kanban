import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final Map<Integer, EpicTask> epics = new HashMap<>();
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();

    // Эпики
    public EpicTask createEpic(EpicTask epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public List<EpicTask> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeAllEpics() {
        epics.clear();
    }

    public EpicTask getEpicById(int id) {
        return epics.get(id);
    }

    public boolean updateEpic(EpicTask epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return false;
        }
        epics.put(epic.getId(), epic);
        return true;
    }

    public boolean removeEpic(EpicTask epic) {
        if (epic == null) return false;
        new ArrayList<>(epic.getTasks().values()).forEach(this::removeTask);
        return epics.remove(epic.getId()) != null;
    }

    public boolean removeEpicById(int id) {
        EpicTask epic = getEpicById(id);
        return removeEpic(epic);
    }

    public Task createTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public boolean updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return false;
        }
        tasks.put(task.getId(), task);
        return true;
    }

    public boolean removeTask(Task task) {
        if (task == null) return false;
        new ArrayList<>(task.getSubTasks().values()).forEach(this::removeSubTask);
        if (task.getEpic() != null) {
            task.getEpic().removeTask(task.getId());
        }
        return tasks.remove(task.getId()) != null;
    }

    public boolean removeTaskById(int id) {
        Task task = getTaskById(id);
        return removeTask(task);
    }

    public SubTask createSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        return subTask;
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void removeAllSubTasks() {
        subTasks.clear();
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public boolean updateSubTask(SubTask subTask) {
        if (subTask == null || !subTasks.containsKey(subTask.getId())) {
            return false;
        }
        subTasks.put(subTask.getId(), subTask);
        return true;
    }

    public boolean removeSubTask(SubTask subTask) {
        if (subTask == null) return false;
        if (subTask.getParentTask() != null) {
            subTask.getParentTask().removeSubTask(subTask.getId());
        }
        return subTasks.remove(subTask.getId()) != null;
    }

    public boolean removeSubTaskById(int id) {
        SubTask subTask = getSubTaskById(id);
        return removeSubTask(subTask);
    }

    public List<SubTask> getSubTasksByEpicId(int epicId) {
        List<SubTask> result = new ArrayList<>();
        EpicTask epic = getEpicById(epicId);
        if (epic != null) {
            for (SubTask subTask : subTasks.values()) {
                if (subTask.getParentTask().getEpic().getId() == epicId) {
                    result.add(subTask);
                }
            }
        }
        return result;
    }
}
