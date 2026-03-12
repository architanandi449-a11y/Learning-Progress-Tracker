package learningtracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:learning.db";

    private static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        }
        return conn;
    }

    public static void createTables() {
        String userTableSql = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id integer PRIMARY KEY AUTOINCREMENT,"
                + "username text NOT NULL UNIQUE,"
                + "password text"
                + ");";

        String progressTableSql = "CREATE TABLE IF NOT EXISTS progress ("
                + "id integer PRIMARY KEY AUTOINCREMENT,"
                + "user_id integer NOT NULL,"
                + "topic text NOT NULL,"
                + "duration integer NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES users (user_id)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            if (conn == null) {
                System.err.println("Failed to get database connection.");
                return;
            }
            stmt.execute(userTableSql);
            stmt.execute(progressTableSql);
        } catch (SQLException e) {
            System.err.println("Table creation error: " + e.getMessage());
        }
    }

    public static int getOrCreateUser(String username) {
        // Backwards-compatible helper: creates user with no password if not present
        String selectSql = "SELECT user_id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users(username) VALUES(?)";
        
        try (Connection conn = connect()) {
            if (conn == null) throw new SQLException("Database connection failed");

            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
                pstmtSelect.setString(1, username);
                ResultSet rs = pstmtSelect.executeQuery();
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }

            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtInsert.setString(1, username);
                pstmtInsert.executeUpdate();
                
                ResultSet generatedKeys = pstmtInsert.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("User login/creation error: " + e.getMessage());
            return -1;
        }
    }

    public static int createUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Create user error: " + e.getMessage());
        }
        return -1;
    }

    public static int authenticateUser(String username, String password) {
        String sql = "SELECT user_id, password FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPass = rs.getString("password");
                    if (dbPass == null) dbPass = "";
                    if (password.equals(dbPass)) {
                        return rs.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authenticate error: " + e.getMessage());
        }
        return -1;
    }

    public static void resetDatabase() {
        // Drop tables and recreate
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS progress");
            stmt.execute("DROP TABLE IF EXISTS users");
        } catch (SQLException e) {
            System.err.println("Reset error (drop): " + e.getMessage());
        }
        createTables();
    }

    public static void addProgress(int userId, String topic, int duration) {
        String sql = "INSERT INTO progress (user_id, topic, duration) VALUES (?, ?, ?)";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, topic);
            pstmt.setInt(3, duration);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Add progress error: " + e.getMessage());
        }
    }

    // New: addProgress that returns the generated id
    public static int addProgressReturnId(int userId, String topic, int duration) {
        String sql = "INSERT INTO progress (user_id, topic, duration) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, topic);
            pstmt.setInt(3, duration);
            int affected = pstmt.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Add progress error: " + e.getMessage());
        }
        return -1;
    }

    // Update an existing progress entry
    public static boolean updateProgress(int id, String topic, int duration) {
        String sql = "UPDATE progress SET topic = ?, duration = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, topic);
            pstmt.setInt(2, duration);
            pstmt.setInt(3, id);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Update progress error: " + e.getMessage());
            return false;
        }
    }

    // Delete a progress entry by id
    public static boolean deleteProgress(int id) {
        String sql = "DELETE FROM progress WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Delete progress error: " + e.getMessage());
            return false;
        }
    }

    // Get recent progress entries for a user (id, topic, duration)
    public static List<Object[]> getRecentProgress(int userId) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, topic, duration FROM progress WHERE user_id = ? ORDER BY id DESC LIMIT 100";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[] { rs.getInt("id"), rs.getString("topic"), rs.getInt("duration") });
            }
        } catch (SQLException e) {
            System.err.println("Get recent progress error: " + e.getMessage());
        }
        return list;
    }

    public static Map<String, Integer> getSubjectData(int userId) {
        Map<String, Integer> subjectData = new HashMap<>();
        String sql = "SELECT topic, SUM(duration) as total_duration FROM progress " +
                    "WHERE user_id = ? GROUP BY topic";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                subjectData.put(rs.getString("topic"), rs.getInt("total_duration"));
            }
        } catch (SQLException e) {
            System.err.println("Get subject data error: " + e.getMessage());
        }
        return subjectData;
    }

    public static Map<String, String> getStatistics(int userId) {
        Map<String, String> stats = new HashMap<>();
        String sqlTotal = "SELECT SUM(duration) as total FROM progress WHERE user_id = ?";
        String sqlAvg = "SELECT AVG(duration) as avg FROM progress WHERE user_id = ?";
        String sqlTop = "SELECT topic, SUM(duration) as total_duration FROM progress "
                     + "WHERE user_id = ? GROUP BY topic ORDER BY total_duration DESC LIMIT 1";

        try (Connection conn = connect()) {
            if (conn == null) return stats;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlTotal)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int totalMinutes = rs.getInt("total");
                    stats.put("Total Time", String.format("%.2f hours", totalMinutes / 60.0));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAvg)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("Average Session", String.format("%.1f minutes", rs.getDouble("avg")));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlTop)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("Top Topic", rs.getString("topic"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Get statistics error: " + e.getMessage());
        }
        
        stats.putIfAbsent("Total Time", "0 hours");
        stats.putIfAbsent("Average Session", "0 minutes");
        stats.putIfAbsent("Top Topic", "N/A");

        return stats;
    }

    public static Object[][] getLeaderboardData() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT u.username, SUM(p.duration) as total_time "
                  + "FROM users u "
                  + "LEFT JOIN progress p ON u.user_id = p.user_id "
                  + "GROUP BY u.user_id, u.username "
                  + "ORDER BY total_time DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String username = rs.getString("username");
                int totalMinutes = rs.getInt("total_time");
                double totalHours = totalMinutes / 60.0;
                String totalTime = String.format("%.2f hours", totalHours);
                data.add(new Object[]{username, totalTime, totalHours});
            }
        } catch (SQLException e) {
            System.err.println("Get leaderboard data error: " + e.getMessage());
        }

        return data.toArray(new Object[0][]);
    }
}