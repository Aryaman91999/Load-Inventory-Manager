package com.InventoryManagement;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AsDate {
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String toString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(date);
    }
}
