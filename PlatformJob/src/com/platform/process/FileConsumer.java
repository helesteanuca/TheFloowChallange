package com.platform.process;

import com.platform.database.ConnectionManager;

import java.io.*;
import java.nio.file.Files;

/**
 * Created by cahel on 5/16/2018.
 */
public class FileConsumer implements Runnable {

    private String fileName;
    private long fileSize = 10870912;

    public FileConsumer(String stfileName, long fSize)
    {
        this.fileName = stfileName;
        this.fileSize = fSize;
    }

    @Override
    public void run() {
        try(BufferedReader rdr = new BufferedReader(new FileReader(this.fileName))){

            String line;
            int i = 0;
            File tmp = File.createTempFile(new File(fileName).getName()+"-"+i+"-",".txt");
            BufferedWriter wrt = new BufferedWriter(new FileWriter(tmp));
            while((line=rdr.readLine())!=null)
            {
                if(!(tmp.length()<fileSize)) {
                    wrt.close();
                    ConnectionManager.uploadFile(tmp);
                    tmp.delete();
                    ++i;
                    tmp = File.createTempFile(new File(fileName).getName()+"-"+i+"-",".txt");
                    wrt = new BufferedWriter(new FileWriter(tmp));
                }

                wrt.append(line);
            }
            wrt.close();
            ConnectionManager.uploadFile(tmp);
            Files.delete(tmp.toPath());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
