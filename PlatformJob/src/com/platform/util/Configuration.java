package com.platform.util;

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

    public Configuration(String[] listParam)
    {

    }

    public Configuration(Map<String,String> params)
    {
        param.putAll(params);
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
