package com.InventoryManagement.Tables.IssueDao;

import com.InventoryManagement.Tables.Issue;
import com.InventoryManagement.Tables.Part;
import com.InventoryManagement.Tables.Student;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Custom DAO for Issues
 * Custom methods: queryForPartEq, queryForStudentEq
 */
public interface IssueDao extends Dao<Issue, Integer> {
    /**
     * Query for equality of part
     * @param part Part to check equality against
     * @return List of all results
     * @throws SQLException Default ORMLite exception
     */
    List<Issue> queryForPartEq(Part part) throws SQLException;


    /**
     * Query for equality of student
     * @param student Student to check equality againsts
     * @return List of all results
     * @throws SQLException Default ORMLite exception
     */
    List<Issue> queryForStudentEq(Student student) throws SQLException;
}
