import ru.common.manager.EpicTask;
import ru.common.manager.SubTask;
import ru.common.manager.Task;
import ru.common.manager.TaskStatus;
import ru.common.model.TaskManager;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("=== СОЗДАНИЕ ЭПИКОВ ===");
        EpicTask developmentEpic = manager.createEpic(new EpicTask("Разработка продукта", "Создание нового SaaS решения"));
        EpicTask marketingEpic = manager.createEpic(new EpicTask("Маркетинговая кампания", "Продвижение на рынке"));
        printAllTasks(manager);

        System.out.println("\n=== СОЗДАНИЕ ЗАДАЧ ===");
        Task designTask = manager.createTask(new Task("Проектирование архитектуры", "Спроектировать микросервисную архитектуру", developmentEpic.getId()));
        Task apiTask = manager.createTask(new Task("Разработка API", "Создать основное API", developmentEpic.getId()));
        Task adsTask = manager.createTask(new Task("Создание рекламы", "Разработать рекламные материалы", marketingEpic.getId()));
        printAllTasks(manager);

        System.out.println("\n=== СОЗДАНИЕ ПОДЗАДАЧ ===");
        SubTask dbDesign = manager.createSubTask(new SubTask("Проектирование БД", "Создать ER-диаграмму", designTask.getId()));
        SubTask authService = manager.createSubTask(new SubTask("Сервис авторизации", "JWT аутентификация", apiTask.getId()));
        printAllTasks(manager);

        System.out.println("\n=== ОБНОВЛЕНИЕ СТАТУСОВ ===");
        dbDesign.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(dbDesign);

        authService.setStatus(TaskStatus.DONE);
        manager.updateTask(authService);

        adsTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(adsTask);
        printAllTasks(manager);

        System.out.println("\n=== ПОИСК ПО ID ===");
        Task foundTask = manager.getTaskById(designTask.getId());
        System.out.println("Найдена задача: " + foundTask.getName());

        List<SubTask> subtasks = manager.getSubTasksByEpicId(developmentEpic.getId());
        System.out.println("Подзадачи эпика 'Разработка продукта':");
        subtasks.forEach(st -> System.out.println("  • " + st.getName()));

        System.out.println("\n=== УДАЛЕНИЕ ===");
        System.out.println("Удаляем задачу:");
        manager.removeTask(apiTask);
        printAllTasks(manager);

        System.out.println("\nУдаляем эпик (каскадное удаление):");
        manager.removeEpic(marketingEpic);
        printAllTasks(manager);

        System.out.println("\n=== ОЧИСТКА ===");
        manager.removeAllSubTasks();
        manager.removeAllTasks();
        manager.removeAllEpics();
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nТекущее состояние менеджера:");
        System.out.println("─".repeat(50));

        System.out.println("Эпики (" + manager.getAllEpics().size() + "):");
        manager.getAllEpics().forEach(epic -> {
            System.out.println("  [EPIC] " + epic.getName() + " (" + epic.getStatus() + ")");

            List<Task> epicTasks = manager.getAllTasks().stream()
                    .filter(task -> task.getParentId() != null && task.getParentId() == epic.getId())
                    .toList();

            System.out.println("    Задачи (" + epicTasks.size() + "):");
            epicTasks.forEach(task -> {
                System.out.println("    • [TASK] " + task.getName() + " (" + task.getStatus() + ")");

                List<SubTask> taskSubtasks = manager.getAllSubTasks().stream()
                        .filter(st -> st.getParentId() != null && st.getParentId() == task.getId())
                        .toList();

                System.out.println("      Подзадачи (" + taskSubtasks.size() + "):");
                taskSubtasks.forEach(st ->
                        System.out.println("      ◦ [SUBTASK] " + st.getName() + " (" + st.getStatus() + ")")
                );
            });
        });
        System.out.println("─".repeat(50));
    }
}