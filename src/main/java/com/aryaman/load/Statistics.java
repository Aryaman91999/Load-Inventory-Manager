package com.aryaman.load;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.aryaman.load.tables.Issue;
import com.aryaman.load.tables.Part;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.aryaman.load.AsDate.asDate;
import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class Statistics {
    public static class Record {
        Date date;
        String part_name;
        int part_quantity;
        String student_name;
        boolean is_return;

        Record(Date d, String p, int pq, String s, boolean r) {
            date = d;
            part_name = p;
            part_quantity = pq;
            student_name = s;
            is_return = r;
        }
    }

    static class sortRecord implements Comparator<Record> {
        @Override
        public int compare(Record o1, Record o2) {
            return o1.date.compareTo(o2.date);
        }
    }

    public static void history(ConnectionSource connectionSource) {
        System.out.println("Issue and return history: ");

        try {
            List<Record> records = new ArrayList<>();

            for (Issue issue :
                    DaoManager.createDao(connectionSource, Issue.class)) {
                records.add(new Record(issue.issued_on, issue.part.name, issue.quantity, issue.issued_to.name, false));

                Date current = AsDate.asDate(LocalDate.now());
                if (issue.return_on.equals(current) || issue.return_on.before(current)) {
                    records.add(new Record(issue.return_on, issue.part.name, issue.quantity, issue.issued_to.name, true));
                }
            }

            records.sort(new sortRecord());

            for (Record rec :
                    records) {

                SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd, yyyy");
                String date = format.format(rec.date);

                if (rec.is_return) {
                    System.out.printf("%s: %s %s %s(%d)%n", colorize(date, BOLD()), rec.student_name,
                            colorize("returned", GREEN_TEXT()), rec.part_name, rec.part_quantity);
                } else {
                    System.out.printf("%s: %s %s(%d) to %s%n", colorize(date, BOLD()), colorize("issued", RED_TEXT()),
                            rec.part_name, rec.part_quantity, rec.student_name);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void currentStock(ConnectionSource connectionSource) {
        try {
            System.out.println("Current Stock: ");

            for (Part part :
                    DaoManager.createDao(connectionSource, Part.class)) {
                Dao<Issue, Integer> issueDao = DaoManager.createDao(connectionSource, Issue.class);

                // select * from issue where part = part and return_on > today's date
                List<Issue> dues = issueDao.query(issueDao.queryBuilder().where().eq("part_id", part.id).and().gt("return_on", AsDate.asDate(LocalDate.now())).prepare());

                int due_num = 0;

                for (Issue due :
                        dues) {
                    due_num += due.quantity;
                }

                System.out.printf("%s: %s%n", colorize(part.name, BOLD()), colorize(String.format("%d out of %d", part.quantity - due_num, part.quantity),
                        due_num == 0 ? GREEN_TEXT() : RED_TEXT())); // green text if everything is available, red otherwise

            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void totalStock(ConnectionSource connectionSource) {
        try {
            for (Part part :
                    DaoManager.createDao(connectionSource, Part.class)) {
                System.out.printf("%s: %s%n", colorize(part.name, BOLD()), colorize(String.valueOf(part.quantity), YELLOW_TEXT()));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
