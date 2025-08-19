package ru.common.manager.task;

import org.junit.jupiter.api.Test;
import ru.common.model.task.*;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    @Test
    void tasksAreEqualIfIdEqual() {
        Task t1 = new Task("A");
        Task t2 = new Task("B");
        Task t3 = new Task("C") {
            @Override
            public int getId() { return t1.getId(); }
        };
        assertEquals(t1.getId(), t3.getId());
    }

    @Test
    void descendantsAreEqualIfIdEqual() {
        EpicTask e1 = new EpicTask("Epic");
        EpicTask e2 = new EpicTask("Epic2") {
            @Override
            public int getId() { return e1.getId(); }
        };
        assertEquals(e1.getId(), e2.getId());
        SubTask s1 = new SubTask("Sub", 1);
        SubTask s2 = new SubTask("Sub2", 2) {
            @Override
            public int getId() { return s1.getId(); }
        };
        assertEquals(s1.getId(), s2.getId());
    }

    @Test
    void epicCannotAddItselfAsSubtask() {
        EpicTask epic = new EpicTask("Epic");
        epic.addSubTaskId(epic.getId());
        assertFalse(epic.getSubTaskIds().contains(epic.getId()));
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        SubTask sub = new SubTask("Sub", 1);
        sub.setParentId(sub.getId());
        assertNotEquals(sub.getId(), sub.getParentId());
    }

    @Test
    void managersGetDefaultReturnsReadyManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
        assertTrue(manager instanceof InMemoryTaskManager);
    }

    @Test
    void inMemoryTaskManagerAddsAndFindsTasksById() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("T");
        EpicTask e = new EpicTask("E");
        SubTask s = new SubTask("S", e.getId());
        manager.createTask(t);
        manager.createEpic(e);
        manager.createSubTask(s);
        assertEquals(t, manager.getTaskById(t.getId()));
        assertEquals(e, manager.getEpicById(e.getId()));
        assertEquals(s, manager.getSubTaskById(s.getId()));
    }

    @Test
    void tasksWithManualAndGeneratedIdDoNotConflict() {
        TaskManager manager = Managers.getDefault();
        Task t1 = new Task("T1");
        Task t2 = new Task("T2") {
            @Override
            public int getId() { return t1.getId(); }
        };
        manager.createTask(t1);
        manager.createTask(t2);
        assertEquals(t1.getId(), t2.getId());
        assertEquals(manager.getTaskById(t1.getId()), manager.getTaskById(t2.getId()));
    }

    @Test
    void taskIsImmutableAfterAddingToManager() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("T", "D");
        manager.createTask(t);
        int idBefore = t.getId();
        String nameBefore = t.getName();
        String descBefore = t.getDescription();
        TaskStatus statusBefore = t.getStatus();

        t.setName("Changed");
        t.setDescription("NewD");
        t.setStatus(TaskStatus.DONE);

        Task fromManager = manager.getTaskById(idBefore);
        assertEquals(nameBefore, fromManager.getName());
        assertEquals(descBefore, fromManager.getDescription());
        assertEquals(statusBefore, fromManager.getStatus());
    }

    @Test
    void historyManagerSavesSnapshotsPerView() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("T", "D");
        manager.createTask(t);
        
        manager.getTaskById(t.getId());

        t.setName("Changed");
        t.setDescription("D2");
        t.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(t);
        manager.getTaskById(t.getId());

        var history = manager.getHistory();
        assertTrue(history.size() >= 2);
        assertEquals("T", history.get(history.size()-2).getName());
        assertEquals("Changed", history.get(history.size()-1).getName());
    }

    @Test
    void historyKeepsDuplicatesOnRepeatedViews() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("Dup");
        manager.createTask(t);

        manager.getTaskById(t.getId());
        manager.getTaskById(t.getId());
        manager.getTaskById(t.getId());

        var history = manager.getHistory();
        assertEquals(3, history.size());
        assertEquals(t.getId(), history.get(0).getId());
        assertEquals(t.getId(), history.get(1).getId());
        assertEquals(t.getId(), history.get(2).getId());
    }

    @Test
    void historyIsCappedAtTenAndEvictsOldest() {
        TaskManager manager = Managers.getDefault();
        Task[] tasks = new Task[12];
        for (int i = 0; i < 12; i++) {
            tasks[i] = new Task("T" + i);
            manager.createTask(tasks[i]);
        }

        for (int i = 0; i < 12; i++) {
            manager.getTaskById(tasks[i].getId());
        }

        var history = manager.getHistory();
        assertEquals(10, history.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(tasks[i + 2].getId(), history.get(i).getId());
        }
    }
}
