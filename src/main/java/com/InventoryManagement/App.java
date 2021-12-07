package com.InventoryManagement;

import com.InventoryManagement.Tables.*;
import com.j256.ormlite.logger.*;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class App {
    enum Objects {
        Part,
        Student,
        Issue
    }

    public static void main(String[] args) throws SQLException, IOException {
        System.out.println("dis new");

        final CommandLineParser cmdParser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = cmdParser.parse(ArgManager.build(), args);
        } catch (ParseException e) {
            System.out.println("Unable to parse arguments. Error: " + e.getMessage());
            System.exit(1);
            return;
        }

        char mod = ' ';

        if (!commandLine.hasOption("v")) {
            LoggerFactory.setLogBackendFactory(new NullLogBackend.NullLogBackendFactory());
        }
        if (commandLine.hasOption("i")) {
            ConnectionSource connectionSource;
            IniManager.initialize();
            System.out.println("Successfully initialized settings.ini");
            if (commandLine.getOptionValue("i") == null) {
                connectionSource = IniManager.setDB("database.db");
            } else {
                connectionSource = IniManager.setDB(commandLine.getOptionValue("i"));
            }

            assert connectionSource != null;

            // create all the tables needed
            TableUtils.createTableIfNotExists(connectionSource, Category.class);
            TableUtils.createTableIfNotExists(connectionSource, Part.class);
            TableUtils.createTableIfNotExists(connectionSource, Student.class);
            TableUtils.createTableIfNotExists(connectionSource, Issue.class);
            connectionSource.close();

            System.out.println("Initialized database");
            System.exit(0);
        }
        if (commandLine.hasOption("a")) {
            mod = 'a';
        }
        if (commandLine.hasOption("r")) {
            mod = 'r';
        }
        if (commandLine.hasOption("H")) {
            Statistics.history(IniManager.getDB());
            System.exit(0);
            return;
        }
        if (commandLine.hasOption("c")) {
            Statistics.currentStock(IniManager.getDB());
            System.exit(0);
            return;
        }
        if (commandLine.hasOption("t")) {
            Statistics.totalStock(IniManager.getDB());
        }

        if (mod != ' ') {
            Class<?> model = null;
            String func = "";

            switch (commandLine.getOptionValue(mod).toLowerCase()) {
                case "student" -> model = Student.class;
                case "part" -> model = Part.class;
                case "issue" -> model = Issue.class;
            }

            switch (mod) {
                case 'a' -> func = "add";
                case 'r' -> func = "remove";
                case 'e' -> func = "edit";
            }

            ConnectionSource db = IniManager.getDB();

            try {
                model.getMethod(func, ConnectionSource.class).invoke(model, db);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }
}
