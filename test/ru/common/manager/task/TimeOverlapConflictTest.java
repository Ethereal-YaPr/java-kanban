package ru.common.manager.task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.model.task.*;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
public class TimeOverlapConflictTest {
    private InMemoryTaskManager taskManager;
    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }
    @Test
    void createTask_withOverlappingTime_shouldThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(30), duration);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
        assertTrue(exception.getMessage().contains("Task 1"));
        assertTrue(exception.getMessage().contains("Task 2"));
    }
    @Test
    void createTask_withNonOverlappingTime_shouldSucceed() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(90), duration);
        assertDoesNotThrow(() -> {
            taskManager.createTask(task2);
        });
        assertEquals(2, taskManager.getAllTasks().size());
    }
    @Test
    void createTask_withAdjacentTime_shouldSucceed() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(60), duration);
        assertDoesNotThrow(() -> {
            taskManager.createTask(task2);
        });
        assertEquals(2, taskManager.getAllTasks().size());
    }
    @Test
    void createSubTask_withOverlappingTime_shouldThrowException() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        subTask1.setStartTime(baseTime);
        subTask1.setDuration(duration);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        subTask2.setStartTime(baseTime.plusMinutes(30));
        subTask2.setDuration(duration);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubTask(subTask2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
        assertTrue(exception.getMessage().contains("SubTask 1"));
        assertTrue(exception.getMessage().contains("SubTask 2"));
    }
    @Test
    void createSubTask_withNonOverlappingTime_shouldSucceed() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        subTask1.setStartTime(baseTime);
        subTask1.setDuration(duration);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        subTask2.setStartTime(baseTime.plusMinutes(90));
        subTask2.setDuration(duration);
        assertDoesNotThrow(() -> {
            taskManager.createSubTask(subTask2);
        });
        assertEquals(2, taskManager.getAllSubTasks().size());
    }
    @Test
    void createTask_withNullTime_shouldNotCheckOverlap() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description", null, null);
        assertDoesNotThrow(() -> {
            taskManager.createTask(task2);
        });
        assertEquals(2, taskManager.getAllTasks().size());
    }
    @Test
    void updateTask_withOverlappingTime_shouldThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(90), duration);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        task2.setStartTime(baseTime.plusMinutes(30));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.updateTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
        assertTrue(exception.getMessage().contains("Task 1"));
        assertTrue(exception.getMessage().contains("Task 2"));
    }
    @Test
    void updateTask_withSameTask_shouldNotThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task = new Task("Task", "Description", baseTime, duration);
        taskManager.createTask(task);
        task.setDescription("Updated Description");
        assertDoesNotThrow(() -> {
            taskManager.updateTask(task);
        });
        assertEquals("Updated Description", taskManager.getTaskById(task.getId()).getDescription());
    }
    @Test
    void updateTask_withNonOverlappingTime_shouldSucceed() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(90), duration);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        task2.setStartTime(baseTime.plusMinutes(120));
        assertDoesNotThrow(() -> {
            taskManager.updateTask(task2);
        });
        assertEquals(baseTime.plusMinutes(120),
                taskManager.getTaskById(task2.getId()).getStartTime());
    }
    @Test
    void updateSubTask_withOverlappingTime_shouldThrowException() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        subTask1.setStartTime(baseTime);
        subTask1.setDuration(duration);
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        subTask2.setStartTime(baseTime.plusMinutes(90));
        subTask2.setDuration(duration);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        subTask2.setStartTime(baseTime.plusMinutes(30));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.updateSubTask(subTask2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
        assertTrue(exception.getMessage().contains("SubTask 1"));
        assertTrue(exception.getMessage().contains("SubTask 2"));
    }
    @Test
    void createTask_withExactSameTime_shouldThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description", baseTime, duration);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
    }
    @Test
    void createTask_withPartialOverlap_shouldThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.minusMinutes(30), Duration.ofMinutes(90));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
    }
    @Test
    void createTask_withContainedTime_shouldThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(15), Duration.ofMinutes(30));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
    }
    @Test
    void createTask_withContainingTime_shouldThrowException() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.minusMinutes(30), Duration.ofMinutes(120));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(task2);
        });
        assertTrue(exception.getMessage().contains("пересекается по времени"));
    }
    @Test
    void createTask_withZeroDuration_shouldNotOverlap() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task1 = new Task("Task 1", "Description", baseTime, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description",
                baseTime.plusMinutes(30), Duration.ZERO);
        assertDoesNotThrow(() -> {
            taskManager.createTask(task2);
        });
        assertEquals(2, taskManager.getAllTasks().size());
    }
}