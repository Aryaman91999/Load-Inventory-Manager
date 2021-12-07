package com.InventoryManagement;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ArgManager {
    public static Options build() {
        final Option verbose = Option.builder("v")
                .required(false)
                .hasArg(false)
                .longOpt("verbose")
                .desc("Print status of SQL queries")
                .build();

        final Option initialize = Option.builder("i")
                .required(false)
                .hasArg(true)
                .optionalArg(true)
                .argName("DB_NAME")
                .hasArg(false)
                .longOpt("initialize")
                .desc("initialize settings")
                .build();

        final Option add = Option.builder("a")
                .required(false)
                .hasArg(true)
                .optionalArg(false)
                .longOpt("add")
                .argName("object to add")
                .desc("set flag to add things")
                .build();

        final Option remove = Option.builder("r")
                .required(false)
                .hasArg(true)
                .optionalArg(false)
                .argName("object to add")
                .longOpt("remove")
                .desc("set flag to remove things")
                .build();

                final Option edit = Option.builder("e")
                .required(false)
                .hasArg(true)
                .optionalArg(false)
                .argName("object to edit")
                .longOpt("edit")
                .desc("set flag to edit things")
                .build();

        final Option history = Option.builder("H")
                .required(false)
                .hasArg(false)
                .longOpt("history")
                .desc("get issue and return history")
                .build();

        final Option currentStock = Option.builder("c")
                .required(false)
                .hasArg(false)
                .longOpt("currentStock")
                .desc("get current stock")
                .build();

        final Option totalStock = Option.builder("t")
                .required(false)
                .hasArg(false)
                .longOpt("totalStock")
                .desc("get total stock")
                .build();

        final Options options = new Options();
        options.addOption(verbose);
        options.addOption(initialize);
        options.addOption(add);
        options.addOption(remove);
        options.addOption(history);
        options.addOption(currentStock);
        options.addOption(totalStock);
        options.addOption(edit);
        return options;
    }
}
