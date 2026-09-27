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

Our next sample is JavaFX and starts like the others:

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

```

## SWT is still around

## But what about the installer?
