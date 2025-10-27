package ru.common.manager.task;
import org.junit.jupiter.api.Test;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            Path tmp = Files.createTempFile("kanban-test", ".csv");
            File file = tmp.toFile();
            file.deleteOnExit();
            return (FileBackedTaskManager) Managers.getFileBackedTasksManager(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file", e);
        }
    }
    @Test
    void saveAndLoadEmptyFile() throws IOException {
        Path tmp = Files.createTempFile("kanban-empty", ".csv");
        File file = tmp.toFile();
        file.deleteOnExit();
        TaskManager manager = Managers.getFileBackedTasksManager(file);
        ((FileBackedTaskManager) manager).save();
        List<String> lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty(), "Файл должен содержать хотя бы заголовок");
        assertEquals("id,type,name,status,description,epic,startTime,duration", lines.get(0));
        assertEquals(1, lines.size(), "Пустой файл после сохранения должен содержать только заголовок");
        TaskManager loaded = Managers.getFileBackedTasksManager(file);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubTasks().isEmpty());
    }
    @Test
    void saveMultipleTasks_writesAllEntitiesToFile() throws IOException {
        Path tmp = Files.createTempFile("kanban-multi-save", ".csv");
        File file = tmp.toFile();
        file.deleteOnExit();
        TaskManager manager = Managers.getFileBackedTasksManager(file);
        EpicTask epic = new EpicTask("Epic-1", "E-desc");
        manager.createEpic(epic);
        Task t1 = new Task("Task-1", "T1-desc", null, null);
        Task t2 = new Task("Task-2", "T2-desc", null, null);
        manager.createTask(t1);
        manager.createTask(t2);
        SubTask s1 = new SubTask("Sub-1", "S1-desc", epic.getId());
        manager.createSubTask(s1);
        List<String> lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        long dataLines = lines.stream()
                .skip(1)
                .filter(l -> !l.isBlank())
                .filter(l -> !l.equals("HISTORY:"))
                .count();
        assertEquals(4, dataLines, "В файле должны быть строки для 2 Task, 1 Epic, 1 SubTask");
        String all = String.join("\n", lines);
        assertTrue(all.contains(",TASK,"), "Должны сохраняться обычные задачи");
        assertTrue(all.contains(",EPIC,"), "Должны сохраняться эпики");
        assertTrue(all.contains(",SUBTASK,"), "Должны сохраняться подзадачи");
    }
    @Test
    void loadMultipleTasks_restoresAllEntities() throws IOException {
        Path tmp = Files.createTempFile("kanban-multi-load", ".csv");
        File file = tmp.toFile();
        file.deleteOnExit();
        TaskManager writer = Managers.getFileBackedTasksManager(file);
        EpicTask epic = new EpicTask("Epic-2", "E2-desc");
        writer.createEpic(epic);
        Task t1 = new Task("Task-A", "TA-desc", null, null);
        Task t2 = new Task("Task-B", "TB-desc", null, null);
        writer.createTask(t1);
        writer.createTask(t2);
        SubTask s1 = new SubTask("Sub-X", "SX-desc", epic.getId());
        writer.createSubTask(s1);
        TaskManager reader = Managers.getFileBackedTasksManager(file);
        assertEquals(2, reader.getAllTasks().size(), "Должно загрузиться 2 обычные задачи");
        assertEquals(1, reader.getAllEpics().size(), "Должен загрузиться 1 эпик");
        assertEquals(1, reader.getAllSubTasks().size(), "Должна загрузиться 1 подзадача");
    }
    @Test
    void saveAndLoadTasksWithTimeFields() throws IOException {
        Path tmp = Files.createTempFile("kanban-time", ".csv");
        File file = tmp.toFile();
        file.deleteOnExit();
        TaskManager manager = Managers.getFileBackedTasksManager(file);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(2).plusMinutes(30);
        Task task = new Task("Test Task", "Description", startTime, duration);
        manager.createTask(task);
        EpicTask epic = new EpicTask("Test Epic", "Epic Description");
        manager.createEpic(epic);
        SubTask subTask = new SubTask("Test SubTask", "SubTask Description", epic.getId());
        subTask.setStartTime(LocalDateTime.of(2024, 1, 1, 13, 0));
        subTask.setDuration(Duration.ofHours(1));
        manager.createSubTask(subTask);
        TaskManager loaded = Managers.getFileBackedTasksManager(file);
        Task loadedTask = loaded.getTaskById(task.getId());
        assertNotNull(loadedTask);
        assertEquals(startTime, loadedTask.getStartTime());
        assertEquals(duration, loadedTask.getDuration());
        assertEquals(startTime.plus(duration), loadedTask.getEndTime());
        EpicTask loadedEpic = loaded.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), loadedEpic.getStartTime());
        assertEquals(Duration.ofHours(1), loadedEpic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 0), loadedEpic.getEndTime());
        SubTask loadedSubTask = loaded.getSubTaskById(subTask.getId());
        assertNotNull(loadedSubTask);
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), loadedSubTask.getStartTime());
        assertEquals(Duration.ofHours(1), loadedSubTask.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 0), loadedSubTask.getEndTime());
    }
    @Test
    void saveAndLoadTasksWithNullTimeFields() throws IOException {
        Path tmp = Files.createTempFile("kanban-null-time", ".csv");
        File file = tmp.toFile();
        file.deleteOnExit();
        TaskManager manager = Managers.getFileBackedTasksManager(file);
        Task task = new Task("Test Task", "Description", null, null);
        manager.createTask(task);
        TaskManager loaded = Managers.getFileBackedTasksManager(file);
        Task loadedTask = loaded.getTaskById(task.getId());
        assertNotNull(loadedTask);
        assertNull(loadedTask.getStartTime());
        assertNull(loadedTask.getDuration());
        assertNull(loadedTask.getEndTime());
    }
}