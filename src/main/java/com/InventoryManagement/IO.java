package com.InventoryManagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.function.Function;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class IO {
    private final Scanner scanner;

    public IO() {
        scanner = new Scanner(System.in);
    }

    public int getInteger(String prompt) {
        return getInteger(prompt, null, null);
    }

    public int getInteger(String prompt, Function<Integer, Boolean> validator, String msg) {
        int r;

        while (true) {
            System.out.print(prompt);

            // typesafe input
            while (!scanner.hasNextInt()) {
                scanner.nextLine();
                System.out.println(colorize("Only integers allowed!", RED_TEXT()));
                System.out.print(prompt);
            }
            r = scanner.nextInt();

            if (r < 0) {
                System.out.println(colorize("No negatives allowed!", RED_TEXT()));
                continue;
            }

            if (validator != null) {
                if (validator.apply(r)) {
                    break;
                }

                System.out.println(colorize(msg, RED_TEXT()));
            } else {
                break;
            }
        }

        scanner.nextLine();
        return r;
    }

    public String getString(String prompt) {
        String in;

        do {
            System.out.print(prompt);
            in = scanner.nextLine();
        } while (in.equals(""));

        return in;
    }

    public String getString(String prompt, Function<String, Boolean> validator) {
        String in;

        do {
            System.out.print(prompt);
            in = scanner.nextLine();
        } while (in.equals("") || !validator.apply(in));

        return in;
    }

    public boolean getBoolean(String prompt) {
        String in = getString(prompt,
                s -> Arrays.asList("y", "yes", "n", "no").contains(s.toLowerCase()) // lazy way of checking if input is valid
        ).toLowerCase();

        return Arrays.asList("yes", "y").contains(in);
    }

    public Date getDate(String prompt) {
        CheckDate c = new CheckDate();
        String in = getString(prompt, c::check);
        DateTimeFormatter d = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return AsDate.asDate(LocalDate.parse(in, d));
    }
}
