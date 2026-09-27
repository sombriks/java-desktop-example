# The state of java desktop

Back in the _Old Days_, it was just a mess. Now **jpackage** exists.

## Local-First is a thing again

Not everything needs to be in "the cloud".

Use local resources wisely and get the best possible from internet resources.

### Local but evergreen

Being locally installed does not mean to be terminally outdated. See how
browsers do, how [steam client][steam] this thing so well that it's easy to
forget that it was installed, not a page in a browser.

[steam]: https://store.steampowered.com

## But what makes desktop java a good idea?

- More than twenty years of solid libraries, documentation and compatibility
- Compatibility with major desktops
- Modern tools like [jbang][jbang]
- There is [jpackage][jpackage] now

[jbang]: https://jbang.dev
[jpackage]: https://dev.java/learn/jvm/other-tools/jpackage/

### JPackage, what's the deal?

Now you create a real, first-class installer for your application. And the
distributed installer doesn't need anything else in the target machine. All
required items goes bundled with it.

All you need is a few command lines.

## Fine, installers are easy bow, so what?

So, let's make a simple desktop app!

Something like this:

```
╔═══════════════════════════════════════════════════════════════╗
║ {My Todo App}                                                 ║
╠══════════════════════════════╦════════════════════════════════╣
║                              ║                                ║
║ [ Type to filter or Create ] ║ [ Type to filter or create   ] ║
║                              ║                                ║
║ Basic                    (5) ║ [ ] Review monthly report      ║
║ General                  (2) ║ [X] Workout                    ║
║ Groceries                (3) ║ [ ] Grocery shopping           ║
║ Important                (1) ║                                ║
║                              ║                                ║
╚══════════════════════════════╩════════════════════════════════╝
```

Now that all the hard work is done, let's code it.

## No HTML, what to use?

Unlike css/javascript frameworks, there is no new desktop widget toolkit every
week, so there are fewer but solid options.

For the sake of simplicity, i am testing all samples on Linux only, although
some of those might run just fine on other platforms.

Let's try the following UI toolkits:

- Swing + FlatLaf
- TamboUI
- JavaFx
- SWT

Before we start , please [install jbang using your preferred method][ins-jbang].

[ins-jbang]: https://www.jbang.dev/documentation/jbang/latest/installation.html

### Project skeleton

Use the powers of terminal to scaffold a minimum java app:

```bash
mkdir -p app/{core,ui}
touch app/core/Todo{Item,List,Manager}.java
touch app/ui/{Swing,JavaFx,Swt,Terminal}App.java
```

## Good Old Swing

Swing is the second oldest UI toolkit available to Java. It succeeded AWT and
decided to draw everything in java, so little platform-dependent code would
be needed to port it, so the _write once, run everywhere_ thing could hold true.

The presented frame can come to life using swing easily like this:

```bash
jbang init TodoSwing.java
```

This jbang entrypoint will provide a simple call to the swing app:

```java
/// usr/bin/env jbang "$0" "$@" ; exit $?
//SOURCES app/**/*.java
//JAVA 25+

import app.core.TodoManager;

import static app.ui.SwingApp.createApp;

void main(String... args) {
    createApp(new TodoManager());
}
```

We have some core operations for our todo app in `TodoManager`, they'll be
used by all desktop samples.

Swing code goes like this:

