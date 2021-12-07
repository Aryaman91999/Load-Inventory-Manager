package com.InventoryManagement.Tables.IssueDao;

import com.InventoryManagement.Tables.Issue;
import com.InventoryManagement.Tables.Part;
import com.InventoryManagement.Tables.Student;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of the custom issue DAO
 */
public class IssueDaoImpl extends BaseDaoImpl<Issue, Integer> implements IssueDao {
    public IssueDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Issue.class);
    }

    @Override
    public List<Issue> queryForPartEq(Part part) throws SQLException {
        return this.queryBuilder().where().eq("part_id", part.id).query();
    }

    @Override
    public List<Issue> queryForStudentEq(Student student) throws SQLException {
        return this.queryBuilder().where().eq("issued_to_id", student.id).query();
    }


}
