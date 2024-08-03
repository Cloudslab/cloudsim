package org.cloudbus.cloudsim.container.utils;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by sareh on 30/07/15.
 * Modified by Remo Andreoli, Jun 2024.
 */
public class CustomCSVWriter {
    String fileAddress;
    boolean newFileCreated = false;

    CSVWriter writer;

    public CustomCSVWriter(String fileAddress) throws IOException {
        File f = new File(fileAddress);
        File parentF = f.getParentFile();

        if(!parentF.exists() && !parentF.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parentF);
        }

        if(f.createNewFile()) {
            newFileCreated = true;
        }

        setFileAddress(fileAddress);
    }

    public void writeTofile(String[] entries) throws IOException {
        writeTofile(entries, true);
    }

    public void writeTofile(String[] entries, boolean appendMode) throws IOException {
        // feed in your array (or convert your data to an array)
        try {
            writer = new CSVWriter(new FileWriter(fileAddress, appendMode),
                    ',',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

        } catch (IOException e) {
            Log.printlnConcat("Couldn't find the file to write to: ", fileAddress);
        }

        writer.writeNext(entries);
        writer.flush();
        writer.close();
    }

    public boolean fileExistedAlready() {
        return !newFileCreated;
    }

    public String getFileAddress() { return fileAddress; }
    public void setFileAddress(String fileAddress) { this.fileAddress = fileAddress; }
}




