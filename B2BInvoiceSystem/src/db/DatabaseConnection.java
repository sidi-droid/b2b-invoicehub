package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// singleton class for managing the MySQL connection
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/b2b_invoice_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "root123";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("  ✔ Database connected successfully.");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. " +
                    "Add mysql-connector-java.jar to your classpath.", e);
        }
    }

    // lazy initialization - creates instance on first call
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("  ✔ Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("  ✘ Error closing connection: " + e.getMessage());
        }
    }
}
