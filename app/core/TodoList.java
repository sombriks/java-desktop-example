package app.core;

import java.util.ArrayList;
import java.util.List;

public record TodoList(String description, List<app.core.TodoItem> items) {
    public TodoList {
        if (items == null) {
            items = new ArrayList<app.core.TodoItem>();
        }
    }

    public TodoList(String description) {
        this(description, null);
    }
}
