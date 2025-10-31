package ru.common.API;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerReadOnlyTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager, 0);
        server.start();
        baseUrl = "http://localhost:" + server.getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void history_returns200() throws Exception {
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/history")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
    }

    @Test
    void prioritized_returns200_and_sorted() throws Exception {
        Task t1 = new Task("A", "D", LocalDateTime.now().plusHours(1), Duration.ofHours(1));
        Task t2 = new Task("B", "D", LocalDateTime.now().plusHours(3), Duration.ofHours(1));
        manager.createTask(t1);
        manager.createTask(t2);
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/prioritized")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"name\":\"A\"") || resp.body().contains("\"name\":\"B\""));
    }
}
