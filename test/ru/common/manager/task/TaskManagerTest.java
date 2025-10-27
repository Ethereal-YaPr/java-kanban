package ru.common.manager.task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.model.task.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected abstract T createTaskManager();
    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }
    @Test
    void createTask_shouldCreateTaskWithCorrectFields() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(60);
        Task task = new Task("Test Task", "Description", startTime, duration);
        Task createdTask = taskManager.createTask(task);
        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getName());
        assertEquals("Description", createdTask.getDescription());
        assertEquals(TaskStatus.NEW, createdTask.getStatus());
        assertEquals(startTime, createdTask.getStartTime());
        assertEquals(duration, createdTask.getDuration());
        assertEquals(startTime.plus(duration), createdTask.getEndTime());
    }
    @Test
    void createTask_withNullStartTimeAndDuration_shouldCreateTask() {
        Task task = new Task("Test Task", "Description", null, null);
        Task createdTask = taskManager.createTask(task);
        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getName());
        assertNull(createdTask.getStartTime());
        assertNull(createdTask.getDuration());
        assertNull(createdTask.getEndTime());
    }
    @Test
    void createTask_withSubTask_shouldThrowException() {
        SubTask subTask = new SubTask("SubTask", 1);
        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createTask(subTask);
        });
    }
    @Test
    void updateTask_shouldUpdateTaskFields() {
        Task task = new Task("Original", "Original Description", null, null);
        taskManager.createTask(task);
        task.setName("Updated");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(90);
        task.setStartTime(startTime);
        task.setDuration(duration);
        boolean updateResult = taskManager.updateTask(task);
        assertTrue(updateResult);
        assertEquals("Updated", task.getName());
        assertEquals("Updated Description", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(startTime, task.getStartTime());
        assertEquals(duration, task.getDuration());
        assertEquals(startTime.plus(duration), task.getEndTime());
    }
    @Test
    void getTaskById_shouldReturnCorrectTask() {
        Task task = new Task("Test Task", "Description", null, null);
        Task createdTask = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTaskById(createdTask.getId());
        assertNotNull(retrievedTask);
        assertEquals(createdTask.getId(), retrievedTask.getId());
        assertEquals("Test Task", retrievedTask.getName());
    }
    @Test
    void getTaskById_withNonExistentId_shouldReturnNull() {
        Task task = taskManager.getTaskById(999);
        assertNull(task);
    }
    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        List<Task> allTasks = taskManager.getAllTasks();
        assertEquals(2, allTasks.size());
        assertTrue(allTasks.contains(task1));
        assertTrue(allTasks.contains(task2));
    }
    @Test
    void removeTask_shouldRemoveTask() {
        Task task = new Task("Test Task", "Description", null, null);
        Task createdTask = taskManager.createTask(task);
        boolean removeResult = taskManager.removeTask(createdTask);
        assertTrue(removeResult);
        assertNull(taskManager.getTaskById(createdTask.getId()));
        assertTrue(taskManager.getAllTasks().isEmpty());
    }
    @Test
    void removeAllTasks_shouldRemoveAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.removeAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }
    @Test
    void createEpic_shouldCreateEpicWithCorrectFields() {
        EpicTask epic = new EpicTask("Test Epic");
        EpicTask createdEpic = taskManager.createEpic(epic);
        assertNotNull(createdEpic);
        assertEquals("Test Epic", createdEpic.getName());
        assertEquals(TaskStatus.NEW, createdEpic.getStatus());
        assertTrue(createdEpic.getSubTaskIds().isEmpty());
    }
    @Test
    void updateEpic_shouldUpdateEpicFields() {
        EpicTask epic = new EpicTask("Original Epic");
        taskManager.createEpic(epic);
        epic.setName("Updated Epic");
        epic.setDescription("Updated Description");
        boolean updateResult = taskManager.updateEpic(epic);
        assertTrue(updateResult);
        assertEquals("Updated Epic", epic.getName());
        assertEquals("Updated Description", epic.getDescription());
    }
    @Test
    void getEpicById_shouldReturnCorrectEpic() {
        EpicTask epic = new EpicTask("Test Epic");
        EpicTask createdEpic = taskManager.createEpic(epic);
        EpicTask retrievedEpic = taskManager.getEpicById(createdEpic.getId());
        assertNotNull(retrievedEpic);
        assertEquals(createdEpic.getId(), retrievedEpic.getId());
        assertEquals("Test Epic", retrievedEpic.getName());
    }
    @Test
    void getAllEpics_shouldReturnAllEpics() {
        EpicTask epic1 = new EpicTask("Epic 1");
        EpicTask epic2 = new EpicTask("Epic 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        List<EpicTask> allEpics = taskManager.getAllEpics();
        assertEquals(2, allEpics.size());
        assertTrue(allEpics.contains(epic1));
        assertTrue(allEpics.contains(epic2));
    }
    @Test
    void removeEpic_shouldRemoveEpic() {
        EpicTask epic = new EpicTask("Test Epic");
        EpicTask createdEpic = taskManager.createEpic(epic);
        boolean removeResult = taskManager.removeEpic(createdEpic);
        assertTrue(removeResult);
        assertNull(taskManager.getEpicById(createdEpic.getId()));
        assertTrue(taskManager.getAllEpics().isEmpty());
    }
    @Test
    void removeAllEpics_shouldRemoveAllEpics() {
        EpicTask epic1 = new EpicTask("Epic 1");
        EpicTask epic2 = new EpicTask("Epic 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.removeAllEpics();
        assertTrue(taskManager.getAllEpics().isEmpty());
    }
    @Test
    void createSubTask_shouldCreateSubTaskWithCorrectFields() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        SubTask subTask = new SubTask("Test SubTask", epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subTask);
        assertNotNull(createdSubTask);
        assertEquals("Test SubTask", createdSubTask.getName());
        assertEquals(epic.getId(), createdSubTask.getParentId());
        assertEquals(TaskStatus.NEW, createdSubTask.getStatus());
        assertNull(createdSubTask.getStartTime());
        assertNull(createdSubTask.getDuration());
        assertNull(createdSubTask.getEndTime());
    }
    @Test
    void createSubTask_withNonExistentEpic_shouldThrowException() {
        SubTask subTask = new SubTask("Test SubTask", 999);
        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubTask(subTask);
        });
    }
    @Test
    void updateSubTask_shouldUpdateSubTaskFields() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Original SubTask", epic.getId());
        taskManager.createSubTask(subTask);
        subTask.setName("Updated SubTask");
        subTask.setDescription("Updated Description");
        subTask.setStatus(TaskStatus.IN_PROGRESS);
        boolean updateResult = taskManager.updateSubTask(subTask);
        assertTrue(updateResult);
        assertEquals("Updated SubTask", subTask.getName());
        assertEquals("Updated Description", subTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, subTask.getStatus());
    }
    @Test
    void getSubTaskById_shouldReturnCorrectSubTask() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Test SubTask", epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subTask);
        SubTask retrievedSubTask = taskManager.getSubTaskById(createdSubTask.getId());
        assertNotNull(retrievedSubTask);
        assertEquals(createdSubTask.getId(), retrievedSubTask.getId());
        assertEquals("Test SubTask", retrievedSubTask.getName());
        assertEquals(epic.getId(), retrievedSubTask.getParentId());
    }
    @Test
    void getAllSubTasks_shouldReturnAllSubTasks() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        List<SubTask> allSubTasks = taskManager.getAllSubTasks();
        assertEquals(2, allSubTasks.size());
        assertTrue(allSubTasks.contains(subTask1));
        assertTrue(allSubTasks.contains(subTask2));
    }
    @Test
    void removeSubTask_shouldRemoveSubTask() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Test SubTask", epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subTask);
        taskManager.removeSubTask(createdSubTask.getId());
        assertNull(taskManager.getSubTaskById(createdSubTask.getId()));
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }
    @Test
    void removeAllSubTasks_shouldRemoveAllSubTasks() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.removeAllSubTasks();
        assertTrue(taskManager.getAllSubTasks().isEmpty());
    }
    @Test
    void getSubTasksByEpicId_shouldReturnCorrectSubTasks() {
        EpicTask epic = new EpicTask("Parent Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        List<SubTask> epicSubTasks = taskManager.getSubTasksByEpicId(epic.getId());
        assertEquals(2, epicSubTasks.size());
        assertTrue(epicSubTasks.contains(subTask1));
        assertTrue(epicSubTasks.contains(subTask2));
    }
    @Test
    void getSubTasksByEpicId_withNonExistentEpic_shouldReturnEmptyList() {
        List<SubTask> epicSubTasks = taskManager.getSubTasksByEpicId(999);
        assertTrue(epicSubTasks.isEmpty());
    }
    @Test
    void getHistory_shouldReturnEmptyHistoryInitially() {
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }
    @Test
    void getHistory_shouldTrackViewedTasks() {
        Task task = new Task("Test Task", "Description", null, null);
        EpicTask epic = new EpicTask("Test Epic");
        SubTask subTask = new SubTask("Test SubTask", epic.getId());
        taskManager.createTask(task);
        taskManager.createEpic(epic);
        taskManager.createSubTask(subTask);
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubTaskById(subTask.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subTask.getId(), history.get(2).getId());
    }
    @Test
    void getHistory_shouldNotDuplicateTasks() {
        Task task = new Task("Test Task", "Description", null, null);
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }
    @Test
    void getHistory_shouldMoveTaskToEndOnRepeatedView() {
        Task task1 = new Task("Task 1", "Description 1", null, null);
        Task task2 = new Task("Task 2", "Description 2", null, null);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task1.getId(), history.get(1).getId());
    }
    @Test
    void getPrioritizedTasks_shouldReturnEmptyListInitially() {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertTrue(prioritizedTasks.isEmpty());
    }
    @Test
    void getPrioritizedTasks_shouldExcludeTasksWithNullStartTime() {
        Task taskWithTime = new Task("Task with time", "Description",
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofMinutes(60));
        Task taskWithoutTime = new Task("Task without time", "Description", null, null);
        taskManager.createTask(taskWithTime);
        taskManager.createTask(taskWithoutTime);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size());
        assertEquals(taskWithTime.getId(), prioritizedTasks.get(0).getId());
    }
    @Test
    void getPrioritizedTasks_shouldSortTasksByStartTime() {
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime time3 = LocalDateTime.of(2024, 1, 1, 11, 0);
        Task task1 = new Task("Task 1", "Description", time1, Duration.ofMinutes(60));
        Task task2 = new Task("Task 2", "Description", time2, Duration.ofMinutes(60));
        Task task3 = new Task("Task 3", "Description", time3, Duration.ofMinutes(60));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.size());
        assertEquals(task2.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task3.getId(), prioritizedTasks.get(1).getId());
        assertEquals(task1.getId(), prioritizedTasks.get(2).getId());
    }
}