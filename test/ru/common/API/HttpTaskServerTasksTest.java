package ru.common.API;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.task.InMemoryTaskManager;
import ru.common.manager.task.TaskManager;
import ru.common.model.task.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;
    private String baseUrl;
    private Gson gson;

    @BeforeEach
    void setUp() throws Exception {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager, 0);
        server.start();
        baseUrl = "http://localhost:" + server.getPort();
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void createTask_returns201_and_managerHasIt() throws Exception {
        Task task = new Task("Task A", "Desc", LocalDateTime.now(), Duration.ofHours(2));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getAllTasks().size());
        assertEquals("Task A", manager.getAllTasks().get(0).getName());
    }

    @Test
    void getTaskById_404_if_not_exists() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/tasks/999")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, resp.statusCode());
    }

    @Test
    void updateTask_returns201() throws Exception {
        Task t = new Task("T", "D", LocalDateTime.now(), Duration.ofHours(1));
        manager.createTask(t);
        t.setName("T2");
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/tasks"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, resp.statusCode());
        assertEquals("T2", manager.getTaskById(t.getId()).getName());
    }

    @Test
    void createTask_overlap_returns406() throws Exception {
        Task t1 = new Task("A", "D", LocalDateTime.now(), Duration.ofHours(2));
        manager.createTask(t1);
        Task t2 = new Task("B", "D", t1.getStartTime().plusMinutes(30), Duration.ofHours(1));
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/tasks"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t2)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(406, resp.statusCode());
    }

    @Test
    void deleteTaskById_returns200_and_removed() throws Exception {
        Task t = new Task("T", "D", LocalDateTime.now(), Duration.ofHours(1));
        manager.createTask(t);
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/tasks/" + t.getId())).DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }
}
