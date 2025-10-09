package ru.common.manager.task;

import org.junit.jupiter.api.Test;
import ru.common.manager.history.HistoryManager;
import ru.common.model.task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    @Test
    void storesSameInstanceAndReflectsChanges_noDuplicates() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t = new Task("A", "D");
        history.add(t);
        t.setName("B");
        history.add(t);
        List<Task> h = history.getHistory();
        assertEquals(1, h.size());
        assertSame(t, h.get(0));
        assertEquals("B", h.get(0).getName());
    }

    @Test
    void deduplicatesAndMovesToEndOnRepeatedAdds() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t1 = new Task("T1");
        Task t2 = new Task("T2");
        history.add(t1);
        history.add(t2);
        history.add(t1); // t1 должен переместиться в конец
        List<Task> h = history.getHistory();
        assertEquals(2, h.size());
        assertEquals(t2.getId(), h.get(0).getId());
        assertEquals(t1.getId(), h.get(1).getId());
    }

    @Test
    void removeByIdRemovesFromLinkedStructure() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t1 = new Task("T1");
        Task t2 = new Task("T2");
        Task t3 = new Task("T3");
        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.removeById(t2.getId());
        List<Task> h = history.getHistory();
        assertEquals(2, h.size());
        assertEquals(t1.getId(), h.get(0).getId());
        assertEquals(t3.getId(), h.get(1).getId());
    }

    @Test
    void getHistoryAsStringContainsIdsAndOrder() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t1 = new Task("T1");
        Task t2 = new Task("T2");
        history.add(t1);
        history.add(t2);
        String s = history.getHistoryAsString();
        assertTrue(s.contains("ID: " + t1.getId()));
        assertTrue(s.contains("ID: " + t2.getId()));
        // порядок: сначала t1, потом t2
        assertTrue(s.indexOf("ID: " + t1.getId()) < s.indexOf("ID: " + t2.getId()));
    }
}


