
package dbworker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import json.JSONWorker;

public class DBWorker {
    private static String password;
    private static String login;
    private final String port;
    private final String ip;
    private static Connection con;
    private static Statement stmt;
    private volatile long max;
    private volatile long min;

    public DBWorker(JSONWorker jsonWorker) {
        password = jsonWorker.getPass_db();
        login = jsonWorker.getLog_db();
        this.port = jsonWorker.getPort_db();
        this.ip = jsonWorker.getIp_db();
    }

    public void startDB() {
        try {
            this.createConnect();
            if (!this.tableExists()) {
                this.initTable();
            }
        } catch (ClassNotFoundException | SQLException var2) {
            var2.printStackTrace();
        }

    }

    public void createConnect() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://" + this.ip + ":" + this.port + "/traffic_limits";
        String jdbcDriver = "com.mysql.jdbc.Driver";
        Class.forName(jdbcDriver);
        con = DriverManager.getConnection(url, login, password);
        if (con != null) {
            System.out.println("Successfully connected to MySQL database traffic_limits.");
        }

        stmt = con.createStatement();
    }

    boolean tableExists() throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        String tableName = "limits_per_hour";
        ResultSet resultSet = meta.getTables((String)null, (String)null, tableName, new String[]{"TABLE"});
        return resultSet.next();
    }

    public void updateLimit() throws SQLException, ClassNotFoundException {
        synchronized(this) {
            if (con.isClosed()) {
                this.createConnect();
            }
        }

        String selectMax = "SELECT MAX(limit_value) maxl FROM limits_per_hour WHERE effectiveDate = (SELECT MAX(effectiveDate) FROM limits_per_hour) LIMIT 1;";
        String selectMin = "SELECT MIN(limit_value) as minl FROM traffic_limits.limits_per_hour WHERE effectiveDate = (SELECT MAX(effectiveDate) FROM traffic_limits.limits_per_hour) LIMIT 1;";
        ResultSet resultMin;
        ResultSet resultMax;
        synchronized(this) {
            resultMin = stmt.executeQuery(selectMin);
            resultMax = stmt.executeQuery(selectMax);
        }

        if (resultMax.next()) {
            this.max = (long)Integer.parseInt(resultMax.getString("maxl"));
        }

        if (resultMin.next()) {
            this.min = (long)Integer.parseInt(resultMin.getString("minl"));
        }

    }

    private void initTable() throws SQLException {
        String newTable = "CREATE TABLE limits_per_hour (id INT(64) PRIMARY KEY AUTO_INCREMENT,limit_name VARCHAR(100) NOT NULL,limit_value INT(64) NOT NULL,effectiveDate TIMESTAMP); ";
        String initMin = "INSERT INTO limits_per_hour (limit_name,limit_value,effectiveDate) VALUES ('min',1024,NOW());";
        String initMax = "INSERT INTO limits_per_hour (limit_name,limit_value,effectiveDate) VALUES ('max',1073741824,NOW());";
        stmt.executeUpdate(newTable);
        stmt.executeUpdate(initMin);
        stmt.executeUpdate(initMax);
    }

    public long getMax() {
        return this.max;
    }

    public long getMin() {
        return this.min;
    }
}
