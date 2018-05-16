package com.platform.database;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
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
 * File: ConnectionManager
 * Version: 3
 * *****************************************************
 * Description:
 * *****************************************************
 */
public class ConnectionManager implements DBConfiguration{

    private Configuration cfg;
    private static String host = "localhost";
    private static int port = 27017;

    public ConnectionManager(Configuration exCfg)
    {
        this.cfg = exCfg;
        host = cfg.getValue("mongo").split(":")[0];
        port = Integer.parseInt(cfg.getValue("mongo").split(":")[1]);
    }


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

        try (MongoClient mongoClient = new MongoClient(host, port)) {

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

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(358400)
                    .metadata(new Document("type", "text").append("processing",false).append("processer","none").append("timeStart",new Date()).append("failed",0));

            ObjectId fileId = gridFSFilesBucket.uploadFromStream(uploadJobFile.getName(), streamToUploadFrom, options);
            System.out.println(fileId);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
