package com.platform.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.UpdateOptions;
import com.platform.util.Configuration;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


import static com.mongodb.client.model.Filters.eq;

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
    private static Integer chunkSize = 358400;

    public ConnectionManager(Configuration exCfg)
    {
        this.cfg = exCfg;
        if(cfg.getValue("mongo").contains(":")) {
            host = cfg.getValue("mongo").split(":")[0];
            port = Integer.parseInt(cfg.getValue("mongo").split(":")[1]);
        }
        else
            host = cfg.getValue("mongo");
        chunkSize = Integer.parseInt(cfg.getValue("chunksize"));
    }


    @Override
    public boolean prepareCollections() {
        return false;
    }

    public static boolean testConnection(String hostname, String port) {

            MongoClient mongoClient = new MongoClient(hostname, Integer.parseInt(port));
            if(mongoClient!=null)
            {
                mongoClient.close();
                return true;
            }
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
        try(MongoClient client = new MongoClient(host, port)){
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            InputStream streamToUploadFrom = new FileInputStream(uploadJobFile);

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(chunkSize)
                    .metadata(new Document("type", "text").append("processing",false).append("processer","none").append("timeStart",new Date()).append("failed",0));

            ObjectId fileId = gridFSFilesBucket.uploadFromStream(uploadJobFile.getName(), streamToUploadFrom, options);
            System.out.println(fileId);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getAvailableJobs()
    {
        List<String> jobIds = new ArrayList<>();
        try(MongoClient client = new MongoClient(host, port)){
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            gridFSFilesBucket.find(eq("metadata.processing", false)).forEach((Consumer<? super com.mongodb.client.gridfs.model.GridFSFile>) gridFSFile -> jobIds.add(gridFSFile.getObjectId().toString()));
        }
        return jobIds;
    }

    public static boolean updateWorker(String workerID,String jobId,String status)
    {
        try(MongoClient client = new MongoClient(host, port)) {
            MongoDatabase db = client.getDatabase("TheFloow");
            MongoCollection<Document> dic = db.getCollection("Workers");
            Document dbExist = dic.find(eq("worker",workerID+jobId)).first();
            Bson filter = new Document("worker",workerID+jobId);
            Bson newValue = new Document("status", status);
            Bson updateOperationDocument = new Document("$set", newValue);
            dic.updateOne(filter, updateOperationDocument,new UpdateOptions().upsert(true));
        }
        return true;
    }

    public static File getJobFile(String jobId)
    {
        try(MongoClient client = new MongoClient(host, port)) {
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            File tmpJob = File.createTempFile("jobId","txt");
            FileOutputStream streamToDownloadTo = new FileOutputStream(tmpJob);
            gridFSFilesBucket.downloadToStream(new ObjectId(jobId), streamToDownloadTo);
            streamToDownloadTo.close();
            return tmpJob;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateDictionary(Map<String,Integer> words)
    {
        try(MongoClient client = new MongoClient(host, port)) {
            MongoDatabase db = client.getDatabase("TheFloow");
            MongoCollection<Document> dic = db.getCollection("Dictionary");
            for(Map.Entry<String,Integer> word : words.entrySet())
            {
                int count = word.getValue();
                Document dbExist = dic.find(eq("word",word.getKey())).first();
                if(dbExist!=null)
                {
                    count = dbExist.getInteger("count") + word.getValue();
                }
                Bson filter = new Document("word", word.getKey());
                Bson newValue = new Document("count", count);
                Bson updateOperationDocument = new Document("$set", newValue);
                dic.updateOne(filter, updateOperationDocument,new UpdateOptions().upsert(true));
            }
        }
        return false;
    }

    public static boolean setFileToProcessing(String jobIds, boolean status, String worker)
    {
        try(MongoClient client = new MongoClient(host, port)) {
            MongoDatabase db = client.getDatabase("TheFloow");
            MongoCollection<Document> dic = db.getCollection("Jobs.files");
            Document dbExist = dic.find(eq("_id",new ObjectId(jobIds))).first();
            if(dbExist!=null)
            {
                Bson filter = new Document("_id", new ObjectId(jobIds));
                Bson newValue = new Document("metadata.processing", true);
                Bson updateOperationDocument = new Document("$set", newValue);
                dic.updateOne(filter, updateOperationDocument);
                newValue = new Document("metadata.processer", worker);
                updateOperationDocument = new Document("$set", newValue);
                dic.updateOne(filter, updateOperationDocument);
                return true;
            }
        }
        return false;
    }

    public static boolean deleteFileAndChunks(String jobIds)
    {
        try(MongoClient client = new MongoClient(host, port)) {
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            gridFSFilesBucket.delete(new ObjectId(jobIds));
        }
        return false;
    }
}
