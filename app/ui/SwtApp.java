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
