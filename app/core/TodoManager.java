package app.core;

//DEPS tools.jackson.core:jackson-databind:3.0.0

import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TodoManager {

    private static final String TODOS = "todos.json";

    private List<TodoList> lists = new ArrayList<>();

    public TodoManager() {
        try {
            Path p = Paths.get(TODOS);
            if (Files.exists(p)) {
                lists = Arrays.stream(new JsonMapper()
                                .readValue(p.toFile(), TodoList[].class))
                        .sorted(Comparator.comparing(TodoList::description))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void save() {
        try {
            Path p = Paths.get(TODOS);
            new JsonMapper().writeValue(p.toFile(), lists);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TodoList setTodoList(String list) {
        String trimmed = list.trim();
        TodoList todos = lists.stream()
                .filter(l -> trimmed.equals(l.description()))
                .findFirst()
                .orElse(null);
        if (todos == null) {
            todos = new TodoList(trimmed);
            lists.add(todos);
        }
        save();
        return todos;
    }

    public List<TodoList> getTodoLists(String q) {
        return lists.stream()
                .filter(l -> l.description().contains(q))
                .sorted(Comparator.comparing(TodoList::description))
                .toList();
    }

    public TodoItem setTodoItem(String list, String item) {
        return setTodoItem(list, item, false);
    }

    public TodoItem setTodoItem(String list, String item, boolean done) {
        TodoList todos = setTodoList(list);
        String trimmed = item.trim();
        TodoItem todo = todos.items().stream()
                .filter(t -> trimmed.equals(t.description()))
                .findFirst()
                .orElse(null);
        if (todo != null) {
            todos.items().remove(todo);
        }
        todo = new TodoItem(trimmed, done);
        todos.items().add(todo);
        save();
        return todo;
    }

    public List<TodoItem> getTodoItems(String list, String q) {
        return lists.stream()
                .filter(l -> list.equals(l.description()))
                .findFirst()
                .map(l -> l.items())
                .stream()
                .flatMap(List::stream)
                .filter(i -> i.description().contains(q))
                .sorted(Comparator.comparing(TodoItem::description))
                .toList();
    }

    public List<TodoItem> getTodoItems(String list, String q, boolean done) {
        return getTodoItems(list, q).stream()
                .filter(i -> i.done() == done)
                .toList();
    }
}
