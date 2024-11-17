package org.cloudbus.cloudsim.visualdata;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatacenterSelectionData {
  public static void saveDatacenterSelectionCSV(String filePath, List<String[]> datacenterLogs) {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write("Time,Datacenter,FossilFreePercentage\n");
      for (String[] log : datacenterLogs) {
        writer.write(String.join(",", log) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
