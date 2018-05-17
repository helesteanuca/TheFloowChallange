package com.platform.util;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 15/05/2018
 * File: InformationProvider
 * Version: 1
 * *****************************************************
 * Description: Used for logging information regarding the ran
 * *****************************************************
 */
public class InformationProvider {

    ///Only used for text typing
    private static final String END_LINE = "\n";
    private static final DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd][HH:mm:ss]");

    ///Used for creating the log file in a relational position with the ran
    private final String workingDir = System.getProperty("user.dir");
    private Configuration.LOGMETHOD logMethod = Configuration.LOGMETHOD.CONSOLE;

    ///Supports 3 type of messages
    enum MSGTYPE{
        INFO("[INFO]"),
        WARN("[WARN]"),
        ERROR("[ERROR]");

        private String prefix;

        MSGTYPE(String msg)
        {
            this.prefix = msg;
        }
        public String getPrefix()
        {
            return this.prefix;
        }

    }

    ///Variables in the text of the log
    private Date timeEvent = null;
    private BufferedWriter logFile = null;

    ///Empty constructor for no output to file : system.out as default output
    public InformationProvider(Configuration.LOGMETHOD estLogMethod)
    {
        this.logMethod = estLogMethod;
    }

    ///Constructor with a log file output
    public InformationProvider(String fileName)
    {
        boolean succLog = initiateLogFileWriter(fileName);
        if(!succLog) {
            error("Problem in initiating logfile: " + fileName + ". Defaulted to console output!");
            logMethod = Configuration.LOGMETHOD.CONSOLE;
        }
        else
            info("Successful initiated logfile");

    }

    private boolean initiateLogFileWriter(String fileName)
    {
        timeEvent = new Date();
        DateFormat aod = new SimpleDateFormat("yyyyMMdd.HH.mm");
        try{
            File logF = new File(workingDir+"/logs/"+fileName + aod.format(timeEvent)+".log");
            if(!logF.exists() && logF.createNewFile()) {
                logFile = new BufferedWriter(new PrintWriter(logF));
                return true;
            }
            return false;
        }
        catch(IOException ex)
        {
            error("Failed to create logfile at this location: "+workingDir+"/logs/fileName."+aod.format(timeEvent)+".log");
            return false;
        }
    }

    public void info(String infoMsg)
    {
        inform(MSGTYPE.INFO,infoMsg);
    }

    public void error(String errMsg)
    {
        inform(MSGTYPE.ERROR, errMsg);
    }

    public void warn(String warnMsg)
    {
        inform(MSGTYPE.WARN, warnMsg);
    }

    private void inform(MSGTYPE type, String message)
    {
        timeEvent = new Date();
        String finMsg = type.getPrefix()+dateFormat.format(timeEvent)+message+ END_LINE;
        switch (logMethod)
        {
            case ALL: informFile(finMsg); informConsole(finMsg); break;
            case FILE: informFile(finMsg); break;
            case CONSOLE: informConsole(finMsg); break;
            case NONE: break;
            default : informConsole(finMsg); break;
        }
    }

    private void informFile(String finalMsg)
    {
        if(logFile==null)
            return;

        try{
            logFile.write(finalMsg);
        }
        catch(IOException ex)
        {
            error("Problem writing log to logfile. LOG: "+finalMsg+ END_LINE +ex);
        }
    }

    private void informConsole(String finalMsg)
    {
        System.out.print(finalMsg);
    }

    public static void printHelp()
    {
        System.out.println();
        System.out.println("------Information Provider-------");
        System.out.println("-source\t\t\tInput file to be processed and filepath(\"/usr/Documents/\", \"C:\\Folder\\filename.ext\")\n");
        System.out.println("-mongo\t\t\tThe ip and port adress of the mongoDB server\n\t\t\t\tlocalhost, 192.168.100.1:99882 ...\n");
        System.out.println("-log\t\t\tThe output method that the program will have\n\t\t\t\tconsole/file/all/none (For file a file will be created with the name /logs/TheFloow.yyyyMMdd.HH.mm.log)\n");
        System.out.println("-chunksize\t\tNumber of bytes of a chunk in mongodb for the subfiles file ex: 371234\n\t\t\t\tDefault:358400\n");
        System.out.println("-filezie\t\tNumber of bytes of the subfiles created from the sourcefile in mongodb for the source file ex: 371234\n\t\t\t\tDefault:10870912\n");
        System.out.println("-rezult\t\t\tThe display method of the rezults\n\t\t\t\tconsole/file/all/none (For file a file will be created with the name /rez/TheFloow.yyyyMMdd.HH.mm.rez)\n");
        System.out.println("------Example of run-------");
        System.out.println("java –Xmx8192m -jar PlatformJob.jar –source dump.xml –mongo localhost -log file -chunksize 358400 -filesize 10870912 -rezult file");
    }

    public void printCfg(Map<String,String> param)
    {
        System.out.println("------Configuration-------");
        for(Map.Entry<String,String> entry : param.entrySet())
        {
            info(entry.getKey()+":\t\t"+entry.getValue());
        }
        System.out.println("------Configuration-------");
    }

}
