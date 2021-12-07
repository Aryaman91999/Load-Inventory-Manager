package com.InventoryManagement.Tables;

import com.InventoryManagement.IO;
import com.InventoryManagement.Pair;
import com.InventoryManagement.UsedDynamically;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable()
public class Category {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @DatabaseField(canBeNull = false, unique = true)
    public String name;

    public Category() {

    }

    public Category(String name) {
        this.name = name;
    }

    public void create(ConnectionSource connectionSource) throws SQLException {
        DaoManager.createDao(connectionSource, Category.class).create(this);
    }

    @UsedDynamically
    public static void add(ConnectionSource connectionSource) throws SQLException {
        new Category(new IO().getString("Category Name: ")).create(connectionSource);
    }

    public static void remove(ConnectionSource connectionSource) throws SQLException {
        Category cat = select(connectionSource);
        
        IO io = new IO();

        if (io.getBoolean("Are you sure you want to delete this category? ") && cat != null) {
            DaoManager.createDao(connectionSource, Category.class).delete(cat);
        }
    }

    public static Category select(ConnectionSource connectionSource) throws SQLException {
        return _select(connectionSource).getFirst();
    }

    private static Pair<Category, String> _select(ConnectionSource connectionSource) throws SQLException {
        IO io = new IO();
        String name = io.getString("Category: ");

        Dao<Category, Integer> dao = DaoManager.createDao(connectionSource, Category.class);

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

    public static Category threeWayAdder(ConnectionSource connectionSource) throws SQLException {
        Pair<Category, String> p = _select(connectionSource);

        if (p.isNull()) {
            Category c = new Category(p.getSecond());
            c.create(connectionSource);
            return c;
        }

        return p.getFirst();
    }
}
