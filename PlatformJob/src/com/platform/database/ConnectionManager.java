package com.platform.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.platform.util.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 15/05/2018
 * File: ${CLASS}
 * Version: ${EDITS}
 * *****************************************************
 * Description:
 * *****************************************************
 */
public class ConnectionManager implements DBConfiguration{
    private static Mongo dbConn;
    private Configuration cfg;


    @Override
    public boolean prepareCollections() {
        return false;
    }

    @Override
    public boolean testConnection(String hostname, String port) {
        return false;
    }

    @Override
    public boolean prepareDataBase() {

        try (MongoClient mongoClient = new MongoClient("127.0.0.1", 27017)) {

            MongoDatabase db = mongoClient.getDatabase("TheFloow");

            List<String> collectionNames = new ArrayList<>();
            db.listCollectionNames().forEach((Consumer<String>)collectionNames::add);
            if(!collectionNames.contains("Dictionary"))
                db.createCollection("Dictionary");
            return true;
        }
    }

    public static boolean uploadFile(File uploadJobFile)
    {
        try(MongoClient client = new MongoClient("127.0.0.1", 27017)){

            GridFS gridFS = new GridFS(client.getDB("TheFloow"),"Jobs");
            GridFSInputFile gridFSInputFile = gridFS.createFile(uploadJobFile.getAbsolutePath());
            gridFSInputFile.setContentType("text/plain");

            DBObject metadata = gridFSInputFile.getMetaData();
            if(metadata==null)
            {
                metadata = new BasicDBObject();
                gridFSInputFile.setMetaData(metadata);
            }
            metadata.put("processing",false);
            metadata.put("processer","none");
            metadata.put("failed",0);
            metadata.put("startedTime",new Date());
            gridFSInputFile.save();
            return true;
        }
    }
}
