package org.cloudbus.cloudsim.container.utils;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by sareh on 30/07/15.
 */
public class CostumeCSVWriter {
    CSVWriter writer;
    String fileAddress;
    Writer fileWriter;

    public CostumeCSVWriter(String fileAddress) throws IOException {
        File f = new File(fileAddress);
        File parent3 = f.getParentFile();
        if(!parent3.exists() && !parent3.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent3);
        }
        if(!f.exists())
            f.createNewFile();
        setFileAddress(fileAddress);


    }

    public void writeTofile(String[] entries) throws IOException {
        // feed in your array (or convert your data to an array)
        try {
            writer = new CSVWriter(new FileWriter(fileAddress, true), ',',CSVWriter.NO_QUOTE_CHARACTER);

        } catch (IOException e) {
            Log.printConcatLine("Couldn't find the file to write to: ", fileAddress);


        }
        writer.writeNext(entries);
        writer.flush();
        writer.close();
    }

    public String getFileAddress() {
        return fileAddress;
    }

    public void setFileAddress(String fileAddress) {
        this.fileAddress = fileAddress;
    }
}




