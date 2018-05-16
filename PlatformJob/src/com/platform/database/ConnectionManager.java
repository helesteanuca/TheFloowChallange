package com.platform.database;

import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.platform.util.Configuration;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            InputStream streamToUploadFrom = new FileInputStream(uploadJobFile);
            // Create some custom options
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(358400)
                    .metadata(new Document("type", "text"));

            ObjectId fileId = gridFSFilesBucket.uploadFromStream("mongodb-tutorial", streamToUploadFrom, options);
            System.out.println(fileId);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
