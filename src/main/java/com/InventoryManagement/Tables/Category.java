package com.InventoryManagement.Tables;

import com.InventoryManagement.Filter;
import com.InventoryManagement.IO;
import com.InventoryManagement.Pair;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;
import static com.diogonunes.jcolor.Ansi.colorize;
import static com.InventoryManagement.Format.*;

@DatabaseTable()
public class Category extends Table {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @Alias(aliases = {"category"})
    @DatabaseField(canBeNull = false, unique = true)
    public String name;

    public Category() {

    }

    public Category(String name) {
        this.name = name;
    }

    public Dao<Category, Integer> getDao(ConnectionSource connectionSource) throws SQLException {
        return DaoManager.createDao(connectionSource, Category.class);
    }

    public void create(ConnectionSource connectionSource) throws SQLException {
        DaoManager.createDao(connectionSource, Category.class).create(this);
    }

    public void add(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Add a category%n",  HEADING));

        new Category(new IO().getString("Category Name: ")).create(connectionSource);

        System.out.printf(colorize("%nCategory successfully added%n",  SUCCESS));
    }

    public void remove(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Remove a category%n%n", HEADING));

        Category cat = select(connectionSource);
        
        IO io = new IO();

        if (io.getBoolean("Are you sure you want to delete this category? ") && cat != null) {
            getDao(connectionSource).delete(cat);
        }

        System.out.printf(colorize("%nCategory successfully removed%n", SUCCESS));
    }

    public static Category select(ConnectionSource connectionSource) throws SQLException {
        return _select(connectionSource).getFirst();
    }


    /**
     * Method to select a category
     * if multiple, ask the user to select
     * if one then select
     * if not found then return null
     * @param connectionSource
     * @return the selected object, null if none found
     * @throws SQLException
     */
    private static Pair<Category, String> _select(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();
        String name = io.getString("Category: ");

        Dao<Category, Integer> dao = new Category().getDao(connectionSource);

        List<Category> categories = dao.queryBuilder().where().like("name", "%" + name + "%").query();
        if (categories.size() > 1) {
            System.out.printf("Multiple categories found for \"%s\": %n", name);
            int i = 1;

            for (Category item : categories) {
                System.out.printf("%d. %s%n", i, item.name);
                i++;
            }

            int num;

            do {
                num = io.getInteger("Which one did you mean? (number) ");
            } while (!(num > 0) || !(num <= categories.size() + 1));

            return new Pair<Category, String>(categories.get(num - 1), name);
        } else if (categories.size() == 0) {
            return new Pair<Category, String>(null, name);
        } else {
            return new Pair<Category, String>(categories.get(0), name);
        }
    }

    /**
     * Method to either create or select a category
     * take a category from the user:
     * if it does not exist create it
     * if there are multiple matching the same, ask user to select
     * if their is only one then select it
     * @param connectionSource connection to the database
     * @return instance which was created
     * @throws SQLException
     */
    public static Category threeWayAdder(ConnectionSource connectionSource) throws SQLException {
        Pair<Category, String> p = _select(connectionSource);

        if (p.isNull()) {
            Category c = new Category(p.getSecond());
            c.create(connectionSource);
            return c;
        }

        return p.getFirst();
    }

    public void edit(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Edit a category %n%n", HEADING));
        
        IO io = new IO();

        System.out.println("Enter old data:");
        Category cat = select(connectionSource);

        cat.name = io.getString("New category name: ");
        getDao(connectionSource).update(cat);

        System.out.printf(colorize("%nSuccessfully edited%n", SUCCESS));
    }

    public void list(ConnectionSource connectionSource) throws SQLException {
        Filter.list(getDao(connectionSource).queryForAll());  
    }

    @Override
    public void filter(ConnectionSource connectionSource) throws SQLException {
        System.out.printf(colorize("Filter categories%n%n", HEADING));
        IO io = new IO();

        String cat = io.getString("Category Name: ");
        Filter.list(getDao(connectionSource).queryBuilder().where().like("name", "%" + cat + "%").query());
    }
}
