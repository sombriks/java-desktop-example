package app.ui;

//DEPS dev.tamboui:tamboui-toolkit:LATEST
//DEPS dev.tamboui:tamboui-jline3-backend:LATEST

import app.core.TodoItem;
import app.core.TodoList;
import app.core.TodoManager;
import dev.tamboui.css.engine.StyleEngine;
import dev.tamboui.style.Color;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;
import dev.tamboui.toolkit.elements.ListElement;
import dev.tamboui.toolkit.elements.TextInputElement;
import dev.tamboui.toolkit.event.EventResult;
import dev.tamboui.tui.TuiConfig;
import dev.tamboui.widgets.input.TextInputState;

import java.util.ArrayList;
import java.util.List;

import static dev.tamboui.toolkit.Toolkit.*;

public class TerminalApp extends ToolkitApp {

    private final TextInputState todoFilterState = new TextInputState();
    private final TextInputState itemFilterState = new TextInputState();

    private final List<TodoList> todoData = new ArrayList<>();
    private final List<TodoItem> itemData = new ArrayList<>();

    private final ListElement<TodoList> todoList = list().id("todoList")
            .data(todoData, r -> {
                String template = "%-20s (%3d)";
                return text(template.formatted(r.description(), r.items().size()));
            });
    ListElement<TodoItem> itemList = list().id("itemList")
            .data(itemData, r -> {
                String template = "[%s] %s";
                return text(template.formatted(r.done() ? "X" : " ", r.description()));
            });

    private final TodoManager manager;

    public TerminalApp(TodoManager manager) {
        this.manager = manager;
    }

    @Override
    protected TuiConfig configure() {
        return super.configure()
                .toBuilder()
                .mouseCapture(true)
                .build();
    }

    @Override
    protected void onStart() {
        setWindowTitle("My Todo App");
        StyleEngine style = StyleEngine
                .create();
        style.addStylesheet("""
                .focusable :focus {
                    border-color: green;
                }
                """);
        runner().styleEngine(style);

        loadTodos();
        loadItems();
    }

    private void loadTodos() {
        todoData.clear();
        todoData.addAll(manager.getTodoLists(todoFilterState.text()));
    }

    private void loadItems() {
        TodoList todos = todoData.get(todoList.selected());
        itemData.clear();
        itemData.addAll(manager.getTodoItems(todos.description(), itemFilterState.text()));
    }

    private void addList() {
        String todo = todoFilterState.text().trim();
        todoFilterState.clear();
        TodoList todos = !todo.isBlank()
                ? manager.setTodoList(todo)
                : null;
        loadTodos();
        if (todos != null) {
            todoList.selected(todoData.indexOf(todos));
        }
        loadItems();
        runner().focusManager().setFocus("itemFilter");
    }

    private void addTask() {
        String task = itemFilterState.text().trim();
        itemFilterState.clear();
        if (!task.isBlank()) {
            TodoList todos = todoData.get(todoList.selected());
            manager.setTodoItem(todos.description(), task, false);
            loadItems();
        }
    }

    private void checkTask() {
        TodoList todos = todoData.get(todoList.selected());
        TodoItem item = itemData.get(itemList.selected());
        manager.setTodoItem(todos.description(), item.description(), !item.done());
        loadItems();
    }

    @Override
    protected Element render() {

        TextInputElement todoFilter = textInput(todoFilterState)
                .id("todoFilter").addClass("focusable")
                .rounded().placeholder("create/search todos")
                .placeholderColor(Color.DARK_GRAY)
                .onSubmit(this::addList);
        todoList
                .addClass("focusable").fill()
                .rounded().focusable().autoScroll()
                .onKeyEvent(keyEvent -> {
                    if (keyEvent.isConfirm()) {
                        loadItems();
                        runner().focusManager().setFocus("itemList");
                        return EventResult.HANDLED;
                    }
                    return EventResult.UNHANDLED;
                });

        TextInputElement itemFilter = textInput(itemFilterState)
                .id("itemFilter").addClass("focusable")
                .rounded().placeholder("create/search tasks")
                .placeholderColor(Color.DARK_GRAY)
                .onSubmit(this::addTask);
        itemList
                .addClass("focusable").fill()
                .rounded().focusable().autoScroll()
                .onKeyEvent(keyEvent -> {
                    if (keyEvent.isConfirm()) {
                        checkTask();
                        return EventResult.HANDLED;
                    }
                    return EventResult.UNHANDLED;
                });

        return panel(" My Todo App ")
                .add(panel()
                        .add(todoFilter)
                        .add(todoList)
                        .margin(1)
                        .borderless()
                        .percent(33))
                .add(panel()
                        .add(itemFilter)
                        .add(itemList)
                        .margin(1)
                        .borderless()
                        .percent(66))
                .doubleBorder()
                .horizontal();
    }

    public static void createApp(TodoManager todoManager) throws Exception {
        new TerminalApp(todoManager).run();
    }
}
