package ru.common.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.common.API.handler.*;
import ru.common.API.util.DurationAdapter;
import ru.common.API.util.LocalDateTimeAdapter;
import ru.common.manager.task.Managers;
import ru.common.manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int DEFAULT_PORT = 8080;
    private final HttpServer server;
    private final Gson gson;
    private final TaskManager manager;

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault(), DEFAULT_PORT);
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this(manager, DEFAULT_PORT);
    }

    public HttpTaskServer(TaskManager manager, int port) throws IOException {
        this.gson = getGson();
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/tasks", new TasksHandler(this.manager, this.gson));
        server.createContext("/epics", new EpicsHandler(this.manager, this.gson));
        server.createContext("/subtasks", new SubTasksHandler(this.manager, this.gson));
        server.createContext("/history", new HistoryHandler(this.manager, this.gson));
        server.createContext("/prioritized", new PrioritizedHandler(this.manager, this.gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту: " + getPort());
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }
}