```java
package app.ui;

//DEPS com.formdev:flatlaf:3.5.4
//DEPS com.formdev:flatlaf-extras:3.5.4

import app.core.TodoItem;
import app.core.TodoList;
import app.core.TodoManager;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class SwingApp extends JFrame {

    public SwingApp(TodoManager manager) {
        Font fonteMono = new Font(Font.MONOSPACED, Font.PLAIN, 14);

        setTitle("My Todo App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        JTextField listsFilter = new JTextField();
        JList<TodoList> todoList = new JList<>();
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.add(listsFilter, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
        listsFilter.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "create/search todos");
        todoList.setFont(fonteMono);
        todoList.setCellRenderer(new DefaultListCellRenderer() {
            private String template = "%-20s (%3d)";

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TodoList todos) {
                    setText(template.formatted(todos.description(), todos.items().size()));
                }
                return this;
            }
        });
        todoList.setModel(new DefaultListModel<>() {
            @Override
            public int getSize() {
                return manager.getTodoLists(listsFilter.getText()).size();
            }

            @Override
            public TodoList getElementAt(int index) {
                return manager.getTodoLists(listsFilter.getText()).get(index);
            }
        });

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        JTextField itemsFilter = new JTextField();
        JList<TodoItem> todoItemList = new JList<>();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(itemsFilter, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(todoItemList), BorderLayout.CENTER);
        itemsFilter.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "create/search tasks");
        todoItemList.setFont(fonteMono);
        todoItemList.setCellRenderer(new DefaultListCellRenderer() {
            String template = "[%s] %s";

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TodoItem item) {
                    setText(template.formatted(item.done() ? "X" : " ", item.description()));
                }
                return this;
            }
        });
        todoItemList.setModel(new DefaultListModel<>() {
            @Override
            public int getSize() {
                TodoList selected = todoList.getSelectedValue();
                if (selected == null) {
                    return 0;
                }
                return manager.getTodoItems(selected.description(), itemsFilter.getText()).size();
            }

            @Override
            public TodoItem getElementAt(int index) {
                TodoList selected = todoList.getSelectedValue();
                if (selected == null) {
                    return null;
                }
                return manager.getTodoItems(selected.description(), itemsFilter.getText()).get(index);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
        setVisible(true);

        listsFilter.addActionListener(e -> {
            String list = listsFilter.getText().trim();
            listsFilter.setText("");
            TodoList selected = !list.isBlank()
                    ? manager.setTodoList(list)
                    : null;
            todoList.updateUI();
            todoList.setSelectedValue(selected, true);
        });

        itemsFilter.addActionListener(e -> {
            String item = itemsFilter.getText().trim();
            itemsFilter.setText("");

            TodoList selected = todoList.getSelectedValue();
            if (selected == null) {
                return;
            }

            TodoItem itemSelected = !item.isBlank()
                    ? manager.setTodoItem(selected.description(), item)
                    : null;
            todoList.updateUI();
            todoItemList.updateUI();
            todoItemList.setSelectedValue(itemSelected, true);
        });

        todoList.addListSelectionListener(e -> {
            if (todoList.getSelectedValue() == null) {
                return;
            }
            todoItemList.updateUI();
        });

        todoItemList.setComponentPopupMenu(new JPopupMenu() {
            {
                JMenuItem item = new JMenuItem("Selected is Done");
                add(item);
                item.addActionListener(e -> {
                    TodoList todoSelected = todoList.getSelectedValue();
                    TodoItem itemSelected = todoItemList.getSelectedValue();
                    if (todoSelected != null && itemSelected != null) {
                        manager.setTodoItem(todoSelected.description(), itemSelected.description(), true);
                        todoItemList.updateUI();
                    }
                });
            }
        });
    }

    public static void createApp(TodoManager manager) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SwingApp(manager);
        });
    }
}
```

Swing at its best: layouts, models, renderes and events.

Note also the dark theme registration: the [flatlaf][flatlaf] dependency
makes the swing appearance more bearable, and the defaults delivers a good
experience.

[flatlaf]: https://github.com/JFormDesigner/FlatLaf

The imperative, push-based mutations must be noted. Swing predates all these
modern UI concepts that we all learned to deal with over the last 10 years
of frontend development.

It works, but choosing swing in 2026 might not be the best take for local first.

## A modern terminal application

At first, a character-based interface and _modern_ might not look like
belonging in the same phrase, but think twice. [TamboUI][tamboui] makes
wonders for you and, since it runs over a terminal, it might save the day
when any tool must be provided over ssh.

[tamboui]: https://tamboui.dev/

For this one our entrypoint goes like this:

```bash
jbang init TodoTerminal.java
```

The entrypoint goes quite the same as our previous example:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//SOURCES app/**/*.java
//JAVA 25+

import app.core.TodoManager;

import static app.ui.TerminalApp.createApp;

