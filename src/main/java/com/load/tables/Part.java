package com.load.tables;

import com.load.IO;
import com.load.ListObject;
import com.load.Pair;
import com.load.tables.issuedao.IssueDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import static com.load.Format.*;
import static com.diogonunes.jcolor.Ansi.colorize;

@DatabaseTable()
public class Part extends Table {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;
    
    @CsvBindByName
    @DatabaseField(canBeNull = false, unique = true)
    public String name;
    
    @CsvBindByName
    @DatabaseField(canBeNull = false)
    public Integer quantity;

    @CsvRecurse
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    public Category category;


    public Part() {

    }

    public Part(int quantity, Category category, String name) {
        this.quantity = quantity;
        this.category = category;
        this.name = name;
    }

    public void add(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Add a part%n%n", HEADING));

        IO io = new IO();
        Part part = new Part();

        part.name = io.getString("Part name: ");
        part.quantity = io.getInteger("Part quantity: ");
        part.category = Category.threeWayAdder(connectionSource);

        part.create(connectionSource);

        System.out.println(colorize("%nPart successfully added%n", SUCCESS));
    }

    public void remove(ConnectionSource connectionSource) throws SQLException {
        System.out.println(colorize("Remove a part", HEADING));

        Part part = select(connectionSource);
        
        IO io = new IO();

        if (io.getBoolean("Are you sure you want to delete this part? This will delete related issue requests too") && part != null) {
            IssueDao dao = new Issue().getDao(connectionSource);
            dao.delete(dao.queryBuilder().where().eq("part_id", part.id).query());
            getDao(connectionSource).delete(part);
        }

        System.out.printf(colorize("%nPart successfully removed%n"));
    }

    public void edit(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Edit a part%n%n", HEADING));

        Part part = select(connectionSource);

        IO io = new IO();
        System.out.println("Now, enter the new values: ");

        // new values
        part.name = io.getString("Part name");
        part.quantity = io.getInteger("Part quantity: ");
        part.category = Category.threeWayAdder(connectionSource);

        getDao(connectionSource).update(part);

        System.out.printf(colorize("%nPart successfully edited%n", SUCCESS));
    }

    public void list(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("All parts%n%n", HEADING));

        Dao<Part, Integer> dao = getDao(connectionSource);
        
        if (dao.countOf() == 0) {
            System.out.println("There are no parts in the database");
            return;
        }

        System.out.printf("Total %d parts", dao.countOf());
        
        ListObject.list(dao.queryForAll());
    }

    public Dao<Part, Integer> getDao(ConnectionSource connectionSource) throws SQLException {
        return DaoManager.createDao(connectionSource, Part.class);
    }

    public void create(ConnectionSource connectionSource) throws SQLException {
        getDao(connectionSource).create(this); 
    }

    public static Part threeWayAdder(ConnectionSource connectionSource) throws SQLException {
        Pair<Part, String> p = _select(connectionSource);

        if (p.isNull()) {
            IO io = new IO();
            if (io.getBoolean(String.format("No results found for %s. Would you like to add this part? ", p.getSecond()))) {
                int quantity = io.getInteger("Part quantity: ");
                Category category = Category.threeWayAdder(connectionSource);

                Part part = new Part(quantity, category, p.getSecond());
                part.create(connectionSource);
                return part;
            } else {
                return null;
            }
        }

        return p.getFirst();
    }

    public String print() {
        return this.name;
    }

    public static Part select(ConnectionSource connectionSource) throws SQLException {
        return _select(connectionSource).getFirst();
    }

    private static Pair<Part, String> _select(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();

        Dao<Part, Integer> dao = new Part().getDao(connectionSource);

        String name = io.getString("Part name: ");

        // where name = %name%
        // % for wildcard
        List<Part> parts = dao.queryBuilder().where().like("name", "%" + name).query();

        if (parts.size() == 0) {
            return new Pair<Part, String>(null, name);
        } else if (parts.size() > 1) {
            System.out.printf("Multiple results found for \"%s\"", name);
            
            int i  = 1;

            for (Part part : parts) {
                System.out.printf("%d. %s%n", i, part.print());
            }

            // get integer with validation of range
            int idx = io.getInteger("Which one did you mean?",
                    (r) ->  r > 0 && r >= parts.size() + 1, "Number must be in the range of options.");

            return new Pair<Part, String>(parts.get(idx-1), name);
        } else {
            return new Pair<Part, String>(parts.get(0), name);
        }
    }

    @Override
    public void filter(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Filter parts%n%n", HEADING));
        System.out.println("Enter \"*\" for no filtering on a field");

        int where_num = 0;
        Where<Part, Integer> where = getDao(connectionSource).queryBuilder().where();

        IO io = new IO();

        String name = io.getString("Part name: ");
        if (!name.equals("*")) {
            where.like("name", name);
        }

        Integer quantity = io.getInteger("Part quantity (-1 for no filtering): ", null, null);
        if (quantity != -1) {
            if (where_num >= 1) {
                where.and();
            }
            where.eq("quantity", quantity);
            where_num++;
        }

        String category = io.getString("Category: ");
        if (!category.equals("*")) {
            if (where_num >= 1) {
                where.and();
            }
            where.in("category_id", new Category().getDao(connectionSource).queryBuilder().where().like("name", "%" + category + "%").query());
            where_num++;
        }

        if (where_num == 0) {
            list(connectionSource);
            return;
        }

        List<Part> res = where.query();
        System.out.printf("Total %d results%n", res.size());
        ListObject.list(res);
    }

    @Override
    public void load(ConnectionSource connectionSource, String csv) throws NoSuchMethodException, SQLException {
        try {
            Reader reader = new BufferedReader(new FileReader(csv));
            CsvToBean<Part> csvToBean = new CsvToBeanBuilder<Part>(reader)
            .withType(Part.class)
            .withSeparator(',')
            .withIgnoreLeadingWhiteSpace(true)
            .withIgnoreEmptyLine(true)
            .build();

            List<Part> parts = csvToBean.parse();

            for (Part part : parts) {
                Category cat = new Category().getDao(connectionSource).queryBuilder().where().like("name", part.category.name).queryForFirst();
                if (cat != null) {
                    part.category = cat;
                }

                System.out.printf(colorize("Adding part %s", SUCCESS), part.name);
                part.create(connectionSource);
            }
        } catch (FileNotFoundException e) {
            System.out.println(colorize("Could not find file because it does not exist", ERROR));
        }
    }
}
