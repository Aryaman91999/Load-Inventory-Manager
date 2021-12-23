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

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class App {
    enum Objects {
        Part,
        Student,
        Issue
    }

    public static void main(String[] args) {

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
            System.out.println(colorize("Successfully initialized settings.ini", GREEN_TEXT()));
            if (commandLine.getOptionValue("i") == null) {
                connectionSource = IniManager.setDB("database.db");
            } else {
                connectionSource = IniManager.setDB(commandLine.getOptionValue("i"));
            }

            assert connectionSource != null;

            try {
                // create all the tables needed
                TableUtils.createTableIfNotExists(connectionSource, Category.class);
                TableUtils.createTableIfNotExists(connectionSource, Part.class);
                TableUtils.createTableIfNotExists(connectionSource, Student.class);
                TableUtils.createTableIfNotExists(connectionSource, Issue.class);
                connectionSource.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }

            System.out.println(colorize("Initialized database", GREEN_TEXT()));
            System.exit(0);
        }
        if (commandLine.hasOption("a")) {
            mod = 'a';
        }
        if (commandLine.hasOption("r")) {
            mod = 'r';
        }
        if (commandLine.hasOption("l")) {
            mod = 'l';
        }
        if (commandLine.hasOption("e")) {
            mod = 'e';
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
                case 'l' -> func = "list";
            }

            ConnectionSource db = IniManager.getDB();

            try {
                model.getMethod(func, ConnectionSource.class).invoke(model.getConstructor().newInstance(), db);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof SQLException) {
                    System.out.println("SQL Error: " + e.getMessage());
                } else {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException | IllegalArgumentException
                    | NoSuchMethodException | SecurityException | InstantiationException e) {

                e.printStackTrace();
            }

        }
    }
}
