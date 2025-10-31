package ru.common.API.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.common.manager.task.TaskManager;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET" -> {
                    List<String> parts = getPathParts(exchange);
                    if (parts.size() == 1) {
                        List<EpicTask> epics = manager.getAllEpics();
                        sendText(exchange, gson.toJson(epics));
                    } else if (parts.size() == 2) {
                        Integer id = getIdFromPathOrQuery(exchange);
                        EpicTask epic = manager.getEpicById(id);
                        if (epic == null) { sendNotFound(exchange, "Epic not found"); return; }
                        sendText(exchange, gson.toJson(epic));
                    } else if (parts.size() == 3 && "subtasks".equals(parts.get(2))) {
                        int id = Integer.parseInt(parts.get(1));
                        EpicTask epic = manager.getEpicById(id);
                        if (epic == null) { sendNotFound(exchange, "Epic not found"); return; }
                        List<SubTask> subs = manager.getSubTasksByEpicId(id);
                        sendText(exchange, gson.toJson(subs));
                    } else {
                        sendNotFound(exchange, "Unknown epics path");
                    }
                }
                case "POST" -> {
                    String body = readBody(exchange);
                    EpicTask epic = gson.fromJson(body, EpicTask.class);
                    boolean shouldCreate = (epic.getId() == 0) || (manager.getEpicById(epic.getId()) == null);
                    if (shouldCreate) {
                        EpicTask toCreate = new EpicTask(epic.getName(), epic.getDescription());
                        manager.createEpic(toCreate);
                    } else {
                        manager.updateEpic(epic);
                    }
                    exchange.sendResponseHeaders(201, 0);
                    exchange.close();
                }
                case "DELETE" -> {
                    Integer id = getIdFromPathOrQuery(exchange);
                    if (id == null) {
                        manager.removeAllEpics();
                    } else {
                        EpicTask epic = manager.getEpicById(id);
                        if (epic == null) { sendNotFound(exchange, "Epic not found"); return; }
                        manager.removeEpic(epic);
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
