import model.Task;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:task.db";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                deadline TEXT,
                completed INTEGER
            )
            """;

        try (Connection conn = connect(); Statement s = conn.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // dodavanje zadatka
    public static void addTask(Task task) {
        String sql = "INSERT INTO tasks(name, description, deadline, completed) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getDeadline().toString());
            pstmt.setInt(4, task.isCompleted() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ažuriranje statusa zadatka
    public static void updateTaskStatus(Task task) {
        String sql = "UPDATE tasks SET completed = ? WHERE name = ? AND deadline = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, task.isCompleted() ? 1 : 0);
            pstmt.setString(2, task.getName());
            pstmt.setString(3, task.getDeadline().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // brisanje zadatka
    public static void deleteTask(Task task) {
        String sql = "DELETE FROM tasks WHERE name = ? AND deadline = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getDeadline().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // učitavanje svih zadataka
    public static List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                LocalDate deadline = LocalDate.parse(rs.getString("deadline"));
                boolean completed = rs.getInt("completed") == 1;
                tasks.add(new Task(name, description, deadline, completed));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}
