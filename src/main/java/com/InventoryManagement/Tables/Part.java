package com.InventoryManagement.Tables;

import com.InventoryManagement.IO;
import com.InventoryManagement.Pair;
import com.InventoryManagement.UsedDynamically;
import com.InventoryManagement.Tables.IssueDao.IssueDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

@DatabaseTable()
public class Part {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public Integer id;

    @DatabaseField(canBeNull = false)
    public Integer quantity;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoCreate = true)
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

    @UsedDynamically
    public static void add(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();
        Part part = new Part();

        part.name = io.getString("Part name: ");
        part.quantity = io.getInteger("Part quantity: ");
        part.category = Category.threeWayAdder(connectionSource);

        part.create(connectionSource);
        System.out.println(colorize("Part successfully added", GREEN_TEXT()));
    }

    public static void remove(ConnectionSource connectionSource) throws SQLException {
        Part part = select(connectionSource);
        
        IO io = new IO();

        if (io.getBoolean("Are you sure you want to delete this part? This will delete related issue requests too") && part != null) {
            IssueDao dao = Issue.getDao(connectionSource);
            dao.delete(dao.queryBuilder().where().eq("part_id", part.id).query());
            getDao(connectionSource).delete(part);
        }
    }

    public static void edit(ConnectionSource connectionSource) throws SQLException {
        Part part = select(connectionSource);

        IO io = new IO();
        System.out.println("Now, enter the new values: ");

        // new values
        part.name = io.getString("Part name");
        part.quantity = io.getInteger("Part quantity: ");
        part.category = Category.threeWayAdder(connectionSource);

        getDao(connectionSource).update(part);
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
