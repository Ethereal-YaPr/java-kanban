package ru.common.manager.task;

import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;

import java.util.List;

public interface TaskManager {
    EpicTask createEpic(EpicTask epic);

    List<EpicTask> getAllEpics();

    void removeAllEpics();

    EpicTask getEpicById(int id);

    boolean updateEpic(EpicTask epic);

    boolean removeEpic(EpicTask epic);

    Task createTask(Task task);

    List<Task> getAllTasks();

    void removeAllTasks();

    Task getTaskById(int id);

    SubTask getSubTaskById(int id);

    boolean updateTask(Task task);

    boolean removeTask(Task task);

    SubTask createSubTask(SubTask subTask);

    List<SubTask> getAllSubTasks();

    void removeAllSubTasks();

    List<SubTask> getSubTasksByEpicId(int epicId);

    List<Task> getHistory();
    
    String getHistoryAsString();
}
