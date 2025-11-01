package ru.common.API.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BaseHttpHandler {
    protected void sendText(HttpExchange http, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        http.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        http.sendResponseHeaders(200, resp.length);
        http.getResponseBody().write(resp);
        http.close();
    }

    protected void sendNotFound(HttpExchange http, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        http.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        http.sendResponseHeaders(404, resp.length);
        http.getResponseBody().write(resp);
        http.close();
    }

    protected void sendHasInteractions(HttpExchange http, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        http.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        http.sendResponseHeaders(406, resp.length);
        http.getResponseBody().write(resp);
        http.close();
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected List<String> getPathParts(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] raw = path.split("/");
        List<String> parts = new ArrayList<>();
        for (String p : raw) {
            if (!p.isEmpty()) parts.add(p);
        }
        return parts;
    }

    protected Integer getIdFromPathOrQuery(HttpExchange exchange) {
        // query ?id=
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("id=")) {
            try {
                return Integer.parseInt(query.substring(3));
            } catch (NumberFormatException ignored) {
            }
        }
        // path .../{id}
        List<String> parts = getPathParts(exchange);
        if (!parts.isEmpty()) {
            try {
                return Integer.parseInt(parts.get(parts.size() - 1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
