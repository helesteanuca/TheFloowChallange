package com.platform.process;

import com.platform.database.ConnectionManager;
import com.platform.process.word.Stemmer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cahel on 5/16/2018.
 */
public class JobConsumer implements Runnable {
    private String jobId;
    private String machineId;
    private Map<String,Integer> wordsInDocs = new HashMap<>();
    public boolean Success = false;

    public JobConsumer(String idJob, String serverId)
    {
        this.jobId = new String(idJob);
        this.machineId = new String(serverId);
    }

    @Override
    public void run() {

        File jobFile = ConnectionManager.getJobFile(jobId);
        ConnectionManager.updateWorker(machineId,jobId,"Alive");
        if(jobFile!=null) {
            ConnectionManager.setFileToProcessing(jobId,true,machineId);
            ConnectionManager.updateWorker(machineId,jobId,"Working");
            Stemmer fileSteam = new Stemmer(jobFile.getAbsolutePath());
            List<String> listOfWords = fileSteam.getListOfWords();
            for (String word : listOfWords) {
                if (null == wordsInDocs.get(word))
                    wordsInDocs.put(word, 1);
                else
                    wordsInDocs.put(word, wordsInDocs.get(word) + 1);
            }
            if(!wordsInDocs.isEmpty()) {
                ConnectionManager.updateDictionary(wordsInDocs);
                ConnectionManager.deleteFileAndChunks(this.jobId);
                ConnectionManager.updateWorker(machineId,jobId,"Success");
                Success = true;
                notify();
            }
            else
            {
                ConnectionManager.updateWorker(machineId,jobId,"Failed");
            }

        }
        ConnectionManager.updateWorker(machineId,jobId,"Finished");
    }
}
