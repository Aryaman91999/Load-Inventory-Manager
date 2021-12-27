package com.load;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class AsDate {
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(String str) {
        return asDate(str, "dd-MM-yyyy");
    }

    public static Date asDate(String str, String format) {
        DateTimeFormatter d = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy").toFormatter();
        return AsDate.asDate(LocalDate.parse(str, d));
    }

    public static String toString(Date date) {
        return toString(date, "dd-MM-yyyy");
    }

    public static String toString(Date date, String f) {
        SimpleDateFormat format = new SimpleDateFormat(f);
        return format.format(date);
    }
}
