package eu.acewolf.spigot.worldtimeout.mysql;

import eu.acewolf.spigot.worldtimeout.WorldTimeout;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private Connection connection;
    private String host, database, username, password;

    public MySQL(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;

        connect();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + database + "?autoReconnect=true", username, password);
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(WorldTimeout.PREFIX + "§cDie Verbindung zum MySQL-Server ist fehlgeschlagen!");
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed() && isConnected()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return getConnection() != null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void reopenConnection() {
        if (!isConnected()) {
            connect();
        }
    }
}


