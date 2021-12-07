package com.InventoryManagement.Tables;

import com.InventoryManagement.Tables.IssueDao.*;
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

/**
 * Model for the issue object
 * Fields: id, part, issued_to, quantity, issued_on, return_on
 */
@DatabaseTable(daoClass = IssueDaoImpl.class)
public class Issue {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true,
    columnDefinition = "integer constraint fk_name references part(id) on delete CASCADE")
    public Part part;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true,
    columnDefinition = "integer constraint fk_name references student(id) on delete CASCADE")
    public Student issued_to;

    @DatabaseField(canBeNull = false)
    public Integer quantity;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "dd-MM-yyyy")
    public Date issued_on;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "dd-MM-yyyy")
    public Date return_on;

    Issue() {

    }

    Issue(Part part, Student issued_to, int quantity, Date return_on) {
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

        issue.quantity = io.getInteger("Part quantity to issue: ", r -> r <= issue.part.quantity && r > 0, "Please enter a valid amount to issue");
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
    }
}