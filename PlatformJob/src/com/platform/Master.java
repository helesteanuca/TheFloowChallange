package com.platform;

import com.platform.database.ConnectionManager;
import com.platform.process.FileConsumer;
import com.platform.process.JobConsumer;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Master {
    private static Configuration cfg;
    private static volatile List<String> jobIds = ConnectionManager.getAvailableJobs();
    private static String machineID = getMachineID();
    private static int jobSeeker = 0;
    private static final long SLEEPING_TIME = 300000;
    private static final int RETRY_ATTEMPTS = 10;

    public static void main(String[] args) {
	// write your code here

        if(args.length < 2) {
            InformationProvider.printHelp();
            System.exit(0);
        }

        cfg = new Configuration(args);
        cfg.log.info("----- SERVER-NAME:"+machineID+"-----");
        cfg.log.printCfg(cfg.getParam());

        if(cfg.getValue("source")!="null")
        {
            cfg.log.info("Starting processing source file:"+cfg.getValue("source"));
            Thread consumeFile = new Thread(new FileConsumer(cfg.getValue("source"),cfg.getLongValue("filesize")));
            consumeFile.start();
        }

        while (jobIds.isEmpty() && jobSeeker<RETRY_ATTEMPTS)
        {
            jobIds = ConnectionManager.getAvailableJobs();
            cfg.log.info("No jobs to process. Wait-"+jobSeeker+"/"+RETRY_ATTEMPTS);
            try {
                Thread.sleep(SLEEPING_TIME);
            } catch (InterruptedException e) {
                cfg.log.error("Error while calling sleeping from the Job Seeker procedure.");
            }
            ++jobSeeker;
            jobIds = ConnectionManager.getAvailableJobs();
        }

        if(!jobIds.isEmpty()) {

            while(!jobIds.isEmpty())
            {
                cfg.log.info("Found "+jobIds.size()+" jobs to process.");
                Thread consumeJob = new Thread(new JobConsumer(jobIds.get(0),machineID));
                consumeJob.start();
                synchronized (consumeJob)
                {
                    try {
                        consumeJob.wait();
                    } catch (InterruptedException e) {
                        cfg.log.error("Error while waiting job "+jobIds.get(0)+" to finish!");
                    }
                }
                jobIds = ConnectionManager.getAvailableJobs();
            }
        }
        cfg.log.info("-----END?-----");
    }

    private static String getMachineID()
    {
        String machId = "UnknownComputer";
        try{
            machId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Map<String, String> env = System.getenv();
            if (env.containsKey("COMPUTERNAME"))
                machId = env.get("COMPUTERNAME");
            else if (env.containsKey("HOSTNAME"))
                machId = env.get("HOSTNAME");
            else
                machId = "UnknownComputer";
        }
        return machId;
    }
}
