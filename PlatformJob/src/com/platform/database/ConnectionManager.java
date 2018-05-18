package com.platform.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.UpdateOptions;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;


import static com.mongodb.client.model.Filters.eq;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 15/05/2018
 * File: ConnectionManager
 * Version: 7
 * *****************************************************
 * Description: The mongoDB handler
 * *****************************************************
 */
public class ConnectionManager{

    private Configuration cfg;
    private static String host = "localhost";
    private static int port = 27017;
    private static Integer chunkSize = 358400;
    private InformationProvider ilog;
    private static final String TAG = "[MONGODB-JOBS]";

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

    public static boolean testConnection(String hostname, String port) {
        MongoClient mongoClient = new MongoClient(hostname, Integer.parseInt(port));
        if(mongoClient!=null)
        {
            mongoClient.close();
            return true;
        }
        return false;
    }

    public void uploadFile(File uploadJobFile)
    {
        cfg.log.info(TAG+"Uploading file "+uploadJobFile+"...");
        try(MongoClient client = new MongoClient(host, port)){
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            InputStream streamToUploadFrom = new FileInputStream(uploadJobFile);

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(chunkSize)
                    .metadata(new Document("type", "text").append("processing",false).append("processer","none").append("timeStart",new Date()).append("failed",0));

            gridFSFilesBucket.uploadFromStream(uploadJobFile.getName(), streamToUploadFrom, options);
            client.close();
            cfg.log.info(TAG+"Uploaded file "+uploadJobFile+" successfully.");
        } catch (FileNotFoundException e) {
            cfg.log.error(TAG+"File not found for upload "+uploadJobFile);
        }
    }

    public static List<String> getAvailableJobs()
    {
        List<String> jobIds = new ArrayList<>();
        try(MongoClient client = new MongoClient(host, port)){
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            gridFSFilesBucket.find(eq("metadata.processing", false)).forEach((Consumer<? super com.mongodb.client.gridfs.model.GridFSFile>) gridFSFile -> jobIds.add(gridFSFile.getObjectId().toString()));
            client.close();
        }
        return jobIds;
    }

    public void updateWorker(String workerID,String jobId,String status)
    {
        cfg.log.info(TAG+"Updating database worker "+workerID+"-j-"+jobId+"...");
        try(MongoClient client = new MongoClient(host, port)) {
            MongoDatabase db = client.getDatabase("TheFloow");
            MongoCollection<Document> dic = db.getCollection("Workers");
            Document dbExist = dic.find(eq("worker",workerID+"-j-"+jobId)).first();
            Bson filter = new Document("worker",workerID+"-j-"+jobId);
            Bson newValue = new Document("status", status);
            Bson updateOperationDocument = new Document("$set", newValue);
            dic.updateOne(filter, updateOperationDocument,new UpdateOptions().upsert(true));
            cfg.log.info(TAG+"Updated database worker "+workerID+"-j-"+jobId+".");
            client.close();
        }
    }

    public File getJobFile(String jobId)
    {
        cfg.log.info(TAG+"Retrieving job "+jobId+"...");
        try(MongoClient client = new MongoClient(host, port)) {
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            File tmpJob = File.createTempFile("jobId","txt");
            FileOutputStream streamToDownloadTo = new FileOutputStream(tmpJob);
            gridFSFilesBucket.downloadToStream(new ObjectId(jobId), streamToDownloadTo);
            streamToDownloadTo.close();
            client.close();
            cfg.log.info(TAG+"Retrieved job "+jobId+".");
            return tmpJob;
        } catch (FileNotFoundException e) {
            cfg.log.error(TAG+"Job failed to retrieve as the temporary file cannot be created."+"\n"+e);
        } catch (IOException e) {
            cfg.log.error(TAG+"Job failed to retrieve as there is a problem writing the temporary file."+"\n"+e);
        }
        return null;
    }

    public void updateDictionary(Map<String,Integer> words)
    {
        cfg.log.info(TAG+"Uploading "+words.size()+"...");
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
            cfg.log.info(TAG+"Uploaded "+words.size()+".");
            client.close();
        }
    }

    public void setFileToProcessing(String jobIds, boolean status, String worker)
    {
        cfg.log.info(TAG+"Setting job "+jobIds+" as in process...");
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
            }
            client.close();
            cfg.log.info(TAG+"Just set job "+jobIds+" as in process.");
        }
    }

    public void deleteFileAndChunks(String jobIds)
    {
        cfg.log.info(TAG+"Deleting "+jobIds+"...");
        try(MongoClient client = new MongoClient(host, port)) {
            GridFSBucket gridFSFilesBucket = GridFSBuckets.create(client.getDatabase("TheFloow"), "Jobs");
            gridFSFilesBucket.delete(new ObjectId(jobIds));
            client.close();
            cfg.log.info(TAG+"Deleted "+jobIds+".");
        }
    }

    public Map<String,String> retrieveFinalRezults()
    {
        Map<String,String> outPut = new HashMap<>();
        try(MongoClient client = new MongoClient(host, port)) {
            MongoDatabase db = client.getDatabase("TheFloow");
            MongoCollection<Document> dic = db.getCollection("Dictionary");
            Bson filter = new Document("count", -1);
            dic.find().sort(filter).limit(cfg.getResultSize()).forEach((Consumer<? super Document>) words -> outPut.put(words.getString("word"),words.get("count").toString()));
            filter = new Document("count", +1);
            dic.find().sort(filter).limit(cfg.getResultSize()).forEach((Consumer<? super Document>) words -> outPut.put(words.getString("word"),words.get("count").toString()));
            return outPut;
        }

    }
}
