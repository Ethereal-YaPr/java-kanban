import ru.common.manager.task.InMemoryTaskManager;
import ru.common.model.task.EpicTask;
import ru.common.model.task.SubTask;
import ru.common.model.task.TaskStatus;
import ru.common.manager.task.TaskManager;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();

        System.out.println("=== СОЗДАНИЕ ЭПИКОВ ===");
        EpicTask projectEpic = manager.createEpic(new EpicTask("Разработка проекта", "Создание трекера задач"));
        EpicTask testingEpic = manager.createEpic(new EpicTask("Тестирование", "Комплексное тестирование системы"));
        printAllTasks(manager);

        System.out.println("\n=== СОЗДАНИЕ ПОДЗАДАЧ ===");
        SubTask modelTask = manager.createSubTask(new SubTask("Разработка моделей", "Создание базовых классов", projectEpic.getId()));
        SubTask managerTask = manager.createSubTask(new SubTask("Разработка менеджера", "Создание TaskManager", projectEpic.getId()));
        SubTask unitTestTask = manager.createSubTask(new SubTask("Модульные тесты", "Написание unit-тестов", testingEpic.getId()));
        printAllTasks(manager);

        System.out.println("\n=== ОБНОВЛЕНИЕ СТАТУСОВ ===");
        modelTask.setStatus(TaskStatus.DONE);
        manager.updateTask(modelTask);

        managerTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(managerTask);

        unitTestTask.setStatus(TaskStatus.NEW);
        manager.updateTask(unitTestTask);
        printAllTasks(manager);

        System.out.println("\n=== ПОЛУЧЕНИЕ ЗАДАЧ ===");
        System.out.println("Все эпики:");
        manager.getAllEpics().forEach(epic -> System.out.println("• " + epic.getName() + " [" + epic.getStatus() + "]"));

        System.out.println("\nПодзадачи эпика 'Разработка проекта':");
        manager.getSubTasksByEpicId(projectEpic.getId())
                .forEach(subtask -> System.out.println("• " + subtask.getName() + " [" + subtask.getStatus() + "]"));

        System.out.println("\n=== УДАЛЕНИЕ ЗАДАЧ ===");
        System.out.println("Удаляем подзадачу:");
        manager.removeTask(unitTestTask);
        printAllTasks(manager);

        System.out.println("\nУдаляем все подзадачи:");
        manager.removeAllSubTasks();
        printAllTasks(manager);

        System.out.println("\n=== ЗАВЕРШЕНИЕ РАБОТЫ ===");
        manager.removeAllTasks();
        manager.removeAllEpics();
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nСостояние трекера задач:");
        System.out.println("─".repeat(50));

        System.out.println("Эпики (" + manager.getAllEpics().size() + "):");
        manager.getAllEpics().forEach(epic -> {
            System.out.println("  [EPIC] "+ epic.getId()+ " " + epic.getName() + " [" + epic.getStatus() + "]");

            List<SubTask> subtasks = manager.getSubTasksByEpicId(epic.getId());
            System.out.println("    Подзадачи (" + subtasks.size() + "):");
            subtasks.forEach(st ->
                    System.out.println("    • [SUBTASK] " +st.getId() + " " + st.getName() + " [" + st.getStatus() + "]")
            );
        });
        System.out.println("─".repeat(50));
    }
}