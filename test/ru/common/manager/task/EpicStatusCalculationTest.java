package ru.common.manager.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.model.task.*;
import static org.junit.jupiter.api.Assertions.*;

public class EpicStatusCalculationTest {
    private InMemoryTaskManager taskManager;
    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }
    @Test
    void epicStatus_allSubTasksNew_shouldBeNew() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        SubTask subTask3 = new SubTask("SubTask 3", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_allSubTasksDone_shouldBeDone() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        SubTask subTask3 = new SubTask("SubTask 3", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        subTask1.setStatus(TaskStatus.DONE);
        subTask2.setStatus(TaskStatus.DONE);
        subTask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        taskManager.updateSubTask(subTask3);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_mixedNewAndDone_shouldBeInProgress() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        SubTask subTask3 = new SubTask("SubTask 3", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        subTask1.setStatus(TaskStatus.NEW);
        subTask2.setStatus(TaskStatus.DONE);
        subTask3.setStatus(TaskStatus.NEW);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        taskManager.updateSubTask(subTask3);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_hasInProgressSubTask_shouldBeInProgress() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        SubTask subTask3 = new SubTask("SubTask 3", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        subTask1.setStatus(TaskStatus.NEW);
        subTask2.setStatus(TaskStatus.IN_PROGRESS);
        subTask3.setStatus(TaskStatus.NEW);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        taskManager.updateSubTask(subTask3);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_mixedAllStatuses_shouldBeInProgress() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        SubTask subTask3 = new SubTask("SubTask 3", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        subTask1.setStatus(TaskStatus.NEW);
        subTask2.setStatus(TaskStatus.IN_PROGRESS);
        subTask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        taskManager.updateSubTask(subTask3);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_noSubTasks_shouldBeNew() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_subTaskStatusChange_shouldUpdateEpicStatus() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("SubTask", epic.getId());
        taskManager.createSubTask(subTask);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
        subTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask);
        retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
        subTask.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask);
        retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_multipleSubTasksStatusChanges_shouldUpdateCorrectly() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        SubTask subTask3 = new SubTask("SubTask 3", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);
        retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
        subTask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask2);
        retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
        subTask1.setStatus(TaskStatus.DONE);
        subTask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask3);
        retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, retrievedEpic.getStatus());
    }
    @Test
    void epicStatus_subTaskRemoved_shouldRecalculateStatus() {
        EpicTask epic = new EpicTask("Test Epic");
        taskManager.createEpic(epic);
        SubTask subTask1 = new SubTask("SubTask 1", epic.getId());
        SubTask subTask2 = new SubTask("SubTask 2", epic.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        subTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTask1);
        EpicTask retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedEpic.getStatus());
        taskManager.removeSubTask(subTask1.getId());
        retrievedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }
}