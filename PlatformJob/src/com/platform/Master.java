package com.platform;

import com.platform.database.ConnectionManager;
import com.platform.process.FileConsumer;
import com.platform.process.JobConsumer;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 15/05/2018
 * File: Master
 * Version: 6
 * *****************************************************
 * Description: The main thread
 * *****************************************************
 */

public class Master {
    private static Configuration cfg;
    private static volatile List<String> jobIds = ConnectionManager.getAvailableJobs();
    private static String machineID = getMachineID();
    private static int jobSeeker = 0;
    private static final long SLEEPING_TIME = 300000;
    private static final int RETRY_ATTEMPTS = 10;
    private static final String TAG = "[MASTER]";

    public static void main(String[] args) {
	// write your code here

        if(args.length < 2 || args.length > 14) {
            InformationProvider.printHelp();
            System.exit(0);
        }

        cfg = new Configuration(args);
        cfg.log.info(TAG+"----- SERVER-NAME:"+machineID+"-----");
        cfg.log.printCfg(cfg.getParam());

        if(cfg.getValue("source")!="null")
        {
            cfg.log.info(TAG+"Starting processing source file:"+cfg.getValue("source"));
            Thread consumeFile = new Thread(new FileConsumer(cfg.getValue("source"),cfg.getLongValue("filesize"),cfg));
            consumeFile.start();
        }

        while (jobIds.isEmpty() && jobSeeker<RETRY_ATTEMPTS)
        {
            jobIds = ConnectionManager.getAvailableJobs();
            cfg.log.info(TAG+"No jobs to process. Wait-"+jobSeeker+"/"+RETRY_ATTEMPTS);
            try {
                Thread.sleep(SLEEPING_TIME);
            } catch (InterruptedException e) {
                cfg.log.error(TAG+"Error while calling sleeping from the Job Seeker procedure.");
            }
            ++jobSeeker;
            jobIds = ConnectionManager.getAvailableJobs();
        }

        if(!jobIds.isEmpty()) {

            while(!jobIds.isEmpty())
            {
                cfg.log.info(TAG+"Found "+jobIds.size()+" jobs to process.");
                Thread consumeJob = new Thread(new JobConsumer(jobIds.get(0),machineID,cfg));
                consumeJob.start();
                synchronized (consumeJob)
                {
                    try {
                        consumeJob.wait();
                    } catch (InterruptedException e) {
                        cfg.log.error(TAG+"Error while waiting job "+jobIds.get(0)+" to finish!");
                    }
                }
                jobIds = ConnectionManager.getAvailableJobs();
            }
        }
        cfg.log.info(TAG+"Preparing results...");
        cfg.log.printResults(new ConnectionManager(cfg).retrieveFinalRezults());
        cfg.log.info(TAG+"-----END?-----");
        cfg.close();
    }


    private static String getMachineID()
    {
        String machId;
        try{
            machId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Map<String, String> env = System.getenv();
            if (env.containsKey("COMPUTERNAME"))
                machId = env.get("COMPUTERNAME");
            else
                machId = env.getOrDefault("HOSTNAME","UnknownServer");
        }
        return machId;
    }
}
