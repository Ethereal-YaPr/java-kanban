package ru.common.manager.task;
import org.junit.jupiter.api.Test;
import ru.common.model.task.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
    @Test
    void tasksAreEqualIfIdEqual() {
        Task t1 = new Task("A", null, null);
        Task t2 = new Task("B", null, null);
        Task t3 = new Task("C", null, null) {
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
    void epicCannotAddItselfAsSubTask() {
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
        assertInstanceOf(InMemoryTaskManager.class, manager);
    }
    @Test
    void inMemoryTaskManagerAddsAndFindsTasksById() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("T", null, null);
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
        Task t1 = new Task("T1", null, null);
        Task t2 = new Task("T2", null, null) {
            @Override
            public int getId() { return t1.getId(); }
        };
        manager.createTask(t1);
        manager.createTask(t2);
        assertEquals(t1.getId(), t2.getId());
        assertEquals(manager.getTaskById(t1.getId()), manager.getTaskById(t2.getId()));
    }
    @Test
    void managerStoresAndReturnsSameInstance() {
        TaskManager manager = Managers.getDefault();
        Task t = new Task("T", "D", null, null);
        manager.createTask(t);
        int id = t.getId();
        t.setName("Changed");
        t.setDescription("NewD");
        t.setStatus(TaskStatus.DONE);
        Task fromManager = manager.getTaskById(id);
        assertSame(t, fromManager);
        assertEquals("Changed", fromManager.getName());
        assertEquals("NewD", fromManager.getDescription());
        assertEquals(TaskStatus.DONE, fromManager.getStatus());
    }
    @Test
    void taskWithStartTimeAndDurationCalculatesEndTime() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(2).plusMinutes(30);
        Task task = new Task("Test Task", "Description", startTime, duration);
        assertEquals(startTime, task.getStartTime());
        assertEquals(duration, task.getDuration());
        assertEquals(startTime.plus(duration), task.getEndTime());
    }
    @Test
    void taskWithNullStartTimeOrDurationReturnsNullEndTime() {
        Task task1 = new Task("Test Task", "Description", null, Duration.ofHours(1));
        Task task2 = new Task("Test Task", "Description", LocalDateTime.now(), null);
        Task task3 = new Task("Test Task", "Description", null, null);
        assertNull(task1.getEndTime());
        assertNull(task2.getEndTime());
        assertNull(task3.getEndTime());
    }
    @Test
    void epicCalculatesTimeFieldsFromSubTasks() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Epic Task", "Epic Description");
        manager.createEpic(epic);
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration1 = Duration.ofHours(1);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        subTask1.setStartTime(startTime1);
        subTask1.setDuration(duration1);
        manager.createSubTask(subTask1);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 1, 11, 0);
        Duration duration2 = Duration.ofHours(2);
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask2.setStartTime(startTime2);
        subTask2.setDuration(duration2);
        manager.createSubTask(subTask2);
        assertEquals(startTime1, epic.getStartTime());
        assertEquals(duration1.plus(duration2), epic.getDuration());
        assertEquals(startTime2.plus(duration2), epic.getEndTime());
    }
    @Test
    void epicWithNoSubTasksHasNullTimeFields() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Empty Epic", "Description");
        manager.createEpic(epic);
        assertNull(epic.getStartTime());
        assertEquals(Duration.ZERO, epic.getDuration());
        assertNull(epic.getEndTime());
    }
    @Test
    void epicUpdatesTimeFieldsWhenSubTasksChange() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Epic Task", "Description");
        manager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", epic.getId());
        subTask1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subTask1.setDuration(Duration.ofHours(1));
        manager.createSubTask(subTask1);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(Duration.ofHours(1), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 11, 0), epic.getEndTime());
        SubTask subTask2 = new SubTask("SubTask 2", "Description 2", epic.getId());
        subTask2.setStartTime(LocalDateTime.of(2024, 1, 1, 8, 0));
        subTask2.setDuration(Duration.ofHours(1));
        manager.createSubTask(subTask2);
        assertEquals(LocalDateTime.of(2024, 1, 1, 8, 0), epic.getStartTime());
        assertEquals(Duration.ofHours(2), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 11, 0), epic.getEndTime());
    }
    @Test
    void epicCalculatesTimeFieldsImmediatelyAfterCreation() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Epic Task", "Epic Description");
        manager.createEpic(epic);
        assertNull(epic.getStartTime());
        assertEquals(Duration.ZERO, epic.getDuration());
        assertNull(epic.getEndTime());
        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());
        subTask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subTask.setDuration(Duration.ofHours(2));
        manager.createSubTask(subTask);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(Duration.ofHours(2), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), epic.getEndTime());
    }
    @Test
    void epicTimeFieldsRecalculatedOnUpdate() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Epic Task", "Epic Description");
        manager.createEpic(epic);
        SubTask subTask = new SubTask("SubTask", "Description", epic.getId());
        subTask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subTask.setDuration(Duration.ofHours(1));
        manager.createSubTask(subTask);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(Duration.ofHours(1), epic.getDuration());
        epic.setDescription("Updated Description");
        manager.updateEpic(epic);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(Duration.ofHours(1), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 11, 0), epic.getEndTime());
    }
    @Test
    void getPrioritizedTasksReturnsTasksSortedByStartTime() {
        TaskManager manager = Managers.getDefault();
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime time3 = LocalDateTime.of(2024, 1, 1, 12, 0);
        Task task1 = new Task("Task 1", "Description 1", time1, Duration.ofHours(1));
        Task task2 = new Task("Task 2", "Description 2", time2, Duration.ofHours(1));
        Task task3 = new Task("Task 3", "Description 3", time3, Duration.ofHours(1));
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(3, prioritizedTasks.size());
        assertEquals(task2, prioritizedTasks.get(0));
        assertEquals(task1, prioritizedTasks.get(1));
        assertEquals(task3, prioritizedTasks.get(2));
    }
    @Test
    void getPrioritizedTasksExcludesTasksWithNullStartTime() {
        TaskManager manager = Managers.getDefault();
        Task taskWithTime = new Task("Task with time", "Description",
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofHours(1));
        Task taskWithoutTime = new Task("Task without time", "Description", null, null);
        manager.createTask(taskWithTime);
        manager.createTask(taskWithoutTime);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size());
        assertEquals(taskWithTime, prioritizedTasks.get(0));
        assertFalse(prioritizedTasks.contains(taskWithoutTime));
    }
    @Test
    void getPrioritizedTasksIncludesBothTasksAndSubTasks() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Epic", "Epic Description");
        manager.createEpic(epic);
        SubTask subTask = new SubTask("SubTask", "SubTask Description", epic.getId());
        subTask.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
        subTask.setDuration(Duration.ofHours(1));
        manager.createSubTask(subTask);
        Task task = new Task("Task", "Task Description",
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofHours(2));
        manager.createTask(task);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size());
        assertEquals(subTask, prioritizedTasks.get(0));
        assertEquals(task, prioritizedTasks.get(1));
    }
    @Test
    void getPrioritizedTasksUpdatesWhenTaskTimeChanges() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task", "Description", null, null);
        manager.createTask(task);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size());
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setDuration(Duration.ofHours(1));
        manager.updateTask(task);
        prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size());
        assertEquals(task, prioritizedTasks.get(0));
    }
    @Test
    void getPrioritizedTasksRemovesTaskWhenDeleted() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task("Task", "Description",
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofHours(1));
        manager.createTask(task);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size());
        manager.removeTask(task);
        prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size());
    }
    @Test
    void getPrioritizedTasksHandlesTasksWithSameStartTime() {
        TaskManager manager = Managers.getDefault();
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Task task1 = new Task("Task 1", "Description 1", baseTime, Duration.ofHours(1));
        Task task2 = new Task("Task 2", "Description 2", baseTime.plusHours(1), Duration.ofHours(1));
        manager.createTask(task1);
        manager.createTask(task2);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size());
        assertEquals(task1, prioritizedTasks.get(0));
        assertEquals(task2, prioritizedTasks.get(1));
    }
    @Test
    void getPrioritizedTasksDisplaysCorrectOrder() {
        TaskManager manager = Managers.getDefault();
        LocalDateTime morning = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime afternoon = LocalDateTime.of(2024, 1, 1, 14, 0);
        LocalDateTime evening = LocalDateTime.of(2024, 1, 1, 18, 0);
        LocalDateTime night = LocalDateTime.of(2024, 1, 1, 22, 0);
        Task morningTask = new Task("Утренняя задача", "Описание утренней задачи", morning, Duration.ofHours(2));
        Task afternoonTask = new Task("Дневная задача", "Описание дневной задачи", afternoon, Duration.ofHours(1));
        Task eveningTask = new Task("Вечерняя задача", "Описание вечерней задачи", evening, Duration.ofHours(3));
        Task nightTask = new Task("Ночная задача", "Описание ночной задачи", night, Duration.ofHours(1));
        manager.createTask(eveningTask);
        manager.createTask(morningTask);
        manager.createTask(nightTask);
        manager.createTask(afternoonTask);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(4, prioritizedTasks.size());
        assertEquals(morningTask, prioritizedTasks.get(0));
        assertEquals(afternoonTask, prioritizedTasks.get(1));
        assertEquals(eveningTask, prioritizedTasks.get(2));
        assertEquals(nightTask, prioritizedTasks.get(3));
        for (int i = 0; i < prioritizedTasks.size() - 1; i++) {
            LocalDateTime currentTime = prioritizedTasks.get(i).getStartTime();
            LocalDateTime nextTime = prioritizedTasks.get(i + 1).getStartTime();
            assertTrue(currentTime.isBefore(nextTime) || currentTime.isEqual(nextTime),
                    "Время задачи " + i + " должно быть меньше или равно времени задачи " + (i + 1));
        }
    }
    @Test
    void getPrioritizedTasksWithMixedTaskTypes() {
        TaskManager manager = Managers.getDefault();
        EpicTask epic = new EpicTask("Большой проект", "Описание большого проекта");
        manager.createEpic(epic);
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        subTask1.setStartTime(LocalDateTime.of(2024, 1, 1, 8, 0));
        subTask1.setDuration(Duration.ofHours(1));
        manager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        subTask2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        subTask2.setDuration(Duration.ofHours(2));
        manager.createSubTask(subTask2);
        Task regularTask1 = new Task("Обычная задача 1", "Описание обычной задачи 1",
                LocalDateTime.of(2024, 1, 1, 10, 0), Duration.ofHours(1));
        Task regularTask2 = new Task("Обычная задача 2", "Описание обычной задачи 2",
                LocalDateTime.of(2024, 1, 1, 15, 0), Duration.ofHours(1));
        manager.createTask(regularTask1);
        manager.createTask(regularTask2);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(4, prioritizedTasks.size());
        assertEquals(subTask1, prioritizedTasks.get(0));
        assertEquals(regularTask1, prioritizedTasks.get(1));
        assertEquals(subTask2, prioritizedTasks.get(2));
        assertEquals(regularTask2, prioritizedTasks.get(3));
        assertFalse(prioritizedTasks.contains(epic));
    }
    @Test
    void getPrioritizedTasksHandlesEmptyList() {
        TaskManager manager = Managers.getDefault();
        Task taskWithoutTime1 = new Task("Задача без времени 1", "Описание", null, null);
        Task taskWithoutTime2 = new Task("Задача без времени 2", "Описание", null, null);
        manager.createTask(taskWithoutTime1);
        manager.createTask(taskWithoutTime2);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertTrue(prioritizedTasks.isEmpty());
    }
    @Test
    void getPrioritizedTasksShowsCorrectTimeFormat() {
        TaskManager manager = Managers.getDefault();
        LocalDateTime specificTime = LocalDateTime.of(2024, 3, 15, 14, 30);
        Task task = new Task("Задача с точным временем", "Описание", specificTime, Duration.ofMinutes(45));
        manager.createTask(task);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size());
        Task retrievedTask = prioritizedTasks.get(0);
        assertEquals(specificTime, retrievedTask.getStartTime());
        assertEquals(Duration.ofMinutes(45), retrievedTask.getDuration());
        assertEquals(specificTime.plus(Duration.ofMinutes(45)), retrievedTask.getEndTime());
        assertEquals(2024, retrievedTask.getStartTime().getYear());
        assertEquals(3, retrievedTask.getStartTime().getMonthValue());
        assertEquals(15, retrievedTask.getStartTime().getDayOfMonth());
        assertEquals(14, retrievedTask.getStartTime().getHour());
        assertEquals(30, retrievedTask.getStartTime().getMinute());
    }
    @Test
    void getPrioritizedTasksRealWorldExample() {
        TaskManager manager = Managers.getDefault();
        System.out.println("=== РЕАЛЬНЫЙ ПРИМЕР: РАБОЧИЙ ДЕНЬ ===");
        EpicTask projectEpic = new EpicTask("Разработка нового модуля", "Создание модуля авторизации");
        manager.createEpic(projectEpic);
        SubTask designTask = new SubTask("Дизайн интерфейса", "Создать макеты страниц", projectEpic.getId());
        designTask.setStartTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        designTask.setDuration(Duration.ofHours(2));
        manager.createSubTask(designTask);
        SubTask codingTask = new SubTask("Написание кода", "Реализовать логику авторизации", projectEpic.getId());
        codingTask.setStartTime(LocalDateTime.of(2024, 1, 15, 11, 0));
        codingTask.setDuration(Duration.ofHours(4));
        manager.createSubTask(codingTask);
        SubTask testingTask = new SubTask("Тестирование", "Провести unit-тесты", projectEpic.getId());
        testingTask.setStartTime(LocalDateTime.of(2024, 1, 15, 16, 0));
        testingTask.setDuration(Duration.ofHours(1));
        manager.createSubTask(testingTask);
        Task meetingTask = new Task("Планерка команды", "Ежедневная встреча команды",
                LocalDateTime.of(2024, 1, 15, 8, 30), Duration.ofMinutes(30));
        manager.createTask(meetingTask);
        Task codeReviewTask = new Task("Code Review", "Проверка кода коллеги",
                LocalDateTime.of(2024, 1, 15, 15, 0), Duration.ofMinutes(45));
        manager.createTask(codeReviewTask);
        Task documentationTask = new Task("Документация", "Написание технической документации",
                LocalDateTime.of(2024, 1, 15, 17, 0), Duration.ofHours(1));
        manager.createTask(documentationTask);
        Task flexibleTask = new Task("Гибкая задача", "Можно выполнить в любое время", null, null);
        manager.createTask(flexibleTask);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(6, prioritizedTasks.size());
        assertEquals(meetingTask, prioritizedTasks.get(0));
        assertEquals(designTask, prioritizedTasks.get(1));
        assertEquals(codingTask, prioritizedTasks.get(2));
        assertEquals(codeReviewTask, prioritizedTasks.get(3));
        assertEquals(testingTask, prioritizedTasks.get(4));
        assertEquals(documentationTask, prioritizedTasks.get(5));
        assertFalse(prioritizedTasks.contains(flexibleTask));
        assertFalse(prioritizedTasks.contains(projectEpic));
        System.out.println("Приоритетный список задач на день:");
        for (int i = 0; i < prioritizedTasks.size(); i++) {
            Task task = prioritizedTasks.get(i);
            String taskType = task instanceof SubTask ? "[Подзадача]" : "[Задача]";
            System.out.printf("%d. %s %s - %s\n",
                i + 1,
                taskType,
                task.getName(),
                task.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
        }
        System.out.println("\nВсего задач в приоритетном списке: " + prioritizedTasks.size());
        System.out.println("Задач без времени (исключены): 1");
    }
}