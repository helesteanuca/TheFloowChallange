package com.platform.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
public class Configuration {
    
    private Map<String,String> param = new HashMap<>();     //List of the parameters that are considered as settings of the ran.
    private final String[] mandatoryKeys ={"source", "mongo"};      //Mandatory keys to be mentioned at the ran or to be defaulted.
    public InformationProvider log;     //The information provider of the ran. Can be instantiated with a 4 output methods.
    
    private static final ImmutableList<String> allKeys = ImmutableList.of("source", "mongo", "log");    //Supported parameters of the program.

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
        validateParams();
    }

    ///Validates the configuration obtained after the initiate
    private void validateParams()
    {
        for(Map.Entry<String,String> set : param.entrySet())
        {
            if(set.getKey().equals("log"))
                verifyAndInstantiateIProvider(set.getValue());
            if(set.getKey().equals("source"))
                verifySourceInformation(set.getValue());
            if(set.getKey().equals("mongo"))
                verifyMongoDBInformation(set.getValue());
        }
    }
    
    ///Validates and test the connection specified by user
    private void verifyMongoDBInformation(String value)
    {
        
    }
    
    ///Validates that the file specified exists
    private void verifySourceInformation(String value)
    {
        
    }
    
    ///Validates the parameter value of the log and initialize the output methods to log information
    private void verifyAndInstantiateIProvider(String value)
    {
        switch(value)
                {
                    case "console": this.log = new InformationProvider(LOGMETHOD.CONSOLE); break;
                    case "file": this.log = new InformationProvider("TheFloow"); break;
                    case "all": this.log = new InformationProvider(LOGMETHOD.ALL); break;
                    case "none": this.log = new InformationProvider(LOGMETHOD.NONE); break;
                    default: System.out.println("Invalid parameter for -log."); break;
                }
    }

    ///Returns the value of a setting. Returns null if the key mentioned is invalid
    public String getValue(String key)
    {
        return param.get(key);
    }

    ///Inserts a new setting if this is not already present in the configuration
    public boolean insert(String key,String value)
    {
        if(param.containsKey(key))
            return false;
        else {
            param.put(key, value);
            return true;
        }
    }

    ///Updates or inserts a setting 
    public boolean update(String key, String value)
    {
        param.put(key,value);
        return true;
    }
}
