package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.container.utils.CustomCSVWriter;

import java.io.IOException;

public class TestCSVWriter {
    private static final String fileAddress = "/tmp/testFile.log";
    public static void main(String[] args) {
        CustomCSVWriter writer;

        String[] strings1 = {"Hello", "World"};
        String[] strings2 = {"000", "111", "222"};

        try {
            writer = new CustomCSVWriter(fileAddress);
            writer.writeTofile(strings1, false);
            writer.writeTofile(strings2, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Output in "+fileAddress);
    }
}
