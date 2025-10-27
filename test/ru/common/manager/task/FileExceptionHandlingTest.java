package ru.common.manager.task;
import org.junit.jupiter.api.Test;
import ru.common.manager.exceptions.ManagerSaveException;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.Task;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
public class FileExceptionHandlingTest {
    @Test
    void createFileBackedTaskManager_withNonExistentFile_shouldCreateFile() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path nonExistentFile = tempDir.resolve("non-existent-kanban.csv");
        try {
            Files.deleteIfExists(nonExistentFile);
        } catch (IOException e) {
        }
        assertDoesNotThrow(() -> {
            TaskManager manager = Managers.getFileBackedTasksManager(nonExistentFile.toFile());
            assertNotNull(manager);
        });
        try {
            Files.deleteIfExists(nonExistentFile);
        } catch (IOException e) {
        }
    }
    @Test
    void createFileBackedTaskManager_withReadOnlyFile_shouldThrowException() {
        try {
            Path tempFile = Files.createTempFile("readonly-kanban", ".csv");
            File file = tempFile.toFile();
            file.setReadOnly();
            assertThrows(ManagerSaveException.class, () -> {
                TaskManager manager = Managers.getFileBackedTasksManager(file);
                Task task = new Task("Test Task", "Description", null, null);
                manager.createTask(task);
            });
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void createFileBackedTaskManager_withDirectoryInsteadOfFile_shouldThrowException() {
        try {
            Path tempDir = Files.createTempDirectory("kanban-dir");
            File dir = tempDir.toFile();
            assertThrows(ManagerSaveException.class, () -> {
                TaskManager manager = Managers.getFileBackedTasksManager(dir);
                Task task = new Task("Test Task", "Description", null, null);
                manager.createTask(task);
            });
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            fail("Не удалось создать временную директорию: " + e.getMessage());
        }
    }
    @Test
    void save_withInvalidFilePath_shouldThrowException() {
        Path invalidPath = Paths.get("/non/existent/directory/kanban.csv");
        File invalidFile = invalidPath.toFile();
        assertThrows(ManagerSaveException.class, () -> {
            TaskManager manager = Managers.getFileBackedTasksManager(invalidFile);
            Task task = new Task("Test Task", "Description", null, null);
            manager.createTask(task);
        });
    }
    @Test
    void save_withCorruptedFile_shouldHandleGracefully() {
        try {
            Path tempFile = Files.createTempFile("corrupted-kanban", ".csv");
            File file = tempFile.toFile();
            Files.write(tempFile, "corrupted,data,here\n".getBytes());
            assertDoesNotThrow(() -> {
                TaskManager manager = Managers.getFileBackedTasksManager(file);
                assertNotNull(manager);
                assertTrue(manager.getAllTasks().isEmpty());
                assertTrue(manager.getAllEpics().isEmpty());
                assertTrue(manager.getAllSubTasks().isEmpty());
            });
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withEmptyFile_shouldWorkCorrectly() {
        try {
            Path tempFile = Files.createTempFile("empty-kanban", ".csv");
            File file = tempFile.toFile();
            Files.write(tempFile, new byte[0]);
            assertDoesNotThrow(() -> {
                TaskManager manager = Managers.getFileBackedTasksManager(file);
                assertNotNull(manager);
                assertTrue(manager.getAllTasks().isEmpty());
                assertTrue(manager.getAllEpics().isEmpty());
                assertTrue(manager.getAllSubTasks().isEmpty());
            });
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withFileWithoutHeader_shouldWorkCorrectly() {
        try {
            Path tempFile = Files.createTempFile("no-header-kanban", ".csv");
            File file = tempFile.toFile();
            Files.write(tempFile, "1,TASK,Test Task,NEW,Description,,,\n".getBytes());
            assertDoesNotThrow(() -> {
                TaskManager manager = Managers.getFileBackedTasksManager(file);
                assertNotNull(manager);
                assertTrue(manager.getAllTasks().isEmpty());
                assertTrue(manager.getAllEpics().isEmpty());
                assertTrue(manager.getAllSubTasks().isEmpty());
            });
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withInvalidCSVFormat_shouldHandleGracefully() {
        try {
            Path tempFile = Files.createTempFile("invalid-csv-kanban", ".csv");
            File file = tempFile.toFile();
            String invalidCSV = "id,type,name,status,description,epic,startTime,duration\n" +
                               "1,TASK,Test Task,NEW,Description,,,\n" +
                               "invalid,line,without,enough,columns\n" +
                               "2,EPIC,Test Epic,NEW,Epic Description,,,\n";
            Files.write(tempFile, invalidCSV.getBytes());
            assertDoesNotThrow(() -> {
                TaskManager manager = Managers.getFileBackedTasksManager(file);
                assertNotNull(manager);
                assertEquals(1, manager.getAllTasks().size());
                assertEquals(1, manager.getAllEpics().size());
                assertEquals(0, manager.getAllSubTasks().size());
                Task task = manager.getTaskById(1);
                assertNotNull(task);
                assertEquals("Test Task", task.getName());
                EpicTask epic = manager.getEpicById(2);
                assertNotNull(epic);
                assertEquals("Test Epic", epic.getName());
            });
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withSpecialCharacters_shouldWorkCorrectly() {
        try {
            Path tempFile = Files.createTempFile("special-chars-kanban", ".csv");
            File file = tempFile.toFile();
            TaskManager manager = Managers.getFileBackedTasksManager(file);
            Task task = new Task("Задача с кириллицей", "Описание с символами: !@#$%^&*()", null, null);
            EpicTask epic = new EpicTask("Эпик с эмодзи 🚀");
            SubTask subTask = new SubTask("Подзадача с кавычками \"test\"", epic.getId());
            assertDoesNotThrow(() -> {
                manager.createTask(task);
                manager.createEpic(epic);
                manager.createSubTask(subTask);
            });
            assertEquals(1, manager.getAllTasks().size());
            assertEquals(1, manager.getAllEpics().size());
            assertEquals(1, manager.getAllSubTasks().size());
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withLargeData_shouldWorkCorrectly() {
        try {
            Path tempFile = Files.createTempFile("large-data-kanban", ".csv");
            File file = tempFile.toFile();
            TaskManager manager = Managers.getFileBackedTasksManager(file);
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    Task task = new Task("Task " + i, "Description " + i, null, null);
                    manager.createTask(task);
                }
            });
            assertEquals(100, manager.getAllTasks().size());
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withTimeFields_shouldWorkCorrectly() {
        try {
            Path tempFile = Files.createTempFile("time-fields-kanban", ".csv");
            File file = tempFile.toFile();
            TaskManager manager = Managers.getFileBackedTasksManager(file);
            LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
            Duration duration = Duration.ofMinutes(60);
            assertDoesNotThrow(() -> {
                Task task = new Task("Task with time", "Description", startTime, duration);
                manager.createTask(task);
            });
            assertEquals(1, manager.getAllTasks().size());
            Task createdTask = manager.getAllTasks().get(0);
            assertEquals(startTime, createdTask.getStartTime());
            assertEquals(duration, createdTask.getDuration());
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
    @Test
    void save_withConcurrentAccess_shouldHandleGracefully() {
        try {
            Path tempFile = Files.createTempFile("concurrent-kanban", ".csv");
            File file = tempFile.toFile();
            TaskManager manager1 = Managers.getFileBackedTasksManager(file);
            TaskManager manager2 = Managers.getFileBackedTasksManager(file);
            assertDoesNotThrow(() -> {
                Task task1 = new Task("Task 1", "Description 1", null, null);
                Task task2 = new Task("Task 2", "Description 2", null, null);
                manager1.createTask(task1);
                manager2.createTask(task2);
            });
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            fail("Не удалось создать временный файл: " + e.getMessage());
        }
    }
}