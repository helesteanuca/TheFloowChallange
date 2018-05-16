package com.platform.process;

import com.platform.database.ConnectionManager;

import java.io.*;

/**
 * Created by cahel on 5/16/2018.
 */
public class FileConsumer implements Runnable {

    private String fileName;

    public FileConsumer(String stfileName)
    {
        this.fileName = new String(stfileName);
    }
    @Override
    public void run() {
        try(BufferedReader rdr = new BufferedReader(new FileReader(this.fileName))){

            String line;
            int i = 0;
            File tmp = File.createTempFile(new File(fileName).getName()+"-"+i+"-",".txt");

            while((line=rdr.readLine())!=null)
            {
                if(!(tmp.length()<536870912)) {
                    ConnectionManager.uploadFile(tmp);
                    tmp.delete();
                    ++i;
                    tmp = File.createTempFile(new File(fileName).getName()+"-"+i+"-",".txt");
                }
                try(BufferedWriter wrt = new BufferedWriter(new FileWriter(tmp))) {
                        wrt.append(line);
                }
            }

            ConnectionManager.uploadFile(tmp);
            tmp.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
