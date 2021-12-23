package com.InventoryManagement.Tables;

import com.InventoryManagement.IO;
import com.InventoryManagement.Pair;
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
import static com.InventoryManagement.Format.*;

@DatabaseTable()
public class Part implements Table {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @DatabaseField(canBeNull = false)
    public Integer quantity;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    public Category category;

    @DatabaseField(canBeNull = false, unique = true)
    public String name;

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
            IssueDao dao = Issue.getDao(connectionSource);
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
        }

        System.out.printf("Total %d parts", dao.countOf());

        int _name = "name".length();
        int _quantity = "quantity".length();
        int _category = "category".length();
        int _id = 2;

        int len_name = "name".length();
        int len_quantity = "quantity".length();
        int len_category = "category".length();
        int len_id = 2;


        for (Part part : dao) {
            int n = part.name.length();
            if (len_name < n) {
                len_name = n;
            }

            int q = part.quantity.toString().length();
            if (len_quantity < q) {
                len_quantity = q;
            }

            int c = part.category.name.length();
            if (len_category < c) {
                len_category = c;
            }

            int i = part.id.toString().length();
            if (len_id < i) {
                len_id = i;
            }
        }

        BiFunction<String, Integer, String> format = (val, max) -> val + " ".repeat(max - val.length());

        System.out.printf("| ID%s | Name%s | Quantity%s | Category%s |%n", 
            " ".repeat(len_id - _id),
            " ".repeat(len_name - _name),
            " ".repeat(len_quantity - _quantity),
            " ".repeat(len_category - _category)
        );

        System.out.printf("|-%s-|-%s-|-%s-|-%s-|%n", "-".repeat(len_id), "-".repeat(len_name), "-".repeat(len_quantity), "-".repeat(len_category));

        for (Part part : dao) {
            System.out.printf("| %s | %s | %s | %s |%n",
                format.apply(part.id.toString(), len_id),
                format.apply(part.name, len_name),
                format.apply(part.quantity.toString(), len_quantity),
                format.apply(part.category.name, len_category)
            );
        }
    }

    private static Dao<Part, Integer> getDao(ConnectionSource connectionSource) throws SQLException {
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

        Dao<Part, Integer> dao = getDao(connectionSource);

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
}
