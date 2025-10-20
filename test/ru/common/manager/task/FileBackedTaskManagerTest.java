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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    @Test
    void saveAndLoadEmptyFile() throws IOException {
        Path tmp = Files.createTempFile("kanban-empty", ".csv");
        File file = tmp.toFile();
        file.deleteOnExit();

        TaskManager manager = Managers.getFileBackedTasksManager(file);
        // Сохраняем пустое состояние
        ((FileBackedTaskManager) manager).save();

        List<String> lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty(), "Файл должен содержать хотя бы заголовок");
        assertEquals("id,type,name,status,description,epic", lines.get(0));
        assertEquals(1, lines.size(), "Пустой файл после сохранения должен содержать только заголовок");

        // Загружаем снова из того же файла и проверяем, что данных нет
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

        Task t1 = new Task("Task-1", "T1-desc");
        Task t2 = new Task("Task-2", "T2-desc");
        manager.createTask(t1);
        manager.createTask(t2);

        SubTask s1 = new SubTask("Sub-1", "S1-desc", epic.getId());
        manager.createSubTask(s1);

        List<String> lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        // Отбрасываем заголовок и возможные пустые строки/блок истории
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

        // Сначала заполним файл через первый менеджер
        TaskManager writer = Managers.getFileBackedTasksManager(file);

        EpicTask epic = new EpicTask("Epic-2", "E2-desc");
        writer.createEpic(epic);

        Task t1 = new Task("Task-A", "TA-desc");
        Task t2 = new Task("Task-B", "TB-desc");
        writer.createTask(t1);
        writer.createTask(t2);

        SubTask s1 = new SubTask("Sub-X", "SX-desc", epic.getId());
        writer.createSubTask(s1);

        // Теперь читаем тем же файлом через новый менеджер
        TaskManager reader = Managers.getFileBackedTasksManager(file);

        assertEquals(2, reader.getAllTasks().size(), "Должно загрузиться 2 обычные задачи");
        assertEquals(1, reader.getAllEpics().size(), "Должен загрузиться 1 эпик");
        assertEquals(1, reader.getAllSubTasks().size(), "Должна загрузиться 1 подзадача");
    }
}


