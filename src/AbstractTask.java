abstract class AbstractTask {
    private static int nextId = 1;
    private final int id;
    private String name;
    private String description;
    private TaskStatus status;

    public AbstractTask(String name, String description) {
        this.id = nextId++;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TaskStatus status) { this.status = status; }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[id=" + id +
                ", name=" + name +
                ", description=" + description +
                ", status=" + status + "]";
    }
}
