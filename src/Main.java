import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("=== СОЗДАНИЕ ЭПИКОВ ===");
        EpicTask developmentEpic = manager.createEpic(new EpicTask("Разработка продукта", "Создание нового SaaS решения"));
        EpicTask marketingEpic = manager.createEpic(new EpicTask("Маркетинговая кампания", "Продвижение на рынке"));
        printAllTasks(manager);


        System.out.println("\n=== СОЗДАНИЕ ЗАДАЧ ===");
        Task designTask = manager.createTask(new Task("Проектирование архитектуры", "Спроектировать микросервисную архитектуру", developmentEpic));
        Task apiTask = manager.createTask(new Task("Разработка API", "Создать основное API", developmentEpic));
        Task adsTask = manager.createTask(new Task("Создание рекламы", "Разработать рекламные материалы", marketingEpic));
        printAllTasks(manager);


        System.out.println("\n=== СОЗДАНИЕ ПОДЗАДАЧ ===");
        SubTask dbDesign = manager.createSubTask(new SubTask("Проектирование БД", "Создать ER-диаграмму", designTask));
        SubTask authService = manager.createSubTask(new SubTask("Сервис авторизации", "JWT аутентификация", apiTask));
        printAllTasks(manager);


        System.out.println("\n=== ОБНОВЛЕНИЕ ОБЪЕКТОВ ===");
        developmentEpic.setDescription("Обновленное описание разработки");
        manager.updateEpic(developmentEpic);

        adsTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(adsTask);

        authService.setStatus(TaskStatus.DONE);
        manager.updateSubTask(authService);
        printAllTasks(manager);


        System.out.println("\n=== РАБОТА ЧЕРЕЗ ID ===");
        int epicId = developmentEpic.getId();
        int taskId = designTask.getId();
        int subtaskId = dbDesign.getId();

        EpicTask foundEpic = manager.getEpicById(epicId);
        System.out.println("Найден эпик по ID: " + foundEpic.getName());


        System.out.println("\n=== ТЕСТИРОВАНИЕ УДАЛЕНИЯ ===");
        System.out.println("Удаляем подзадачу по объекту:");
        manager.removeSubTask(authService);
        printAllTasks(manager);

        System.out.println("\nУдаляем задачу по ID:");
        manager.removeTaskById(apiTask.getId());
        printAllTasks(manager);

        System.out.println("\nУдаляем эпик по объекту (каскадное удаление):");
        manager.removeEpic(marketingEpic);
        printAllTasks(manager);


        System.out.println("\n=== ПОДЗАДАЧИ ЭПИКА ===");
        List<SubTask> epicSubTasks = manager.getSubTasksByEpicId(developmentEpic.getId());
        epicSubTasks.forEach(st -> System.out.println("  • " + st.getName()));


        System.out.println("\n=== ОЧИСТКА МЕНЕДЖЕРА ===");
        System.out.println("Удаляем все подзадачи:");
        manager.removeAllSubTasks();
        printAllTasks(manager);

        System.out.println("\nУдаляем все задачи:");
        manager.removeAllTasks();
        printAllTasks(manager);

        System.out.println("\nУдаляем все эпики:");
        manager.removeAllEpics();
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nТекущее состояние менеджера:");
        System.out.println("─".repeat(50));

        System.out.println("Эпики (" + manager.getAllEpics().size() + "):");
        manager.getAllEpics().forEach(epic -> {
            System.out.println("  [EPIC] " + epic.getName() + " (" + epic.getStatus() + ")");

            System.out.println("    Задачи (" + epic.getTasks().size() + "):");
            epic.getTasks().values().forEach(task -> {
                System.out.println("    • [TASK] " + task.getName() + " (" + task.getStatus() + ")");

                System.out.println("      Подзадачи (" + task.getSubTasks().size() + "):");
                task.getSubTasks().values().forEach(subTask -> {
                    System.out.println("      ◦ [SUBTASK] " + subTask.getName() + " (" + subTask.getStatus() + ")");
                });
            });
        });
        System.out.println("─".repeat(50));
    }
}
