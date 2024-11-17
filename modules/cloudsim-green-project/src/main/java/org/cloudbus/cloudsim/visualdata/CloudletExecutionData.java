package org.cloudbus.cloudsim.visualdata;

import org.cloudbus.cloudsim.Cloudlet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CloudletExecutionData {
  public static void exportCloudletExecutionTime(List<Cloudlet> cloudlets, String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write("CloudletID,ExecutionTime,VMID\n");

      for (Cloudlet cloudlet : cloudlets) {
        double executionTime = cloudlet.getActualCPUTime();
        int cloudletId = cloudlet.getCloudletId();
        int vmId = cloudlet.getVmId();

        writer.write(String.format("%d,%.2f,%d\n", cloudletId, executionTime, vmId));
      }

      System.out.println("Cloudlet execution time data saved to: " + filePath);
    } catch (IOException e) {
      System.err.println("Error writing Cloudlet execution time data to CSV: " + e.getMessage());
    }
  }
}
