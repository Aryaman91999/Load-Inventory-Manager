package com.InventoryManagement;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    /**
     * Rigorous Test :-)
     */
    @Test
    public void issueList() {
        App.main(new String[] { "-l", "issue" });
    }
}
