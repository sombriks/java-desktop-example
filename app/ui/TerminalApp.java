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

    private final ListElement todoList = list()
            .id("todoList").addClass("focusable")
            .rounded().focusable().autoScroll().fill()
            .data(todoData, r -> {
                return text(r.description());
            });
    private final ListElement itemList = list()
            .id("itemList").addClass("focusable")
            .rounded().focusable().autoScroll().fill()
            .data(itemData, r -> {
                return text(r.description());
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

    @Override
    protected Element render() {
        TextInputElement todoFilter = textInput(todoFilterState)
                .id("todoFilter").addClass("focusable")
                .rounded().placeholder("create/search todos")
                .placeholderColor(Color.DARK_GRAY);
        TextInputElement itemFilter = textInput(itemFilterState)
                .id("itemFilter").addClass("focusable")
                .rounded().placeholder("create/search tasks")
                .placeholderColor(Color.DARK_GRAY);
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
