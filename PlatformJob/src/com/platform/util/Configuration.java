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
    private Map<String,String> param = new HashMap<>();
    private final String[] mandatoryKeys ={"source", "mongo"};
    public InformationProvider log;

    enum LOGMETHOD{CONSOLE,FILE,ALL,NONE}

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

    private void validateParams()
    {
        for(Map.Entry<String,String> set : param.entrySet())
        {
            if(set.getKey().equals("log"))
            {
                switch(set.getValue())
                {
                    case "console": this.log = new InformationProvider(LOGMETHOD.CONSOLE); break;
                    case "file": this.log = new InformationProvider("TheFloow"); break;
                    case "all": this.log = new InformationProvider(LOGMETHOD.ALL); break;
                    case "none": this.log = new InformationProvider(LOGMETHOD.NONE); break;
                    default: System.out.println("Invalid parameter for -log."); break;
                }
            }
        }
    }

    public String getValue(String key)
    {
        return param.get(key);
    }

    public boolean insert(String key,String value)
    {
        if(param.containsKey(key))
            return false;
        else {
            param.put(key, value);
            return true;
        }
    }

    public boolean update(String key, String value)
    {
        param.put(key,value);
        return true;
    }
}
