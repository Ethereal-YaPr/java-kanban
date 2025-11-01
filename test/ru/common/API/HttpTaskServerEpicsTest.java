package ru.common.API;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.task.InMemoryTaskManager;
import ru.common.manager.task.TaskManager;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest {
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
    void createEpic_and_getById_and_getSubtasks() throws Exception {
        EpicTask epic = new EpicTask("Epic A", "Big");
        HttpResponse<String> createResp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/epics"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createResp.statusCode(), "Epic create failed: " + createResp.body());

        HttpResponse<String> listResp = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/epics")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, listResp.statusCode(), "List epics failed: " + listResp.body());
        EpicTask[] epics = gson.fromJson(listResp.body(), EpicTask[].class);
        assertNotNull(epics, "List body: " + listResp.body());
        assertTrue(epics.length >= 1, "List body: " + listResp.body());
        int epicId = epics[0].getId();

        // создаём подзадачу через API
        SubTask st = new SubTask("S1", "d", epicId);
        HttpResponse<String> createSt = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/subtasks"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(st)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createSt.statusCode(), "Subtask create failed: " + createSt.body());

        HttpResponse<String> byId = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/epics/" + epicId)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, byId.statusCode(), "Epic by id body: " + byId.body());
        EpicTask epicFromApi = gson.fromJson(byId.body(), EpicTask.class);
        assertNotNull(epicFromApi, "Epic by id body: " + byId.body());
        assertEquals("Epic A", epicFromApi.getName(), "Epic by id body: " + byId.body());

        HttpResponse<String> subs = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/epics/" + epicId + "/subtasks")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, subs.statusCode(), "Subs body: " + subs.body());
        SubTask[] subtasks = gson.fromJson(subs.body(), SubTask[].class);
        assertNotNull(subtasks, "Subs body: " + subs.body());
        assertTrue(subtasks.length >= 1, "Subs body: " + subs.body());
        assertEquals("S1", subtasks[0].getName(), "Subs body: " + subs.body());
    }
}
