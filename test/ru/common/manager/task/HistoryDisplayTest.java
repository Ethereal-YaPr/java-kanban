package ru.common.manager.task;
import org.junit.jupiter.api.Test;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;
import static org.junit.jupiter.api.Assertions.*;
public class HistoryDisplayTest {
    @Test
    void historyDisplaysInCorrectOrderWithIds() {
        TaskManager manager = Managers.getDefault();
        Task task1 = new Task("Задача 1", null, null);
        Task task2 = new Task("Задача 2", null, null);
        EpicTask epic = new EpicTask("Эпик");
        SubTask subtask = new SubTask("Подзадача", epic.getId());
        manager.createTask(task1);
        manager.createEpic(epic);
        manager.createTask(task2);
        manager.createSubTask(subtask);
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic.getId());
        manager.getTaskById(task2.getId());
        manager.getSubTaskById(subtask.getId());
        String historyString = manager.getHistoryAsString();
        System.out.println(historyString);
        assertTrue(historyString.contains("История просмотров"));
        assertTrue(historyString.contains("ID: " + task1.getId()));
        assertTrue(historyString.contains("ID: " + epic.getId()));
        assertTrue(historyString.contains("ID: " + task2.getId()));
        assertTrue(historyString.contains("ID: " + subtask.getId()));
        assertTrue(historyString.contains("Task"));
        assertTrue(historyString.contains("EpicTask"));
        assertTrue(historyString.contains("SubTask"));
    }
}