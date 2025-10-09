package ru.common.manager.history;

import ru.common.model.task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;


    @Override
    public <T extends Task> void add(T task) {
        if (task == null) return;
        if(history.containsKey(task.getId())) {
            removeNode(history.get(task.getId()));
        }

        Node node = new Node(task);
        linkLast(node);
        history.put(task.getId(), node);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }

    public String getHistoryAsString() {
        List<Task> historyList = getHistory();
        if (historyList.isEmpty()) {
            return "История пуста";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("История просмотров (последние ").append(historyList.size()).append("):\n");
        for (int i = 0; i < historyList.size(); i++) {
            Task task = historyList.get(i);
            sb.append(i + 1).append(". [ID: ").append(task.getId()).append("] ")
              .append(task.getName()).append(" (").append(task.getClass().getSimpleName()).append(")\n");
        }
        return sb.toString();
    }

    @Override
    public void removeById(int id) {
        if(history.containsKey(id)) {
            removeNode(history.get(id));
        }
    }

    private void linkLast(Node node) {
        node.prev = tail;
        node.next = null;

        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
    }

    private void removeNode(Node node) {
        if (node == null) return;

        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }

        if (node == head) {
            head = next;
        }
        if (node == tail) {
            tail = prev;
        }

        node.prev = null;
        node.next = null;
        history.remove(node.task.getId());
    }

    private static class Node {
        private Task task;
        private Node prev;
        private Node next;

        public Node(Task task) {
            this.task = task;
        }
    }

}


