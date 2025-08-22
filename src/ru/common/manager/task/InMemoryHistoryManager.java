package ru.common.manager.task;

import ru.common.config.Config;
import ru.common.model.task.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new LinkedList<>();

    @Override
    public <T extends Task> void add(T task) {
        if (task == null) return;
        history.add(task);
        if (history.size() > Config.HISTORY_COLLECTION_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void removeById(int id) {
        history.removeIf(t -> t.getId() == id);
    }
}


