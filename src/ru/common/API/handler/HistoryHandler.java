package ru.common.API.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.common.manager.task.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET" -> {
                    String jsonHistory = gson.toJson(manager.getHistory());
                    sendText(exchange, jsonHistory);
                }
                default -> sendNotFound(exchange, "Only GET supported for /history");
            }
        } catch (Exception e) {
            String jsonError = String.format("{\"error\":\"%s\"}", e.getMessage());
            exchange.sendResponseHeaders(500, jsonError.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(jsonError.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        }
    }
}
