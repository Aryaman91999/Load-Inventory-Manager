package com.load.tables;

import com.load.AsDate;
import com.load.IO;
import com.load.ListObject;
import com.load.tables.issuedao.*;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.load.Format.*;
import static com.diogonunes.jcolor.Ansi.colorize;

/**
 * Model for the issue object
 * Fields: id, part, issued_to, quantity, issued_on, return_on
 */
@DatabaseTable(daoClass = IssueDaoImpl.class)
public class Issue extends Table {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "integer constraint fk_name references part(id) on delete CASCADE")
    public Part part;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "integer constraint fk_name references student(id) on delete CASCADE")
    public Student issued_to;

    @DatabaseField(canBeNull = false)
    public Integer quantity;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    public Date issued_on;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    public Date return_on;

    @DatabaseField(persisted = false, useGetSet = true)
    public static String returned = "date(return_on) > date('now','localtime')";

    public String getReturned() {
        return this.return_on.after(AsDate.asDate(LocalDate.now())) ? "Yes" : "No";
    }

    public void setReturned(String returned) throws AccessDeniedException {
        throw new AccessDeniedException("You cannot set returned.");
    }

    public Issue() {

    }

    public Issue(Part part, Student issued_to, int quantity, Date return_on) {
        this.part = part;
        this.issued_to = issued_to;
        this.quantity = quantity;
        this.return_on = return_on;
    }

    public void create(ConnectionSource connectionSource) throws SQLException {
        getDao(connectionSource).create(this);
    }

    public void remove(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Remove an issue%n%n", HEADING));

        Issue issue = select(connectionSource);
        IO io = new IO();
        if (io.getBoolean("Are you sure you want to remove this issue? ") && issue != null) {
            getDao(connectionSource).delete(issue);
        }

        System.out.printf(colorize("%nIssue removed successfully%n", SUCCESS));
    }

    public void edit(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Edit an issue%n%n", HEADING));

        System.out.println("First, enter the current values of the issue you want to edit: ");
        Issue issue = select(connectionSource);

        System.out.println("Now, enter the new values: ");

        IO io = new IO();

        System.out.printf("Issue to:%n%n");
        issue.issued_to = Student.threeWayAdder(connectionSource);
        issue.part = Part.threeWayAdder(connectionSource);
        issue.quantity = io.getInteger("Part quantity to issue: ");
        issue.issued_on = io.getDate("Issued on (dd-MM-yyyy): ");
        issue.return_on = io.getDate("Return on (dd-MM-yyyy): ");

        getDao(connectionSource).update(issue);

        System.out.printf(colorize("%nIssue edited successfully%n", SUCCESS));
    }

    public IssueDao getDao(ConnectionSource connectionSource) throws SQLException {
        return DaoManager.createDao(connectionSource, Issue.class);
    }

    public static Issue select(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();
        int part_id = Part.select(connectionSource).id;

        System.out.printf("Issue to:%n%n");
        int student_id = Student.select(connectionSource).id;
        int quantity = io.getInteger("Quantity: ");

        Date issued_on = io.getDate("Issued On: (dd-MM-yyyy): ");

        IssueDao dao = new Issue().getDao(connectionSource);
        Where<Issue, Integer> where = dao.queryBuilder().where();

        // equality checks for all the values
        where.eq("part_id", part_id);
        where.eq("issued_to_id", student_id);
        where.eq("quantity", quantity);
        where.eq("issued_on", issued_on);
        where.and(4);

        return where.queryForFirst();
    }

    public void add(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Add an issue%n%n", HEADING));

        Issue issue = new Issue();
        IO io = new IO();

        System.out.printf("Issue to:%n%n");
        issue.issued_to = Student.threeWayAdder(connectionSource);
        if (issue.issued_to == null) {
            return;
        }

        issue.part = Part.threeWayAdder(connectionSource);
        if (issue.part == null) {
            return;
        }

        int total_issued = 0;

        for (Issue i : getDao(connectionSource).queryBuilder().where().gt("return_on", AsDate.asDate(LocalDate.now()))
                .query()) {
            total_issued += i.quantity;
        }

        int available = issue.part.quantity - total_issued;

        issue.quantity = io.getInteger("Part quantity to issue: ", r -> r <= available && r > 0,
                "Please enter a valid amount to issue");

        issue.return_on = io.getDate("Return on (dd-MM-yyyy): ");

        // convert current local date to date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate currentDate = LocalDate.now();
        currentDate.format(dtf);

        issue.issued_on = AsDate.asDate(currentDate);

        issue.create(connectionSource);

        System.out.printf(colorize("%nIssue successfully added%n", SUCCESS));
    }

    public void list(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("All issue requests%n%n", HEADING));

        IssueDao dao = getDao(connectionSource);

        if (dao.countOf() == 0) {
            System.out.println("No issue requests in database.");
            return;
        }

        System.out.printf("%d total issue requests, %d active.%n", dao.countOf(),
                dao.queryBuilder().where().gt("return_on", AsDate.asDate(LocalDate.now())).countOf());

        ListObject.list(dao.queryForAll());
    }

    @Override
    public void filter(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Filter issues%n%n", HEADING));
        System.out.println("Enter \"*\" for no filtering on a field");

        int where_num = 0;

        Where<Issue, Integer> where = getDao(connectionSource).queryBuilder().where();

        IO io = new IO();
        String part = io.getString("Part name: ");
        if (!part.equals("*")) {
            where.in("part_id",
                    new Part().getDao(connectionSource).queryBuilder().where().like("name", "%" + part + "%").query());
            where_num++;
        }

        String student = io.getString("Student name: ");
        if (!student.equals("*")) {
            if (where_num >= 1) {
                where.and();
            }
            where.in("issued_to_id",
                    new Student().getDao(connectionSource).queryBuilder().where().like("name", "%" + student + "%"));
            where_num++;
        }

        int quantity = io.getInteger("Quantity issued (-1 for no filtering): ", null, null);
        if (quantity != -1) {
            if (where_num >= 1) {
                where.and();
            }
            where.eq("quantity", quantity);
            where_num++;
        }

        String date = io.getString("Issued On: ");
        if (!date.equals("*")) {
            if (where_num >= 1) {
                where.and();
            }
            where.raw("date(issued_on) = date(" + AsDate.toString(AsDate.asDate(date, "yyyy-MM-dd"), "yyyy-MM-dd") + ")");
            where_num++;
        }

        String ret = io.getString("Return On: ");
        if (!ret.equals("*")) {
            if (where_num >= 1) {
                where.and();
            }
            where.raw("date(return_on) = date(" + AsDate.toString(AsDate.asDate(ret), "yyyy-MM-dd") + ")");
            where_num++;
        }

        String returned = io.getString("Returned? (y/n) ",
                s -> Arrays.asList("y", "yes", "n", "no", "*").contains(s.toLowerCase()) // lazy way of checking if input is
                                                                                    // valid
        ).toLowerCase();
        if (!returned.equals("*")) {
            if (where_num >= 1) {
                where.and();
            }
            if (Arrays.asList("y", "yes").contains(returned)) {
                where.raw(Issue.returned);
            } else {
                where.raw("NOT " + Issue.returned);
            }
            where_num++;
        }

        if (where_num == 0) {
            list(connectionSource);
            return;
        }

        List<Issue> res = where.query();
        System.out.printf("Total %d results%n");
        ListObject.list(res);
    }
}