package ru.common.manager.task;

import ru.common.manager.exceptions.ManagerSaveException;
import ru.common.model.task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
        loadFromFile(file);
    }


    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic\n");

            // Порядок важен для корректной загрузки: сначала EPIC, затем TASK, затем SUBTASK
            for (EpicTask epic : getAllEpics()) {
                writer.write(epic.toCSVString() + "\n");
            }
            for (Task task : getAllTasks()) {
                writer.write(task.toCSVString() + "\n");
            }
            for (SubTask subtask : getAllSubTasks()) {
                writer.write(subtask.toCSVString() + "\n");
            }


            List<Task> history = getHistory();
            if (!history.isEmpty()) {
                writer.write("\nHISTORY:\n");
                for (Task task : history) {
                    writer.write(task.getId() + "\n");
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл", e);
        }
    }

    private void loadFromFile(File file) {
        if (!file.exists()) {
            throw new ManagerSaveException("Файла не существует: " + file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeaderRead = false;
            List<String> historyIds = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (!isHeaderRead) {
                    isHeaderRead = true;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                System.out.println("Текущая строка: " + line);

                if (line.contains("HISTORY:")) {
                    while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                        try {
                            int id = Integer.parseInt(line.trim());
                            historyIds.add(String.valueOf(id));
                        } catch (NumberFormatException e) {
                            System.err.println("Некорректный ID в истории: " + line);
                        }
                    }
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.err.println("Строка не соответствует формату: " + line);
                    continue;
                }

                switch (TaskType.valueOf(parts[1])) {
                    case TASK:
                        addTaskFromCSV(parts);
                        break;
                    case SUBTASK:
                        addSubTaskFromCSV(parts);
                        break;
                    case EPIC:
                        addEpicFromCSV(parts);
                        break;
                    default:
                        System.err.println("Неизвестный тип задачи: " + parts[1]);
                }
            }

            restoreHistory(historyIds);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось прочитать данные из файла", e);
        }
    }

    private void addTaskFromCSV(String[] parts) {
        int id = Integer.parseInt(parts[0]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        Task task = new Task(name, description);
        task.setId(id);
        task.setStatus(status);
        createTask(task);
    }

    private void addSubTaskFromCSV(String[] parts) {
        int id = Integer.parseInt(parts[0]);
        int parentId = Integer.parseInt(parts[5]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        SubTask subtask = new SubTask(name, description, parentId);
        subtask.setId(id);
        subtask.setStatus(status);
        createSubTask(subtask);
    }

    private void addEpicFromCSV(String[] parts) {
        int id = Integer.parseInt(parts[0]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        EpicTask epic = new EpicTask(name, description);
        epic.setId(id);
        epic.setStatus(status);
        createEpic(epic);
    }

    private void restoreHistory(List<String> historyIds) {
        for (String idStr : historyIds) {
            try {
                int id = Integer.parseInt(idStr);
                Task task = getTaskById(id);
                if (task != null) {
                    historyManager.add(task);
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка восстановления истории: " + idStr);
            }
        }
    }

    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public Task getTaskById(int id) {
        Task result = super.getTaskById(id);
        save();
        return result;
    }

    @Override
    public boolean updateTask(Task task) {
        try {
            super.updateTask(task);
            save();
            return true;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }


    @Override
    public boolean removeTask(Task task) {
        try {
            super.removeTask(task);
            save();
            return true;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public void removeAllTasks() {
        try {
            super.removeAllTasks();
            save();
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }


    @Override
    public SubTask createSubTask(SubTask subtask) {
        try {
            SubTask result = super.createSubTask(subtask);
            save();
            return result;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public SubTask getSubTaskById(int id) {
        try {
            SubTask result = super.getSubTaskById(id);
            save();
            return result;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }

    }

    @Override
    public void removeAllSubTasks() {
        try {
            super.removeAllSubTasks();
            save();
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public EpicTask createEpic(EpicTask epic) {
        try {
            EpicTask result = super.createEpic(epic);
            save();
            return result;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public EpicTask getEpicById(int id) {
        try {
            EpicTask result = super.getEpicById(id);
            save();
            return result;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public boolean updateEpic(EpicTask epic) {
        try {
            super.updateEpic(epic);
            save();
            return true;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public boolean removeEpic(EpicTask epic) {
        try {
            super.removeEpic(epic);
            save();
            return true;
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    @Override
    public void removeAllEpics() {
        try {
            super.removeAllEpics();
            save();
        } catch (ManagerSaveException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }
}