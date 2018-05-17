package com.platform.util;

import com.platform.database.ConnectionManager;

import java.io.File;
import java.util.*;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 15/05/2018
 * File: Configuration
 * Version: 3
 * *****************************************************
 * Description: Configuration manager of the ran
 * *****************************************************
 */
public class Configuration{
    
    private Map<String,String> param = new HashMap<>();     //List of the parameters that are considered as settings of the ran.
    private final String[] mandatoryKeys ={"source", "mongo"};      //Mandatory keys to be mentioned at the ran or to be defaulted.
    public InformationProvider log;     //The information provider of the ran. Can be instantiated with a 4 output methods.
    
    private static final List<String> allKeys = Arrays.asList("source", "mongo", "log","chunksize","filesize","rezult");    //Supported parameters of the program.



    enum LOGMETHOD{CONSOLE,FILE,ALL,NONE}       //The information provider methods of output

    public Configuration(String[] listParam)
    {
        for(int i = 0; i < listParam.length; ++i)
        {
            if(listParam[i].contains("-")) {
                param.put(listParam[i].replace("-",""), listParam[i + 1]);
            }
            ++i;
        }
        initiateDefaults();

        if(!validateParams()) {
            InformationProvider.printHelp();
            System.exit(0);
        }
    }

    private void initiateDefaults()
    {
        if(!param.containsKey("source"))
        {
            param.put("source","null");
        }
        if(!param.containsKey("log"))
        {
            param.put("log","console");
        }
        if(!param.containsKey("chunksize"))
        {
            param.put("chunksize","358400");
        }
        if(!param.containsKey("filesize"))
        {
            param.put("filesize","10870912");
        }
        if(!param.containsKey("rezult"))
        {
            param.put("rezult","console");
        }
    }

    ///Validates the configuration obtained after the initiate
    private boolean validateParams()
    {
        boolean correct = false;
        for(Map.Entry<String,String> set : param.entrySet())
        {
            if(!allKeys.contains(set.getKey()))
                return false;
            if(set.getKey().equals("log"))
                correct = verifyAndInstantiateIProvider(set.getValue());
            if(set.getKey().equals("source"))
                correct = verifySourceInformation(set.getValue());
            if(set.getKey().equals("mongo"))
                correct = verifyMongoDBInformation(set.getValue());
            if(set.getKey().equals("chunksize"))
                correct = verifySize(set.getValue());
            if(set.getKey().equals("filesize"))
                correct = verifySize(set.getValue());
        }
        return correct;
    }

    ///Validates that the specified chunksize / filesize is a number
    private boolean verifySize(String value)
    {
        try{
            long cSize = Integer.parseInt(value);
            return true;
        }
        catch(NumberFormatException ex)
        {
            System.out.println("Invalid -chunksize / -filesize specified: "+ value + " - Not a number!");
            return false;
        }
    }
    
    ///Validates and test the connection specified by user
    private boolean verifyMongoDBInformation(String value)
    {
        String test = value;
        if(!value.contains(":"))
            test+=":27017";
        return ConnectionManager.testConnection(test.split(":")[0],test.split(":")[1]);
    }
    
    ///Validates that the file specified exists
    private boolean verifySourceInformation(String value)
    {
        File test = new File(value);
        return test.exists() && test.isFile();
    }
    
    ///Validates the parameter value of the log and initialize the output methods to log information
    private boolean verifyAndInstantiateIProvider(String value)
    {
        switch(value)
                {
                    case "console": this.log = new InformationProvider(LOGMETHOD.CONSOLE); break;
                    case "file": this.log = new InformationProvider("TheFloow"); break;
                    case "all": this.log = new InformationProvider(LOGMETHOD.ALL); break;
                    case "none": this.log = new InformationProvider(LOGMETHOD.NONE); break;
                    default: System.out.println("Invalid parameter for -log."); break;
                }
        return this.log!=null;
    }

    ///Returns the value of a setting. Returns null if the key mentioned is invalid
    public String getValue(String key)
    {
        return param.get(key);
    }

    public Map<String,String> getParam()
    {
        return this.param;
    }

    public long getLongValue(String Key) {
        return Integer.parseInt(param.get(Key));
    }

}
