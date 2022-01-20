package com.aryaman.load;

import com.aryaman.load.tables.Category;
import com.aryaman.load.tables.Issue;
import com.aryaman.load.tables.Part;
import com.aryaman.load.tables.Student;
import com.diogonunes.jcolor.Ansi;
import com.j256.ormlite.logger.*;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.fusesource.jansi.AnsiConsole;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class App {
    public static void main(String[] args) {

        final CommandLineParser cmdParser = new DefaultParser();
        CommandLine commandLine;

        AnsiConsole.systemInstall();

        Options options = ArgManager.build();

        try {
            commandLine = cmdParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(Ansi.colorize(e.getMessage(), RED_TEXT()));
            System.exit(1);
            return;
        }

        char mod = ' ';

        if (!commandLine.hasOption("v")) {
            Logger.setGlobalLogLevel(Level.ERROR);
        }

        if (commandLine.hasOption("h")) {
            new HelpFormatter().printHelp("./load <options>", options);
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
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
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
        if (commandLine.hasOption("f")) {
            mod = 'f';
        }
        if (commandLine.hasOption("L")) {
            mod = 'L';
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
            System.exit(0);
            return;
        }

        if (mod != ' ') {
            Class<?> model = null;
            String func;

            switch (commandLine.getOptionValue(mod).toLowerCase()) {
                case "student" -> model = Student.class;
                case "part" -> model = Part.class;
                case "issue" -> model = Issue.class;
                default -> System.out.printf("No such object: %s%n", commandLine.getOptionValue(mod));
            }

            if (model == null) {
                return;
            }

            switch (mod) {
                case 'a' -> func = "add";
                case 'r' -> func = "remove";
                case 'e' -> func = "edit";
                case 'l' -> func = "list";
                case 'f' -> func = "filter";
                case 'L' -> func = "load";
                default -> func = "";
            }

            ConnectionSource db = IniManager.getDB();

            // when ctrl + c is pressed, close the connection
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    db.close();
                } catch (Exception e) {
                    System.out.printf("An Exception occurred: %s%n", e.getMessage());
                }
            }));

            try {
                if (mod == 'L') {
                    model.getMethod(func, ConnectionSource.class, String.class).invoke(model.getConstructor().newInstance(), db, commandLine.getOptionValues("L")[1]);
                    db.close();
                    return;
                }
                model.getMethod(func, ConnectionSource.class).invoke(model.getConstructor().newInstance(), db);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof SQLException) {
                    System.out.printf("SQL Error: %s%n", e.getCause().getMessage());
                }
            } catch (Exception e) {
                System.out.printf("An Exception occurred: %s%n", e.getMessage());
            }

        }
    }

}