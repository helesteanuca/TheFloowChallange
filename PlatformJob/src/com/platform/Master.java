package com.platform;

import com.platform.database.ConnectionManager;
import com.platform.process.FileConsumer;
import com.platform.process.JobConsumer;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Master {
    private static Configuration cfg;
    private static volatile List<String> jobIds = new ArrayList<>();

    public static void main(String[] args) {
	// write your code here
        if(args.length < 2) {
            InformationProvider.printHelp();
            System.exit(0);
        }
        cfg = new Configuration(args);
        jobIds = ConnectionManager.getAvailableJobs();

        if(cfg.getValue("source")!=null)
        {
            Thread consume = new Thread(new FileConsumer(cfg.getValue("source")));
            consume.start();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        while(!jobIds.isEmpty()){
            jobIds = ConnectionManager.getAvailableJobs();
            executorService.submit(new JobConsumer(jobIds.get(0)));
        }

    }
}
