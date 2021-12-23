package com.InventoryManagement.Tables;

import com.InventoryManagement.Tables.IssueDao.*;
import com.diogonunes.jcolor.Attribute;
import com.InventoryManagement.AsDate;
import com.InventoryManagement.IO;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

/**
 * Model for the issue object
 * Fields: id, part, issued_to, quantity, issued_on, return_on
 */
@DatabaseTable(daoClass = IssueDaoImpl.class)
public class Issue {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "integer constraint fk_name references part(id) on delete CASCADE")
    public Part part;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnDefinition = "integer constraint fk_name references student(id) on delete CASCADE")
    public Student issued_to;

    @DatabaseField(canBeNull = false)
    public Integer quantity;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "dd-MM-yyyy")
    public Date issued_on;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "dd-MM-yyyy")
    public Date return_on;

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
        Issue issue = select(connectionSource);
        IO io = new IO();
        if (io.getBoolean("Are you sure you want to remove this issue? ") && issue != null) {
            getDao(connectionSource).delete(issue);
        }
    }

    public void edit(ConnectionSource connectionSource) throws SQLException {
        System.out.println("First, enter the current values of the issue you want to edit: ");
        Issue issue = select(connectionSource);

        System.out.println("Now, enter the new values: ");

        IO io = new IO();

        issue.issued_to = Student.threeWayAdder(connectionSource);
        issue.part = Part.threeWayAdder(connectionSource);
        issue.quantity = io.getInteger("Part quantity to issue: ");
        issue.issued_on = io.getDate("Issued on (dd-MM-yyyy): ");
        issue.return_on = io.getDate("Return on (dd-MM-yyyy): ");

        getDao(connectionSource).update(issue);
    }

    public static IssueDao getDao(ConnectionSource connectionSource) throws SQLException {
        return DaoManager.createDao(connectionSource, Issue.class);
    }

    public static Issue select(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();
        int part_id = Part.select(connectionSource).id;
        int student_id = Student.select(connectionSource).id;
        int quantity = io.getInteger("Quantity: ");

        Date issued_on = io.getDate("Issued On: (dd-MM-yyyy): ");

        IssueDao dao = getDao(connectionSource);
        Where<Issue, Integer> where = dao.queryBuilder().where();

        // equality checks for all the values
        where.eq("part_id", part_id);
        where.eq("issued_to_id", student_id);
        where.eq("quantity", quantity);
        where.eq("issued_on", issued_on);
        where.and(4);

        return where.queryForFirst();
    }

    public static void add(ConnectionSource connectionSource) throws SQLException {
        Issue issue = new Issue();
        IO io = new IO();

        issue.part = Part.threeWayAdder(connectionSource);
        if (issue.part == null) {
            return;
        }

        issue.quantity = io.getInteger("Part quantity to issue: ", r -> r <= issue.part.quantity && r > 0,
                "Please enter a valid amount to issue");
        issue.issued_to = Student.threeWayAdder(connectionSource);
        if (issue.issued_to == null) {
            return;
        }

        issue.return_on = io.getDate("Return on: ");

        // convert current local date to date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate currentDate = LocalDate.now();
        currentDate.format(dtf);

        issue.issued_on = AsDate.asDate(currentDate);

        issue.create(connectionSource);

        System.out.println(colorize("Issue successfully added", GREEN_TEXT()));
    }

    public void list(ConnectionSource connectionSource) throws SQLException {
        // print the list of all the issues in tabular format

        int len_part = "part".length(); // not 0 because we need space for the headers of the table
        int len_quantity = "quantity".length();
        int len_student = "issued to".length();
        int len_id = 2;

        // format = dd-MM-yyyy
        final int len_date = 10;

        // calculate the maximum lengths for each field
        for (Issue issue : getDao(connectionSource)) {
            int part = issue.part.name.length();
            if (part > len_part) {
                len_part = part;
            }

            int quan = issue.quantity.toString().length();
            if (quan > len_quantity) {
                len_quantity = quan;
            }

            int student = issue.issued_to.name.length();
            if (student > len_student) {
                len_student = student;
            }

            int id = issue.id.toString().length();
            if (id > len_id) {
                len_id = id;
            }
        }

        System.out.printf("| ID%s | Part%s | Issued to%s | Quantity%s | Issued On%s | Return On%s |%n",
                " ".repeat(len_id - 2),
                " ".repeat(len_part - "Part".length()),
                " ".repeat(len_student - "Issued to".length()),
                " ".repeat(len_quantity - "Quantity".length()),
                " ".repeat(len_date - "Issued On".length()),
                " ".repeat(len_date - "Return On".length()));

        System.out.printf("|-%s-|-%s-|-%s-|-%s-|-%s-|-%s-|%n", "-".repeat(len_id), "-".repeat(len_part), "-".repeat(len_student), "-".repeat(len_quantity), "-".repeat(len_date), "-".repeat(len_date));

        for (Issue issue : getDao(connectionSource).queryBuilder().orderBy("id", true).query()) {
            String id = issue.id.toString();
            String student = issue.issued_to.name;
            String part = issue.part.name;
            String quantity = issue.quantity.toString();
            String issued_on = AsDate.toString(issue.issued_on);
            String return_on = AsDate.toString(issue.return_on);
        

            System.out.printf("| %s | %s | %s | %s | %s | %s |%n", 
                id + " ".repeat(len_id - id.length()),
                part + " ".repeat(len_part - part.length()),
                student + " ".repeat(len_student - student.length()),
                quantity + " ".repeat(len_quantity - quantity.length()),
                issued_on + " ".repeat(len_date - issued_on.length()),
                return_on + " ".repeat(len_date - return_on.length())
            );

        }
    }
}