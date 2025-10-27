package ru.common.manager.task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.history.HistoryManager;
import ru.common.manager.history.InMemoryHistoryManager;
import ru.common.model.task.Task;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class HistoryManagerComprehensiveTest {
    private HistoryManager historyManager;
    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }
    @Test
    void getHistory_emptyHistory_shouldReturnEmptyList() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
    @Test
    void removeById_emptyHistory_shouldNotThrowException() {
        assertDoesNotThrow(() -> {
            historyManager.removeById(999);
        });
    }
    @Test
    void add_sameTaskMultipleTimes_shouldNotDuplicate() {
        Task task = new Task("Test Task", "Description", null, null);
        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertSame(task, history.get(0));
    }
    @Test
    void add_sameTaskMultipleTimes_shouldMoveToEnd() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
        assertEquals(task1.getId(), history.get(2).getId());
    }
    @Test
    void add_sameTaskTwice_shouldMoveToEnd() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task1.getId(), history.get(1).getId());
    }
    @Test
    void removeById_fromBeginning_shouldRemoveCorrectly() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.removeById(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }
    @Test
    void removeById_fromMiddle_shouldRemoveCorrectly() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.removeById(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }
    @Test
    void removeById_fromEnd_shouldRemoveCorrectly() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.removeById(task3.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }
    @Test
    void removeById_singleTask_shouldMakeHistoryEmpty() {
        Task task = new Task("Test Task", "Description", null, null);
        historyManager.add(task);
        historyManager.removeById(task.getId());
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
    @Test
    void removeById_nonExistentId_shouldNotThrowException() {
        Task task = new Task("Test Task", "Description", null, null);
        historyManager.add(task);
        assertDoesNotThrow(() -> {
            historyManager.removeById(999);
        });
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }
    @Test
    void add_nullTask_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            historyManager.add(null);
        });
    }
    @Test
    void add_singleTask_shouldWorkCorrectly() {
        Task task = new Task("Test Task", "Description", null, null);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertSame(task, history.get(0));
    }
    @Test
    void add_multipleTasks_shouldMaintainOrder() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
        assertEquals(task3.getId(), history.get(2).getId());
    }
    @Test
    void add_taskWithSameId_shouldReplaceExisting() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        Task task1New = new Task("Task 1 New", "New Description", null, null) {
            @Override
            public int getId() {
                return task1.getId();
            }
        };
        historyManager.add(task1New);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task1.getId(), history.get(1).getId());
        assertSame(task1New, history.get(1));
    }
    @Test
    void complexScenario_addRemoveAdd_shouldWorkCorrectly() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        Task task4 = new Task("Task 4", "Description 4", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.removeById(task2.getId());
        historyManager.add(task4);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task3.getId(), history.get(0).getId());
        assertEquals(task4.getId(), history.get(1).getId());
        assertEquals(task1.getId(), history.get(2).getId());
    }
    @Test
    void complexScenario_multipleRemovals_shouldWorkCorrectly() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        Task task4 = new Task("Task 4", "Description 4", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);
        historyManager.removeById(task1.getId());
        historyManager.removeById(task4.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
    }
    @Test
    void complexScenario_allTasksRemoved_shouldBeEmpty() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        Task task3 = new Task("Task 3", "Description 3", null, null);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.removeById(task1.getId());
        historyManager.removeById(task2.getId());
        historyManager.removeById(task3.getId());
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
    @Test
    void addManyTasks_shouldMaintainPerformance() {
        for (int i = 0; i < 1000; i++) {
            Task task = new Task("Task " + i, "Description " + i, null, null);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(1000, history.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals("Task " + i, history.get(i).getName());
        }
    }
    @Test
    void removeManyTasks_shouldMaintainPerformance() {
        List<Integer> taskIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Task task = new Task("Task " + i, "Description " + i, null, null);
            historyManager.add(task);
            taskIds.add(task.getId());
        }
        for (int i = 0; i < taskIds.size(); i += 2) {
            historyManager.removeById(taskIds.get(i));
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(50, history.size());
        List<Integer> remainingIds = history.stream()
                .map(Task::getId)
                .sorted()
                .toList();
        List<Integer> expectedIds = new ArrayList<>();
        for (int i = 1; i < taskIds.size(); i += 2) {
            expectedIds.add(taskIds.get(i));
        }
        expectedIds.sort(Integer::compareTo);
        assertEquals(expectedIds, remainingIds);
    }
}