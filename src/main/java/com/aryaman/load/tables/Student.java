package com.aryaman.load.tables;

import com.aryaman.load.IO;
import com.aryaman.load.ListObject;
import com.aryaman.load.Pair;
import com.aryaman.load.ValidateEmail;
import com.aryaman.load.Format;
import com.aryaman.load.tables.issuedao.IssueDao;
import com.diogonunes.jcolor.Ansi;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

@DatabaseTable()
public class Student extends Table {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @CsvBindByName
    @DatabaseField(canBeNull = false)
    public String name;

    @CsvBindByName(column = "class")
    @DatabaseField(canBeNull = false, columnName = "class")
    public Integer _class;

    @CsvBindByName
    @DatabaseField(canBeNull = false)
    public Integer roll_no;

    @CsvBindByName
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
        System.out.printf(colorize("Add a student to the database%n%n", Format.HEADING));

        IO io = new IO();
        Student student = new Student();

        student.name = io.getString("Student name: ");
        student.roll_no = io.getInteger("Student roll no.: ");
        student._class = io.getInteger("Student class: ");
        student.email = io.getString("Student email: ", new ValidateEmail()::validate);

        student.create(connectionSource);

        System.out.printf(
                colorize("%nStudent successfully added%n", GREEN_TEXT()));
    }

    public Dao<Student, Integer> getDao(ConnectionSource connectionSource) throws SQLException {
        return DaoManager.createDao(connectionSource, Student.class);
    }

    public void remove(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(Ansi.colorize("Remove a student from the database%n%n", Format.HEADING));

        Student student = select(connectionSource);

        if (student == null) {
            System.out.println("Student not found");
            return;
        }

        IO io = new IO();

        if (io.getBoolean("Are you sure you want to delete this student? This will delete related issue requests too ")
                && student != null) {
            IssueDao dao = new Issue().getDao(connectionSource);
            dao.delete(dao.queryBuilder().where().eq("issued_to_id", student.id).query());
            getDao(connectionSource).delete(student);
        }

        System.out.printf(Ansi.colorize("%nPart successfully removed%n", Format.SUCCESS));
    }

    public void edit(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(Ansi.colorize("Edit a student's info%n%n", Format.HEADING));

        Student student = select(connectionSource);

        if (student == null) {
            System.out.println("Student not found");
            return;
        }

        IO io = new IO();
        System.out.println("Now, enter the new details: ");

        student.name = io.getString("Student name: ");
        student._class = io.getInteger("Student class");
        student.roll_no = io.getInteger("Student roll no.:");
        student.email = io.getString("Student email: ", new ValidateEmail()::validate);

        getDao(connectionSource).update(student);

        System.out.printf(Ansi.colorize("%nSuccessfully edited student info%n", Format.SUCCESS));
    }

    public void list(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(Ansi.colorize("All students%n%n", Format.HEADING));

        Dao<Student, Integer> dao = getDao(connectionSource);

        if (dao.countOf() == 0) {
            System.out.println("There are no students in the database");
            return;
        }

        System.out.printf("Total %d students%n", dao.countOf());

        ListObject.list(dao.queryForAll());
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

        Dao<Student, Integer> dao = new Student().getDao(connectionSource);

        String name = io.getString("Student name: ");

        // where name = %name%
        // % for wildcard
        List<Student> students = dao.queryBuilder().where().like("name", "%" + name + "%").query();

        if (students.size() == 0) {
            return new Pair<Student, String>(null, name);
        } else if (students.size() > 1) {
            System.out.printf("Multiple students found for \"%s\": %n", name);

            int i = 1;

            for (Student student : students) {
                System.out.printf("%d. %s%n", i, student.print());
                i++;
            }

            // get integer with validation of range
            int idx = io.getInteger("Who did you mean?",
                    (r) -> r > 0 && r <= students.size(), "Number must be in the range of options.");

            return new Pair<Student, String>(students.get(idx - 1), name);
        } else {
            return new Pair<Student, String>(students.get(0), name);
        }
    }

    @Override
    public void filter(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(Ansi.colorize("Filter students%n%n", Format.HEADING));
        System.out.println("Enter \"*\" for no filtering on a field");

        int where_num = 0;
        Where<Student, Integer> where = getDao(connectionSource).queryBuilder().where();
        IO io = new IO();

        String name = io.getString("Student name: ");
        if (!name.equals("*")) {
            where.like("name", "%" + name + "%");
            where_num++;
        }

        int _class = io.getInteger("Student class (-1 for no filtering): ", null, null);
        if (_class != -1) {
            if (where_num >= 1) {
                where.and();
            }

            where.eq("class", _class);
            where_num++;
        }

        int roll_no = io.getInteger("Student Roll No. (-1 for no filtering): ", null, null);
        if (roll_no != -1) {
            if (where_num >= 1) {
                where.and();
            }

            where.eq("roll_no", roll_no);
            where_num++;
        }

        String email = io.getString("Student Email: ");
        if (!email.equals("*")) {
            if (!new ValidateEmail().validate(email)) {
                System.out.println(Ansi.colorize("Invalid error", Format.ERROR));
                return;
            }

            if (where_num >= 1) {
                where.and();
            }

            where.eq("email", email);
            where_num++;
        }

        if (where_num == 0) {
            list(connectionSource);
        }

        List<Student> res = where.query();
        System.out.printf("total %d results%n", res.size());
        ListObject.list(res);
    }

    @Override
    public void load(ConnectionSource connectionSource, String csv) throws NoSuchMethodException, SQLException {
        try {
            Reader reader = new BufferedReader(new FileReader(csv));
            CsvToBean<Student> csvToBean = new CsvToBeanBuilder<Student>(reader)
            .withType(Student.class)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build();

            List<Student> students = csvToBean.parse();

            for (Student student : students) {
                System.out.printf(Ansi.colorize("Adding student %s", Format.SUCCESS), student.name);
                student.create(connectionSource);
            }
        } catch (FileNotFoundException e) {
            System.out.println(Ansi.colorize("Could not find file because it does not exist", Format.ERROR));
        }
    }
}