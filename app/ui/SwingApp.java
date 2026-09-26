package app.ui;

//DEPS com.formdev:flatlaf:3.5.4
//DEPS com.formdev:flatlaf-extras:3.5.4

import app.core.TodoItem;
import app.core.TodoList;
import app.core.TodoManager;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class SwingApp extends JFrame {

    public SwingApp(TodoManager manager) {
        setTitle("My Todo App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu appMenu = new JMenu("My Todo App");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        appMenu.add(exitItem);
        menuBar.add(appMenu);
        setJMenuBar(menuBar);

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        JTextField listsFilter = new JTextField();
        JList<TodoList> todoList = new JList<>();
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.add(listsFilter, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);

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
        listsFilter.addActionListener(e -> {
            String list = listsFilter.getText().trim();
            if (!list.isBlank()) {
                manager.setTodoList(list);
            }
            listsFilter.setText("");
            todoList.updateUI();
        });

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        JTextField itemsFilter = new JTextField();
        JList<TodoItem> totoItemList = new JList<>();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(itemsFilter, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(totoItemList), BorderLayout.CENTER);

        totoItemList.setModel(new DefaultListModel<>() {
            @Override
            public int getSize() {
                TodoList selected = todoList.getSelectedValue();
                if(selected == null) {
                    return 0;
                }
                return manager.getTodoItems(selected.description(), itemsFilter.getText()).size();
            }

            @Override
            public TodoItem getElementAt(int index) {
                TodoList selected = todoList.getSelectedValue();
                if(selected == null) {
                    return null;
                }
                return manager.getTodoItems(selected.description(), itemsFilter.getText()).get(index);
            }
        });
        itemsFilter.addActionListener(e -> {
            String item = itemsFilter.getText().trim();
            itemsFilter.setText("");
            if (item.isEmpty()) {
                return;
            }
            TodoList selected = todoList.getSelectedValue();
            if(selected == null) {
                return;
            }
            manager.setTodoItem(selected.description(), item);
            totoItemList.updateUI();
        });
        todoList.addListSelectionListener(e -> {
            if(todoList.getSelectedValue() == null) {
                return;
            }
            totoItemList.updateUI();
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(220);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
        setVisible(true);
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
