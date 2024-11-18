// Written by Kevin Le (kevinle2)

package org.cloudbus.cloudsim.visualdata;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class DataCenterStats {

  public static void main(String[] args) throws IOException {
    String filePath = Paths.get("modules", "cloudsim-green-project", "src", "main", "java", "org", "cloudbus", "cloudsim", "visualdata", "datacenterStats.csv").toString();
    FileWriter writer = new FileWriter(filePath);

    // headers for python to parse data later
    writer.append("Datacenter,CPU Power (MIPS),RAM (MB),Storage (GB),Bandwidth (Mbps)\n");

    saveDatacenterStatistics(writer, "High Resource Datacenter", 2000, 32768, 2000, 20000);
    saveDatacenterStatistics(writer, "Medium Resource Datacenter", 1500, 16384, 1000, 10000);
    saveDatacenterStatistics(writer, "Low Resource Datacenter", 1000, 8192, 500, 5000);

    writer.close();
  }

  public static void saveDatacenterStatistics(FileWriter writer, String name, int mips, int ram, long storage, int bw) throws IOException {
    writer.append(name).append(",")
            .append(String.valueOf(mips)).append(",")
            .append(String.valueOf(ram)).append(",")
            .append(String.valueOf(storage)).append(",")
            .append(String.valueOf(bw)).append("\n");
  }
}
