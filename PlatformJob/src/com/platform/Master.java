package com.platform;

import com.platform.process.FileConsumer;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;

public class Master {
    private static Configuration cfg;

    public static void main(String[] args) {
	// write your code here
        if(args.length < 2) {
            InformationProvider.printHelp();
            System.exit(0);
        }
        cfg = new Configuration(args);

        if(cfg.getValue("source")!=null)
        {
            Thread consume = new Thread(new FileConsumer(cfg.getValue("source")));
            consume.start();
        }



    }
}
