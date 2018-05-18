package com.platform.process;

import com.platform.database.ConnectionManager;
import com.platform.util.Configuration;
import com.platform.util.InformationProvider;

import java.io.*;
import java.nio.file.Files;

/**
 * *****************************************************
 * Project: TheFloow
 * Date: 15/05/2018
 * File: FileConsumer
 * Version: 9
 * *****************************************************
 * Description: The source consumer that breaks the file into bytes and uploads them as jobs to be processed later
 * *****************************************************
 */
public class FileConsumer implements Runnable {

    private String fileName;
    private long fileSize = 10870912;
    private InformationProvider ilog;
    private ConnectionManager dbMng;
    private static final String TAG = "[FILE-CONSUMER]";

    public FileConsumer(String stfileName, long fSize, Configuration exCfg)
    {
        this.dbMng = new ConnectionManager(exCfg);
        this.ilog = exCfg.log;
        this.fileName = stfileName;
        this.fileSize = fSize;
    }

    @Override
    public void run() {
        try(BufferedReader rdr = new BufferedReader(new FileReader(this.fileName))){
            ilog.info(TAG+"Started consuming: "+fileName);
            String line;
            int i = 0;
            File tmp = File.createTempFile(new File(fileName).getName()+"-"+i+"-",".txt");
            BufferedWriter wrt = new BufferedWriter(new FileWriter(tmp));
            while((line=rdr.readLine())!=null)
            {
                if(!(tmp.length()<fileSize)) {
                    wrt.close();
                    dbMng.uploadFile(tmp);
                    ilog.info(TAG+"Uploaded Job with filename: "+tmp);
                    tmp.delete();
                    ++i;
                    tmp = File.createTempFile(new File(fileName).getName()+"-"+i+"-",".txt");
                    wrt = new BufferedWriter(new FileWriter(tmp));
                }

                wrt.append(line);
            }
            wrt.close();

            dbMng.uploadFile(tmp);
            ilog.info(TAG+"Uploaded Job with filename: "+tmp);
            ilog.info(TAG+"Finished consuming : "+fileName);

        } catch (FileNotFoundException e) {
            ilog.error(TAG+"File not found."+fileName+".\n"+e);
        } catch (IOException e) {
            ilog.error(TAG+"IOException consuming "+fileName+".\n"+e);
        }
    }
}
