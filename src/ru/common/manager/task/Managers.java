package ru.common.manager.task;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}

