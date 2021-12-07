package com.InventoryManagement;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class IniManager {
    public static void initialize() {
        try {
            File settings = new File("settings.ini");
            settings.createNewFile();
            Wini ini = new Wini(settings);
            ini.put("Defaults", "Database", "database.db");
            ini.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConnectionSource getDB() {
        try {
            Wini ini = new Wini(new File("settings.ini"));
            return new JdbcConnectionSource("jdbc:sqlite:" + ini.get("Defaults", "Database"));
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static ConnectionSource setDB(String db) {
        try {
            Wini ini = new Wini(new File("settings.ini"));
            ini.put("Defaults", "Database", db);
            ini.store();
            return getDB();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
