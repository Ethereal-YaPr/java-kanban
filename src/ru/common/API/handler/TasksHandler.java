package ru.common.API.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.common.manager.task.TaskManager;
import ru.common.model.task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET" -> {
                    Integer id = getIdFromPathOrQuery(exchange);
                    if (id == null) {
                        List<Task> tasks = manager.getAllTasks();
                        sendText(exchange, gson.toJson(tasks));
                    } else {
                        Task task = manager.getTaskById(id);
                        if (task == null) {
                            sendNotFound(exchange, "Task not found");
                            return;
                        }
                        sendText(exchange, gson.toJson(task));
                    }
                }
                case "POST" -> {
                    String body = readBody(exchange);
                    Task task = gson.fromJson(body, Task.class);
                    boolean shouldCreate = (task.getId() == 0) || (manager.getTaskById(task.getId()) == null);
                    if (shouldCreate) {
                        Task toCreate = new Task(task.getName(), task.getDescription(), task.getStartTime(), task.getDuration());
                        manager.createTask(toCreate);
                    } else {
                        manager.updateTask(task);
                    }
                    exchange.sendResponseHeaders(201, 0);
                    exchange.close();
                }
                case "DELETE" -> {
                    Integer id = getIdFromPathOrQuery(exchange);
                    if (id == null) {
                        manager.removeAllTasks();
                    } else {
                        Task task = manager.getTaskById(id);
                        if (task == null) {
                            sendNotFound(exchange, "Task not found");
                            return;
                        }
                        manager.removeTask(task);
                    }
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                }
                default -> sendNotFound(exchange, "Unsupported HTTP method");
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            String jsonError = String.format("{\\\"error\\\":\\\"%s\\\"}", e.getMessage());
            exchange.sendResponseHeaders(500, jsonError.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(jsonError.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        }
    }
}
