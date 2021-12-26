package com.InventoryManagement.Tables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to represent fields which will be added to the SQL query
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryString {}
