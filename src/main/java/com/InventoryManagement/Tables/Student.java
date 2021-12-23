package com.InventoryManagement.Tables;

import com.InventoryManagement.IO;
import com.InventoryManagement.Pair;
import com.InventoryManagement.ValidateEmail;
import com.InventoryManagement.Tables.IssueDao.IssueDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;
import java.util.function.BiFunction;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;
import static com.InventoryManagement.Format.*;

@DatabaseTable()
public class Student implements Table {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @DatabaseField(canBeNull = false)
    public String name;

    @DatabaseField(canBeNull = false, columnName = "class")
    public Integer _class;

    @DatabaseField(canBeNull = false)
    public Integer roll_no;

    @DatabaseField(canBeNull = false)
    public String email;

    public Student() {

    }

    public Student(String name, Integer _class, Integer roll_no, String email) {
        this.name = name;
        this._class = _class;
        this.email = email;
        this.roll_no = roll_no;
    }

    public void add(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();
        Student student = new Student();

        student.name = io.getString("Student name: ");
        student.roll_no = io.getInteger("Student roll no.: ");
        student._class = io.getInteger("Student class: ");
        student.email = io.getString("Student email: ");

        student.create(connectionSource);

        System.out.println(
                colorize("Student successfully added", GREEN_TEXT()));
    }

    public static Dao<Student, Integer> getDao(ConnectionSource connectionSource) throws SQLException {
        return DaoManager.createDao(connectionSource, Student.class);
    }

    public void remove(ConnectionSource connectionSource) throws SQLException {
        Student student = select(connectionSource);
        IO io = new IO();

        if (io.getBoolean("Are you sure you want to delete this student? This will delete related issue requests too ")
                && student != null) {
            IssueDao dao = Issue.getDao(connectionSource);
            dao.delete(dao.queryBuilder().where().eq("issued_to_id", student.id).query());
            getDao(connectionSource).delete(student);
        }
    }

    public void edit(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Edit a student's info%n%n", HEADING));

        Student student = select(connectionSource);

        IO io = new IO();
        System.out.println("Now, enter the new details: ");

        student.name = io.getString("Student name: ");
        student._class = io.getInteger("Student class");
        student.roll_no = io.getInteger("Student roll no.:");
        student.email = io.getString("Student email: ");

        getDao(connectionSource).update(student);

        System.out.printf(colorize("%nSuccessfully edited student info%n", SUCCESS));
    }

    public void list(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("All students%n%n", HEADING));

        Dao<Student, Integer> dao = getDao(connectionSource);
        
        if (dao.countOf() == 0) {
            System.out.println("There are no students in the database");
        }

        System.out.printf("Total %d students%n", dao.countOf());

        // store these lengths so that we don't have to recompute them
        int _id = 2;
        int _name = "name".length();
        int _class = "class".length();
        int _roll_no = "roll no.".length();
        int _email = "Email".length();

        // max lengths for the fields
        // not needed for class at it will be only 2 digits
        int len_id = _id;
        int len_name = _name;
        int len_roll_no = _roll_no;
        int len_email = _email;

        for (Student student : dao) {
            int i = student.id.toString().length();
            if (i > len_id) {
                len_id = i;
            }

            int n = student.name.length();
            if (n > len_name) {
                len_name = n;
            }

            int e = student.email.length();
            if (e > len_email) {
                len_email = e;
            }

            int r = student.roll_no.toString().length();
            if (r > len_roll_no) {
                len_roll_no = r;
            }
        }

        System.out.printf("| ID%s | Name%s | Class%s | Roll No.%s | Email%s |%n", 
            " ".repeat(len_id - _id),
            " ".repeat(len_name - _name),
            " ".repeat(0),
            " ".repeat(len_roll_no - _roll_no),
            " ".repeat(len_email - _email)
        );

        System.out.printf("|-%s-|-%s-|-%s-|-%s-|-%s-|%n", 
            "-".repeat(len_id),
            "-".repeat(len_name),
            "-".repeat(_class),
            "-".repeat(len_roll_no),
            "-".repeat(len_email)
        );

        BiFunction<String, Integer, String> format = (val, max) -> val + " ".repeat(max - val.length());

        for (Student student : dao) {
            System.out.printf("| %s | %s | %s | %s | %s |%n", 
                format.apply(student.id.toString(), len_id),
                format.apply(student.name, len_name),
                format.apply(student._class.toString(), _class),
                format.apply(student.roll_no.toString(), len_roll_no),
                format.apply(student.email, len_email)
            );
        }
    }

    public void create(ConnectionSource connectionSource) throws SQLException {
        getDao(connectionSource).create(this);
    }

    public static Student threeWayAdder(ConnectionSource connectionSource) throws SQLException {
        Pair<Student, String> p = _select(connectionSource);

        if (p.isNull()) {
            IO io = new IO();
            if (io.getBoolean(
                    String.format("No students found for %s. Would you like to add this student? ", p.getSecond()))) {
                int _class = io.getInteger("Student class: ");
                int roll_no = io.getInteger("Student roll no.: ");
                ValidateEmail v = new ValidateEmail();
                String email = io.getString("Student email: ", v::validate);

                Student student = new Student(p.getSecond(), _class, roll_no, email);
                student.create(connectionSource);
                return student;
            } else {
                return null;
            }
        }

        return p.getFirst();
    }

    public String print() {
        return String.format("%s of class %d (roll no %d)", this.name, this._class, this.roll_no);
    }

    public static Student select(ConnectionSource connectionSource) throws SQLException {
        return _select(connectionSource).getFirst();
    }

    private static Pair<Student, String> _select(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();

        Dao<Student, Integer> dao = getDao(connectionSource);

        String name = io.getString("Student name: ");

        // where name = %name%
        // % for wildcard
        List<Student> students = dao.queryBuilder().where().like("name", "%" + name).query();

        if (students.size() == 0) {
            return new Pair<Student, String>(null, name);
        } else if (students.size() > 1) {
            System.out.printf("Multiple students found for \"%s\"", name);

            int i = 1;

            for (Student student : students) {
                System.out.printf("%d. %s%n", i, student.print());
            }

            // get integer with validation of range
            int idx = io.getInteger("Who did you mean?",
                    (r) -> r > 0 && r >= students.size() + 1, "Number must be in the range of options.");

            return new Pair<Student, String>(students.get(idx - 1), name);
        } else {
            return new Pair<Student, String>(students.get(0), name);
        }
    }
}