package org.cloudbus.cloudsim.workload; 

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CloudletDataParser {

    public static List<Cloudlet> parseCloudletData(String filePath) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        try (InputStream inputStream = CloudletDataParser.class.getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            br.readLine(); // Skip header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                long timestamp = Long.parseLong(values[0]);
                int cloudletId = Integer.parseInt(values[1]);
                long length = Long.parseLong(values[2]);
                long fileSize = Long.parseLong(values[3]);
                long outputSize = Long.parseLong(values[4]);
                int pesNumber = Integer.parseInt(values[5]);
                String utilizationModel = values[6];

                UtilizationModelFull utilization = new UtilizationModelFull();
                Cloudlet cloudlet = new Cloudlet(cloudletId, length, pesNumber, fileSize, outputSize, utilization, utilization, utilization);
                cloudletList.add(cloudlet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloudletList;
    }

    public static void main(String[] args) {
        CloudSim.init(1, null, false);
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        String filePath = "/workload.csv";
        List<Cloudlet> cloudletList = parseCloudletData(filePath);

        // Print the cloudlets to verify
        for (Cloudlet cloudlet : cloudletList) {
            System.out.println(cloudlet);
        }
    }
}