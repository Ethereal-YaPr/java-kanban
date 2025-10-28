package ru.common.manager.task;
import org.junit.jupiter.api.Test;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;
import static org.junit.jupiter.api.Assertions.*;
public class TaskManagerHistoryDeletionTest {
    @Test
    void removeTaskRemovesFromHistory() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("T", null, null);
        manager.createTask(t);
        manager.getTaskById(t.getId());
        assertFalse(manager.getHistory().isEmpty());
        manager.removeTask(t);
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == t.getId()));
    }
    @Test
    void removeEpicRemovesEpicAndSubTaskFromHistory() {
        TaskManager manager = Managers.getDefault();
        EpicTask e = new EpicTask("E");
        manager.createEpic(e);
        SubTask s = new SubTask("S", e.getId());
        manager.createSubTask(s);
        manager.getEpicById(e.getId());
        manager.getSubTaskById(s.getId());
        manager.removeEpic(e);
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == e.getId()));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == s.getId()));
    }
    @Test
    void removeAllTasksRemovesOnlyTasksFromHistory() {
        TaskManager manager = Managers.getDefault();
        EpicTask e = new EpicTask("E");
        manager.createEpic(e);
        SubTask s = new SubTask("S", e.getId());
        manager.createSubTask(s);
        Task t1 = new Task("T1", null, null);
        Task t2 = new Task("T2", null, null);
        manager.createTask(t1);
        manager.createTask(t2);
        manager.getTaskById(t1.getId());
        manager.getTaskById(t2.getId());
        manager.getSubTaskById(s.getId());
        manager.removeAllTasks();
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == t1.getId()));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == t2.getId()));
        assertTrue(manager.getHistory().stream().anyMatch(x -> x.getId() == s.getId()));
    }
    @Test
    void removeAllSubTasksRemovesSubTasksFromHistory() {
        TaskManager manager = Managers.getDefault();
        EpicTask e = new EpicTask("E");
        manager.createEpic(e);
        SubTask s1 = new SubTask("S1", e.getId());
        SubTask s2 = new SubTask("S2", e.getId());
        manager.createSubTask(s1);
        manager.createSubTask(s2);
        manager.getSubTaskById(s1.getId());
        manager.getSubTaskById(s2.getId());
        manager.removeAllSubTasks();
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == s1.getId()));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == s2.getId()));
    }
    @Test
    void removeAllEpicsRemovesEpicsAndSubTasksFromHistory() {
        TaskManager manager = Managers.getDefault();
        EpicTask e1 = new EpicTask("E1");
        EpicTask e2 = new EpicTask("E2");
        manager.createEpic(e1);
        manager.createEpic(e2);
        SubTask s1 = new SubTask("S1", e1.getId());
        SubTask s2 = new SubTask("S2", e2.getId());
        manager.createSubTask(s1);
        manager.createSubTask(s2);
        manager.getEpicById(e1.getId());
        manager.getSubTaskById(s1.getId());
        manager.getEpicById(e2.getId());
        manager.getSubTaskById(s2.getId());
        manager.removeAllEpics();
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == e1.getId()));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == e2.getId()));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == s1.getId()));
        assertTrue(manager.getHistory().stream().noneMatch(x -> x.getId() == s2.getId()));
    }
}