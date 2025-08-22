package ru.common.manager.task;

import org.junit.jupiter.api.Test;
import ru.common.model.task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    @Test
    void storesSameInstancesAndReflectsChanges() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t = new Task("A", "D");
        history.add(t);
        t.setName("B");
        history.add(t);
        List<Task> h = history.getHistory();
        assertEquals(2, h.size());
        assertSame(t, h.get(0));
        assertSame(t, h.get(1));
        assertEquals("B", h.get(0).getName());
        assertEquals("B", h.get(1).getName());
    }

    @Test
    void keepsDuplicatesOnRepeatedAdds() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t = new Task("Dup");
        history.add(t);
        history.add(t);
        history.add(t);
        List<Task> h = history.getHistory();
        assertEquals(3, h.size());
        assertEquals(t.getId(), h.get(0).getId());
        assertEquals(t.getId(), h.get(1).getId());
        assertEquals(t.getId(), h.get(2).getId());
    }

    @Test
    void cappedAtTenEvictsOldest() {
        HistoryManager history = Managers.getDefaultHistory();
        Task[] tasks = new Task[12];
        for (int i = 0; i < 12; i++) {
            tasks[i] = new Task("T" + i);
            history.add(tasks[i]);
        }
        List<Task> h = history.getHistory();
        assertEquals(10, h.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(tasks[i + 2].getId(), h.get(i).getId());
        }
    }
}


