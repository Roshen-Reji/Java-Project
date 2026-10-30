package com.hotel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    public static final String DB_URL = "jdbc:sqlite:hotel_astra.db";
    public static final String DB_USER = "";
    public static final String DB_PASSWORD = "";

    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/hotel_astra";
    public static final String MYSQL_USER = "root";
    public static final String MYSQL_PASSWORD = "password";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {

        }
    }

    private DatabaseConnection() {

    }

    public static Connection getConnection() throws SQLException {
        Connection conn;
        if (DB_USER.isEmpty() && DB_PASSWORD.isEmpty()) {
            conn = DriverManager.getConnection(DB_URL);
        } else {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        } catch (SQLException ignored) {

        }
        return conn;
    }

    public static void closeConnection() {

    }
}
