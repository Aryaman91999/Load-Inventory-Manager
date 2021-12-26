package com.InventoryManagement;

import com.InventoryManagement.Tables.Table;
import com.j256.ormlite.field.DatabaseField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Filter {
    public static <T> void list(List<T> rows) {
        Class<? extends Object> cls = rows.get(0).getClass();

        Map<Field, Integer> max_lens = new LinkedHashMap<Field, Integer>();
        for (Field field : cls.getFields()) {
            if (field.isAnnotationPresent(DatabaseField.class)) {
                max_lens.put(field, field.getName().length());
            }
        }

        for (T row : rows) {
            for (Field key : max_lens.keySet()) {
                int a;
                try {
                    if (Table.class.isAssignableFrom(key.getType())) { // if its a table
                        Class<?> c = key.get(row).getClass();
                        Field f = c.getField("name");
                        a = f.get(key.get(row)).toString().length();

                    } else if (key.getType() == Date.class) { // if its a date
                        a = AsDate.toString((Date) key.get(row)).length();

                    } else if (key.getAnnotation(DatabaseField.class).useGetSet()) { // if it uses get/set
                        a = ((String) getGetter(key, cls).invoke(row)).length();

                    } else { // normal field
                        a = key.get(row).toString().length();
                    }
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                    return;
                }

                if (a > max_lens.get(key)) {
                    max_lens.put(key, a);
                }
            }
        }

        // print header
        for (Field field : cls.getFields()) {
            System.out.printf("| %s ", capitalize(field.getName().replaceAll("_", " ")) + " ".repeat(max_lens.get(field) - field.getName().length()));
        }
        System.out.println("|");

        // print... separator?
        for (Field f : max_lens.keySet()) {
            System.out.printf("|-%s-", "-".repeat(max_lens.get(f)));
        }

        System.out.println("|");

        for (T row : rows) {
            for (Field field : max_lens.keySet()) {
                try {
                    String field_val;
                    if (Table.class.isAssignableFrom(field.getType())) {
                        Class<?> c = field.get(row).getClass();
                        Field f = c.getField("name");
                        field_val = f.get(field.get(row)).toString();

                    } else if (field.getType() == Date.class) {
                        field_val = AsDate.toString((Date) field.get(row));

                    } else if (field.getAnnotation(DatabaseField.class).useGetSet()) {
                        field_val = (String) getGetter(field, cls).invoke(row);

                    } else {
                        field_val = field.get(row).toString();
                    }
                    System.out.printf("| %s ", field_val + " ".repeat(max_lens.get(field) - field_val.length()));

                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("|");
        }
    }

    public static String capitalize(String str) {
        int strLen = str.length();
        StringBuffer buffer = new StringBuffer(strLen);
        boolean capitalizeNext = true;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);

            if (ch == ' ') {
                buffer.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    private static Method getGetter(Field field, Class<?> cls) {
        try {
            return cls.getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
