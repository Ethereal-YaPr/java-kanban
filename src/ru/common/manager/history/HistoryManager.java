package ru.common.manager.history;

import ru.common.model.task.Task;

import java.util.List;

public interface HistoryManager {
    <T extends Task> void add(T task);

    List<Task> getHistory();

    void removeById(int id);

    String getHistoryAsString();
}