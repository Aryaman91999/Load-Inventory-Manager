package com.InventoryManagement.Tables;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

/*
All tables must implement this interface
contains basic methods needed for any table
*/
public interface Table {
    /**
     * method to add a row to the table
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public void add(ConnectionSource connectionSource) throws SQLException;


    /**
     * method to remove a row from the table
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public void remove(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to edit a row in the table
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public void edit(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to list all rows in tabular or list format
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public void list(ConnectionSource connectionSource) throws SQLException;

    /**
     * method to create the current object
     * @param connectionSource connection to the database
     * @throws SQLException
     */
    public void create(ConnectionSource connectionSource) throws SQLException;
}
