package com.platform.database;

/**
 * *****************************************************
 * Project: Tnomkrad
 * Date: 15/05/2018
 * File: ${CLASS}
 * Version: ${EDITS}
 * *****************************************************
 * Description:
 * *****************************************************
 */
public interface DBConfiguration {
    boolean prepareCollections();
    boolean testConnection(String hostname,String port);
    boolean prepareDataBase();
}
