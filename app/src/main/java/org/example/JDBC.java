package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {
    static final String dbURL = "jdbc:mysql://localhost:3306/CatTracker";
    static final String USER = System.getenv("DB_USER");
    static final String PASS = System.getenv("DB_PASSWORD");
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbURL, USER, PASS);
    }
}
