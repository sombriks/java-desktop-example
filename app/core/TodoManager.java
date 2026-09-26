package app.core;

//DEPS tools.jackson.core:jackson-databind:3.0.0

import java.nio.file.*;
import tools.jackson.databind.json.JsonMapper;


public class TodoManager {
    
    private static final String TODOS = "todos.json";

    private TodoList[] lists;

    public TodoManager() {
        try {
            Path p = Paths.get(TODOS);
            if(!Files.exists(p)) {
                lists = new TodoList[0];
            } else {
                lists = new JsonMapper().readValue(p.toFile(),TodoList[].class);
            }
        } catch(Exception e) {
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
        save();
        return null;
    }

    public TodoList[] getTodoLists(String q) {
        return null;
    }

    public TodoItem setTodoItem(String list, String item, boolean done) {
        save();
        return null;
    }

    public TodoItem[] getTodoItems(String list, String q) {
        return null;
    }

    public TodoItem[] getTodoItems(String list, boolean done) {
        return null;
    }
}
