package ru.common.API.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.common.manager.task.TaskManager;
import ru.common.model.task.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubTasksHandler(TaskManager manager, Gson gson) {
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
                        List<SubTask> subTasks = manager.getAllSubTasks();
                        sendText(exchange, gson.toJson(subTasks));
                    } else {
                        SubTask subTask = manager.getSubTaskById(id);
                        if (subTask == null) {
                            sendNotFound(exchange, "SubTask not found");
                            return;
                        }
                        sendText(exchange, gson.toJson(subTask));
                    }
                }
                case "POST" -> {
                    String body = readBody(exchange);
                    SubTask subTask = gson.fromJson(body, SubTask.class);
                    boolean shouldCreate = (subTask.getId() == 0) || (manager.getSubTaskById(subTask.getId()) == null);
                    if (shouldCreate) {
                        SubTask toCreate = new SubTask(subTask.getName(), subTask.getDescription(), subTask.getParentId());
                        manager.createSubTask(toCreate);
                    } else {
                        manager.updateSubTask(subTask);
                    }
                    exchange.sendResponseHeaders(201, 0);
                    exchange.close();
                }
                case "DELETE" -> {
                    Integer id = getIdFromPathOrQuery(exchange);
                    if (id == null) {
                        manager.removeAllSubTasks();
                    } else {
                        boolean ok = manager.removeSubTask(id);
                        if (!ok) {
                            sendNotFound(exchange, "SubTask not found");
                            return;
                        }
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