void main(String... args) throws Exception {
    createApp(new TodoManager());
}
```

And our implementation goes like this:

```java
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
                .no-border {
                    border-type: none;
                }
                .aside {
                }
                .principal {
                }
                .focusable:focus {
                    border-color: green;
                }
                ListElement-item:selected {
                    text-style: bold;
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
                .id("todoFilter")
                .addClass("focusable")
                .placeholder("create/search todos")
                .placeholderColor(Color.DARK_GRAY)
                .rounded()
                .onSubmit(this::addList);
        todoList
                .addClass("focusable").fill()
                .focusable().autoScroll()
                .rounded()
                .onKeyEvent(keyEvent -> {
                    if (keyEvent.isConfirm()) {
                        loadItems();
                        runner().focusManager().setFocus("itemList");
                        return EventResult.HANDLED;
                    }
                    return EventResult.UNHANDLED;
                });

        TextInputElement itemFilter = textInput(itemFilterState)
                .id("itemFilter")
                .addClass("focusable")
                .placeholder("create/search tasks")
                .placeholderColor(Color.DARK_GRAY)
                .rounded()
                .onSubmit(this::addTask);
        itemList
                .addClass("focusable").fill()
                .focusable().autoScroll()
                .rounded()
                .onKeyEvent(keyEvent -> {
                    if (keyEvent.isConfirm()) {
                        checkTask();
                        return EventResult.HANDLED;
                    }
                    return EventResult.UNHANDLED;
                });

        return panel(" My Todo App ")
                .addClass("no-border")
                .add(panel()
                        .addClass("aside")
                        .add(todoFilter)
                        .add(todoList)
                        .fill(1))
                .add(panel()
                        .addClass("principal")
                        .add(itemFilter)
                        .add(itemList)
                        .fill(3))
                .horizontal();
    }

    public static void createApp(TodoManager todoManager) throws Exception {
        new TerminalApp(todoManager).run();
    }
}
```

Now, this is interesting.

We have a CSS dialect, a render function and a fluent api to setup the UI,
and the separation of concerns is pretty solid.

It is arguably better to maintain that the swing one.

## JavaFX, the really modern one

Our next sample is [JavaFX][JavaFX].

[JavaFX]: https://dev.java/learn/javafx/

This what people tend to suggest to use if there is _java_ and _desktop_ in
the same sentence.

It starts like the others:

```bash
jbang init TodoFx.java
```

The entrypoint is also identical:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//SOURCES app/**/*.java
//JAVA 25+

import app.core.TodoManager;

import static app.ui.JavaFxApp.createApp;

void main(String... args) {
    createApp(new TodoManager());
}
```

Again, using the same core business component.

The JavaFx implementation goes like this:

```java
package app.ui;

//DEPS org.openjfx:javafx-controls:23.0.2
//JAVA_OPTIONS --enable-native-access=ALL-UNNAMED

import app.core.TodoItem;
import app.core.TodoList;
import app.core.TodoManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
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
```

What a ride.

JavaFx delivers a nice experience, but hits an uncanny spot: it either can
be better than swing or worse, much worse.

It suits well for really big projects.

## SWT is still around

Our last study case is this oldie.

[SWT][SWT] was created because swing was a thing so hideous that some IBM
boys couldn't stand it.

[SWT]: https://eclipse.dev/eclipse/swt/

We create our entrypoint like this:

```bash
jbang init TodoSwt.java
```

The entrypoint is also identical:

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//SOURCES app/**/*.java
//JAVA 25+

import app.core.TodoManager;

import static app.ui.SwtApp.createApp;

void main(String... args) {
    createApp(new TodoManager());
}
```

The SWT implementation goes like this:

```java
package app.ui;

//DEPS org.eclipse.platform:org.eclipse.swt.gtk.linux.x86_64:3.129.0

import app.core.TodoItem;
import app.core.TodoList;
import app.core.TodoManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.List;

public class SwtApp {

    private final TodoManager manager;
    private List<TodoList> currentTodoLists = new ArrayList<>();
    private List<TodoItem> currentTodoItems = new ArrayList<>();

    private Text listsFilter;
    private org.eclipse.swt.widgets.List todoList;
    private Text itemsFilter;
    private org.eclipse.swt.widgets.List todoItemList;

    private final String listTemplate = "%-20s (%3d)";
    private final String itemTemplate = "[%s] %s";

    public SwtApp(TodoManager manager) {
        this.manager = manager;
    }

    public void start() {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("My Todo App");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());

        Font monoFont = new Font(display, "Monospace", 10, SWT.NORMAL);

        SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);

        Composite leftComposite = new Composite(sashForm, SWT.NONE);
        GridLayout leftLayout = new GridLayout(1, false);
        leftLayout.marginWidth = 10;
        leftLayout.marginHeight = 10;
        leftComposite.setLayout(leftLayout);

        listsFilter = new Text(leftComposite, SWT.BORDER | SWT.SEARCH);
        listsFilter.setMessage("create/search todos");
        listsFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        todoList = new org.eclipse.swt.widgets.List(leftComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        todoList.setFont(monoFont);
        todoList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite rightComposite = new Composite(sashForm, SWT.NONE);
        GridLayout rightLayout = new GridLayout(1, false);
        rightLayout.marginWidth = 10;
        rightLayout.marginHeight = 10;
        rightComposite.setLayout(rightLayout);

        itemsFilter = new Text(rightComposite, SWT.BORDER | SWT.SEARCH);
        itemsFilter.setMessage("create/search tasks");
        itemsFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        todoItemList = new org.eclipse.swt.widgets.List(rightComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        todoItemList.setFont(monoFont);
        todoItemList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        sashForm.setWeights(40, 60);

        Menu contextMenu = new Menu(todoItemList);
        MenuItem doneItem = new MenuItem(contextMenu, SWT.NONE);
        doneItem.setText("Selected is Done");
        doneItem.addListener(SWT.Selection, e -> {
            int listIdx = todoList.getSelectionIndex();
            int itemIdx = todoItemList.getSelectionIndex();
            if (listIdx >= 0 && listIdx < currentTodoLists.size() && itemIdx >= 0 && itemIdx < currentTodoItems.size()) {
                TodoList todoSelected = currentTodoLists.get(listIdx);
                TodoItem itemSelected = currentTodoItems.get(itemIdx);
                manager.setTodoItem(todoSelected.description(), itemSelected.description(), true);
                loadTodos();
                loadItems();
            }
        });
        todoItemList.setMenu(contextMenu);

        todoItemList.addListener(SWT.DefaultSelection, e -> {
            int listIdx = todoList.getSelectionIndex();
            int itemIdx = todoItemList.getSelectionIndex();
            if (listIdx >= 0 && listIdx < currentTodoLists.size() && itemIdx >= 0 && itemIdx < currentTodoItems.size()) {
                TodoList todoSelected = currentTodoLists.get(listIdx);
                TodoItem itemSelected = currentTodoItems.get(itemIdx);
                manager.setTodoItem(todoSelected.description(), itemSelected.description(), !itemSelected.done());
                loadTodos();
                loadItems();
            }
        });

        listsFilter.addListener(SWT.Modify, e -> {
            loadTodos();
            loadItems();
        });

        listsFilter.addListener(SWT.DefaultSelection, e -> {
            String text = listsFilter.getText().trim();
            listsFilter.setText("");
            TodoList created = !text.isBlank() ? manager.setTodoList(text) : null;
            loadTodos();
            if (created != null) {
                for (int i = 0; i < currentTodoLists.size(); i++) {
                    if (currentTodoLists.get(i).description().equals(created.description())) {
                        todoList.setSelection(i);
                        break;
                    }
                }
            }
            loadItems();
            itemsFilter.setFocus();
        });

        todoList.addListener(SWT.Selection, e -> loadItems());

        itemsFilter.addListener(SWT.Modify, e -> loadItems());

        itemsFilter.addListener(SWT.DefaultSelection, e -> {
            int selectedIndex = todoList.getSelectionIndex();
            if (selectedIndex < 0 || selectedIndex >= currentTodoLists.size()) {
                return;
            }
            TodoList selected = currentTodoLists.get(selectedIndex);
            String text = itemsFilter.getText().trim();
            itemsFilter.setText("");
            TodoItem created = !text.isBlank() ? manager.setTodoItem(selected.description(), text) : null;
            loadTodos();
            loadItems();
            if (created != null) {
                for (int i = 0; i < currentTodoItems.size(); i++) {
                    if (currentTodoItems.get(i).description().equals(created.description())) {
                        todoItemList.setSelection(i);
                        break;
                    }
                }
            }
        });

        shell.addListener(SWT.Dispose, e -> monoFont.dispose());

        loadTodos();
        if (todoList.getItemCount() > 0) {
            todoList.setSelection(0);
        }
        loadItems();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private void loadTodos() {
        String filter = listsFilter != null ? listsFilter.getText() : "";
        int selectedIndex = todoList != null ? todoList.getSelectionIndex() : -1;
        String selectedDescription = selectedIndex >= 0 && selectedIndex < currentTodoLists.size()
                ? currentTodoLists.get(selectedIndex).description()
                : null;

        currentTodoLists = manager.getTodoLists(filter != null ? filter : "");
        if (todoList == null) {
            return;
        }
        todoList.removeAll();
        int newSelectIndex = -1;
        for (int i = 0; i < currentTodoLists.size(); i++) {
            TodoList list = currentTodoLists.get(i);
            todoList.add(listTemplate.formatted(list.description(), list.items().size()));
            if (selectedDescription != null && selectedDescription.equals(list.description())) {
                newSelectIndex = i;
            }
        }
        if (newSelectIndex != -1) {
            todoList.setSelection(newSelectIndex);
        }
    }

    private void loadItems() {
        if (todoList == null || todoItemList == null) {
            return;
        }
        int selectedIndex = todoList.getSelectionIndex();
        if (selectedIndex < 0 || selectedIndex >= currentTodoLists.size()) {
            currentTodoItems = new ArrayList<>();
            todoItemList.removeAll();
            return;
        }
        TodoList selected = currentTodoLists.get(selectedIndex);
        String filter = itemsFilter != null ? itemsFilter.getText() : "";
        int itemSelectedIndex = todoItemList.getSelectionIndex();
        String selectedItemDescription = itemSelectedIndex >= 0 && itemSelectedIndex < currentTodoItems.size()
                ? currentTodoItems.get(itemSelectedIndex).description()
                : null;

        currentTodoItems = manager.getTodoItems(selected.description(), filter != null ? filter : "");
        todoItemList.removeAll();
        int newItemSelectIndex = -1;
        for (int i = 0; i < currentTodoItems.size(); i++) {
            TodoItem item = currentTodoItems.get(i);
            todoItemList.add(itemTemplate.formatted(item.done() ? "X" : " ", item.description()));
            if (selectedItemDescription != null && selectedItemDescription.equals(item.description())) {
                newItemSelectIndex = i;
            }
        }
        if (newItemSelectIndex != -1) {
            todoItemList.setSelection(newItemSelectIndex);
        }
    }

    public static void createApp(TodoManager manager) {
        new SwtApp(manager).start();
    }
}
```

The swt version is the most verbose one.

On the other hand, it will integrate perfectly with your current desktop.

But boy, it's so much work to get just the same.

## But what about the installer?

So, the user is not supposed to keep a working jbang setup, so we need to
package and ship it.

> **Note on Packaging Prerequisites:**
> `jpackage` relies on host system utilities to create package formats:
> - For `--type rpm`: `rpm-build` (command `rpmbuild`) is required
    (`sudo dnf install rpm-build` on Fedora/RHEL).
> - For `--type deb`: `dpkg-deb` and `fakeroot` are required
    (`sudo apt install dpkg fakeroot` on Debian/Ubuntu).
> - For `--type app-image`: No external packaging tools are required; produces a
    self-contained runtime folder.

```bash
rm -rf dist lib TodoSwt.jar
mkdir dist
# choose which app you want to ship
jbang export portable TodoSwt.java
mv lib dist
mv TodoSwt.jar dist
# generate rpm package with jpackage (requires rpm-build)
jpackage \
  --type rpm \
  --dest dist \
  --input dist \
  --name todo-swt \
  --main-jar TodoSwt.jar \
  --main-class TodoSwt \
  --app-version 1.0.0 \
  --linux-shortcut
```

The generated file will be inside the dist folder, and can be installed like
this:

```bash
sudo dnf install dist/todo-swt-1.0.0-1.x86_64.rpm
```

And _just like that_, it will appear on your menu.

You can check the `/opt/todo-swt` folder to see what is packaged. In short,
it's a trimmed down jre containing all that is needed to run the application.

## What a time to be alive

Deploy java applications on desktop took just 30 years to get it right. Sure,
the world was way more hostile back then, with microsoft trying to kill java
at any cost, and even the distribution channels still not mature.

Now it's all past, check your options noire broadly. Not everything needs to
be a web app to use web services. Consider the good part of local-first
applications. Even the project configuration can be as simple as a couple of
jbang entrypoints.

Happy coding, and check [the complete sample code here][repo].

[repo]: https://github.com/sombriks/java-desktop-example
