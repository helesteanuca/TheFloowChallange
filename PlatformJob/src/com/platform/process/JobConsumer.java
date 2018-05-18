package com.platform.process;

import com.platform.database.ConnectionManager;
import com.platform.process.word.Stemmer;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 16/05/2018
 * File: JobConsumer
 * Version: 9
 * *****************************************************
 * Description: The Job consumer consumes gridfs files, processes them with the Stemmer
 * *****************************************************
 */
public class JobConsumer implements Runnable {
    private String jobId;
    private String machineId;
    private Map<String,Integer> wordsInDocs = new HashMap<>();
    public boolean Success = false;
    private InformationProvider ilog;
    private ConnectionManager dbMng;
    private static final String TAG = "[JOB-CONSUMER]";

    public JobConsumer(String idJob, String serverId, Configuration exCfg)
    {
        this.ilog = exCfg.log;
        this.dbMng = new ConnectionManager(exCfg);
        this.jobId = new String(idJob);
        this.machineId = new String(serverId);
    }

    @Override
    public void run() {

        File jobFile = dbMng.getJobFile(jobId);
        dbMng.updateWorker(machineId,jobId,"Alive");
        ilog.info(TAG+"Worker: "+machineId+"-j-"+jobId+" - Status: Alive.");
        if(jobFile!=null) {

            dbMng.setFileToProcessing(jobId,true,machineId);
            dbMng.updateWorker(machineId,jobId,"Working");
            ilog.info(TAG+"Worker: "+machineId+"-j-"+jobId+" - Status: Working.");

            Stemmer fileSteam = new Stemmer(jobFile.getAbsolutePath());
            List<String> listOfWords = fileSteam.getListOfWords();

            for (String word : listOfWords) {
                if (null == wordsInDocs.get(word))
                    wordsInDocs.put(word, 1);
                else
                    wordsInDocs.put(word, wordsInDocs.get(word) + 1);
            }

            if(!wordsInDocs.isEmpty()) {
                ilog.info(TAG+"Worker: "+machineId+"-j-"+jobId+" - Status: Uploading.");
                dbMng.updateDictionary(wordsInDocs);
                dbMng.updateWorker(machineId,jobId,"Uploading");
                ilog.info(TAG+"Worker: "+machineId+"-j-"+jobId+" - Status: Uploaded/updated "+wordsInDocs.size()+" words.");
                dbMng.deleteFileAndChunks(this.jobId);
                dbMng.updateWorker(machineId,jobId,"Success/Finished");
                ilog.info(TAG+"Worker: "+machineId+"-j-"+jobId+" - Status: Success/Finished.");
                Success = true;
            }
            else
            {
                dbMng.updateWorker(machineId,jobId,"Failed/Finished");
                ilog.warn(TAG+"Worker: "+machineId+"-j-"+jobId+" - Status: Failed/Finished.");
            }
        }
        notify();
    }
}
