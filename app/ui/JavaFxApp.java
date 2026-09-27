package app.ui;

//DEPS org.openjfx:javafx-controls:23.0.2
//JAVA_OPTIONS --enable-native-access=ALL-UNNAMED --enable-native-access=javafx.graphics

import app.core.TodoItem;
import app.core.TodoList;
import app.core.TodoManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFxApp extends Application {

    private static TodoManager manager;

    private TextField listsFilter;
    private ListView<TodoList> todoListView;
    private TextField itemsFilter;
    private ListView<TodoItem> todoItemListView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("My Todo App");

        listsFilter = new TextField();
        listsFilter.setPromptText("create/search todos");

        todoListView = new ListView<>();
        todoListView.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");
        todoListView.setCellFactory(lv -> new ListCell<>() {
            private final String template = "%-20s (%3d)";

            @Override
            protected void updateItem(TodoList item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(template.formatted(item.description(), item.items().size()));
                }
            }
        });

        VBox leftPanel = new VBox(10, listsFilter, todoListView);
        leftPanel.setPadding(new Insets(10));
        VBox.setVgrow(todoListView, Priority.ALWAYS);

        itemsFilter = new TextField();
        itemsFilter.setPromptText("create/search tasks");

        todoItemListView = new ListView<>();
        todoItemListView.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");
        todoItemListView.setCellFactory(lv -> new ListCell<>() {
            private final String template = "[%s] %s";

            @Override
            protected void updateItem(TodoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(template.formatted(item.done() ? "X" : " ", item.description()));
                }
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem doneMenuItem = new MenuItem("Selected is Done");
        doneMenuItem.setOnAction(e -> {
            TodoList todoSelected = todoListView.getSelectionModel().getSelectedItem();
            TodoItem itemSelected = todoItemListView.getSelectionModel().getSelectedItem();
            if (todoSelected != null && itemSelected != null) {
                manager.setTodoItem(todoSelected.description(), itemSelected.description(), true);
                loadTodosKeepSelection();
                loadItems();
            }
        });
        contextMenu.getItems().add(doneMenuItem);
        todoItemListView.setContextMenu(contextMenu);

        todoItemListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                TodoList todoSelected = todoListView.getSelectionModel().getSelectedItem();
                TodoItem itemSelected = todoItemListView.getSelectionModel().getSelectedItem();
                if (todoSelected != null && itemSelected != null) {
                    manager.setTodoItem(todoSelected.description(), itemSelected.description(), !itemSelected.done());
                    loadTodosKeepSelection();
                    loadItems();
                }
            }
        });

        VBox rightPanel = new VBox(10, itemsFilter, todoItemListView);
        rightPanel.setPadding(new Insets(10));
        VBox.setVgrow(todoItemListView, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane(leftPanel, rightPanel);
        splitPane.setDividerPositions(0.4);

        listsFilter.textProperty().addListener((obs, oldVal, newVal) -> loadTodos());
        itemsFilter.textProperty().addListener((obs, oldVal, newVal) -> loadItems());

        listsFilter.setOnAction(e -> {
            String list = listsFilter.getText().trim();
            listsFilter.clear();
            TodoList selected = !list.isBlank() ? manager.setTodoList(list) : null;
            loadTodos();
            if (selected != null) {
                selectTodoList(selected.description());
            }
            loadItems();
        });

        itemsFilter.setOnAction(e -> {
            String item = itemsFilter.getText().trim();
            itemsFilter.clear();
            TodoList selected = todoListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            TodoItem itemSelected = !item.isBlank() ? manager.setTodoItem(selected.description(), item) : null;
            loadTodosKeepSelection();
            loadItems();
            if (itemSelected != null) {
                selectTodoItem(itemSelected.description());
            }
        });

        todoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadItems());

        loadTodos();
        if (!todoListView.getItems().isEmpty()) {
            todoListView.getSelectionModel().selectFirst();
        }

        Scene scene = new Scene(splitPane, 640, 480);
        scene.getStylesheets().add("data:text/css," +
                ".root { -fx-base: #2b2b2b; -fx-background: #2b2b2b; -fx-control-inner-background: #1e1e1e; }");
        primaryStage.setScene(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadTodos() {
        String filter = listsFilter != null && listsFilter.getText() != null ? listsFilter.getText() : "";
        TodoList currentSelection = todoListView != null ? todoListView.getSelectionModel().getSelectedItem() : null;
        todoListView.getItems().setAll(manager.getTodoLists(filter));
        if (currentSelection != null) {
            selectTodoList(currentSelection.description());
        }
    }

    private void loadTodosKeepSelection() {
        TodoList currentSelection = todoListView.getSelectionModel().getSelectedItem();
        String filter = listsFilter != null && listsFilter.getText() != null ? listsFilter.getText() : "";
        todoListView.getItems().setAll(manager.getTodoLists(filter));
        if (currentSelection != null) {
            selectTodoList(currentSelection.description());
        }
    }

    private void selectTodoList(String description) {
        for (TodoList list : todoListView.getItems()) {
            if (list.description().equals(description)) {
                todoListView.getSelectionModel().select(list);
                break;
            }
        }
    }

    private void selectTodoItem(String description) {
        for (TodoItem item : todoItemListView.getItems()) {
            if (item.description().equals(description)) {
                todoItemListView.getSelectionModel().select(item);
                break;
            }
        }
    }

    private void loadItems() {
        TodoList selected = todoListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            todoItemListView.getItems().clear();
            return;
        }
        String filter = itemsFilter != null && itemsFilter.getText() != null ? itemsFilter.getText() : "";
        todoItemListView.getItems().setAll(manager.getTodoItems(selected.description(), filter));
    }

    public static void createApp(TodoManager todoManager) {
        manager = todoManager;
        Application.launch(JavaFxApp.class);
    }
}
