package com.aryaman.load.tables;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

/*
All tables must extend this class
contains basic methods needed for any table
*/
public abstract class Table {
    /**
     * Empty constructor for ORMLite
     */
    public Table() {

    };

    /**
     * method to add a row to the table
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public abstract void add(ConnectionSource connectionSource) throws SQLException;


    /**
     * method to remove a row from the table
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public abstract void remove(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to edit a row in the table
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public abstract void edit(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to list all rows in tabular or list format
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public abstract void list(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to create the current object
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public abstract void create(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to filter the current objet
     * @param connectionSource connect to the database
     * @throws SQLException
     */
    public abstract void filter(ConnectionSource connectionSource) throws SQLException;

    /**
     * get the ORMLite dao for the object
     * @param connectionSource connection to the database
     * @return
     * @throws SQLException
     */
    public abstract Dao<? extends Table, Integer> getDao(ConnectionSource connectionSource) throws SQLException;

    public void load(ConnectionSource connectionSource, String csv) throws NoSuchMethodException, SQLException {
        throw new NoSuchMethodException("Load cannot be used on this object");
    }
}
