package app.ui;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import app.core.TodoManager;

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
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField filterOrCreateField = new JTextField();
        JList<String> categoryList = new JList<>();
        leftPanel.add(filterOrCreateField, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField searchField = new JTextField();
        JPanel todoListPanel = new JPanel();
        todoListPanel.setLayout(new BoxLayout(todoListPanel, BoxLayout.Y_AXIS));

        JScrollPane todoScrollPane = new JScrollPane(todoListPanel);
        rightPanel.add(searchField, BorderLayout.NORTH);
        rightPanel.add(todoScrollPane, BorderLayout.CENTER);

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