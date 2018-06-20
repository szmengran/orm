package com.szmengran.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Package com.szmengran.test
 * @Description: 高并发测试
 * @date 2018年4月13日 上午8:51:46
 * @author <a href="mailto:android_li@sina.cn">Joe</a>
 */
public class ConcurrentTest {
	private static final String dbClassName = "com.mysql.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://192.168.0.117:3306/test";
    private static final String USER = "test";
    private static final String PASSWORD = "TesT12345";
    private static void executeSQL(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    private static void ResetEnvironment() throws SQLException {
        Properties p = new Properties();
        p.put("user", USER);
        p.put("password", PASSWORD);
        try (Connection conn = DriverManager.getConnection(CONNECTION, p)) {
            for (String query: new String[] {
                    "DROP DATABASE IF EXISTS test",
                    "CREATE DATABASE test",
                    "USE test",
                    "CREATE TABLE tbl (id INT AUTO_INCREMENT PRIMARY KEY, createstamp timestamp default current_timestamp)"
            }) {
                executeSQL(conn, query);
            }
        }
    }
    private static void worker() {
        Properties properties = new Properties();
        properties.put("user", USER);
        properties.put("password", PASSWORD);
        try (Connection conn = DriverManager.getConnection(CONNECTION, properties)) {
//        		while (!Thread.interrupted()) {
        			executeSQL(conn, "INSERT INTO tbl(ID) VALUES (NULL)");
//        		}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        Class.forName(dbClassName);
        ResetEnvironment();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 1000; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    worker();
                }
            });
        }
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.err.println("Pool did not terminate");
        }
    }
}
