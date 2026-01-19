import model.Task;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TidyTaskFrame extends JFrame {
    private JTextField titleField;
    private JTextField descriptionField;
    private JTextField dueDateField; // dd-MM-yyyy
    private JPanel taskListPanel;
    private JLabel statsLabel;
    private List<Task> tasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();

    public TidyTaskFrame() {
        setTitle("To Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 800);
        setLayout(new BorderLayout());//north,west,south,east,center

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        inputPanel.add(new JLabel("Naslov:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Opis zadatka:"));
        descriptionField = new JTextField();
        inputPanel.add(descriptionField);

        inputPanel.add(new JLabel("Rok (DD-MM-YYYY):"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        JButton addButton = new JButton("Dodaj zadatak");
        addButton.addActionListener(this::addTask);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addButton);

        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.add(inputPanel);
        wrapperPanel.add(buttonPanel);

        add(wrapperPanel, BorderLayout.NORTH);

        // Panel za prikaz liste zadataka
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Panel za filtriranje i statistiku
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton filterButton = new JButton("Filtriraj");
        filterPanel.add(filterButton);
        bottomPanel.add(filterPanel, BorderLayout.EAST);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsLabel = new JLabel("Ukupno: 0 | Završeno: 0 | Aktivno: 0");
        statsPanel.add(statsLabel);
        bottomPanel.add(statsPanel, BorderLayout.WEST);

        add(bottomPanel, BorderLayout.SOUTH);
        filterButton.addActionListener(e -> openFilterDialog());

        DatabaseHelper.createTable();
        tasks = DatabaseHelper.getAllTasks();
        updateTaskList();
        setVisible(true);
    }

    private void addTask(ActionEvent e) {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String deadlineStr = dueDateField.getText().trim();

        if (title.isEmpty() || description.isEmpty() || deadlineStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Popunite sva polja!", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate deadline = LocalDate.parse(deadlineStr, formatter);
            Task task = new Task(title, description, deadline, false);
            tasks.add(task);
            DatabaseHelper.addTask(task);
            updateTaskList();
            titleField.setText("");
            descriptionField.setText("");
            dueDateField.setText("");

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Datum mora biti u formatu DD-MM-YYYY!", "Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTaskList() {
        taskListPanel.removeAll();
        for (Task task : tasks) {
            JPanel taskPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel(task.getName() + "  |  " + task.getDescription() + "  |  " + task.getDeadline());

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.add(Box.createVerticalGlue());
            centerPanel.add(titleLabel);
            centerPanel.add(Box.createVerticalGlue());
            taskPanel.add(centerPanel, BorderLayout.CENTER);
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(task.isCompleted());
            checkBox.addActionListener(ev -> {
                task.setCompleted(checkBox.isSelected());
                DatabaseHelper.updateTaskStatus(task);
                updateTaskList();
            });
            checkBox.setEnabled(!checkBox.isSelected());

            taskPanel.add(checkBox, BorderLayout.WEST);
            JButton deleteButton = new JButton("Obriši");
            deleteButton.addActionListener(ev -> {
                tasks.remove(task);
                DatabaseHelper.deleteTask(task);
                updateTaskList();
            });
            taskPanel.add(deleteButton, BorderLayout.EAST);

            if (!task.isCompleted()) {
                LocalDate today = LocalDate.now();
                LocalDate deadline = task.getDeadline();

                if (deadline.isBefore(today)) {
                    titleLabel.setForeground(Color.RED);
                } else if (deadline.isEqual(today)) {
                    titleLabel.setForeground(Color.ORANGE);
                }
            }
            else{
                titleLabel.setForeground(Color.GREEN);
            }

            taskListPanel.add(taskPanel);
            taskListPanel.add(Box.createVerticalStrut(10));
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
        updateStats();
    }

    private void updateStats() {
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(Task::isCompleted).count();
        int active = total - completed;
        statsLabel.setText("Ukupno: " + total + " | Završeno: " + completed + " | Aktivno: " + active);
    }

    private void openFilterDialog() {
        String[] options = {"Po statusu", "Po datumu"};
        String choice = (String) JOptionPane.showInputDialog(this, "Izaberite način filtriranja:",
                "Filtriranje", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == null) return;

        filteredTasks.clear();

        switch (choice) {
            case "Po statusu" -> {
                tasks.sort(Comparator.comparing(Task::isCompleted));
                filteredTasks.addAll(tasks);
                showSortedTasksInNewWindow();
            }
            case "Po datumu" -> {
                tasks.sort(Comparator.comparing(Task::getDeadline));
                filteredTasks.addAll(tasks);
                showSortedTasksInNewWindow();
            }
        }
    }

    private void showSortedTasksInNewWindow() {
        JFrame sortedFrame = new JFrame("Sortirani zadaci");
        sortedFrame.setSize(400, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (Task task : filteredTasks) {
            JLabel taskLabel = new JLabel(task.getName() + " | " + task.getDeadline() + " | " +
                    (task.isCompleted() ? "Završeno" : "Nezavršeno"));
            JPanel taskPanel = new JPanel(new BorderLayout());
            taskPanel.add(taskLabel, BorderLayout.CENTER);
            taskPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            panel.add(taskPanel);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        sortedFrame.add(scrollPane);
        sortedFrame.setVisible(true);
    }
}
