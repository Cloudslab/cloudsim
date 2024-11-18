package org.cloudbus.cloudsim.visualdata;

import org.cloudbus.cloudsim.Cloudlet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CloudletToCSV {
  public static void logResultsToCsv(List<Cloudlet> cloudlets, String fileName) {
    try (FileWriter writer = new FileWriter(fileName)) {
      writer.append("CloudletID,Status,DatacenterID,VMID,ExecutionTime,StartTime,FinishTime\n");
      for (Cloudlet cloudlet : cloudlets) {
        if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
          writer.append(String.format("%d,SUCCESS,%d,%d,%.2f,%.2f,%.2f\n",
                  cloudlet.getCloudletId(),
                  cloudlet.getResourceId(),
                  cloudlet.getVmId(),
                  cloudlet.getActualCPUTime(),
                  cloudlet.getExecStartTime(),
                  cloudlet.getFinishTime()));
        }
      }
      System.out.println("Results logged to " + fileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
