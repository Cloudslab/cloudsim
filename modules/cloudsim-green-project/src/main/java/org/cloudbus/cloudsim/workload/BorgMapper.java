package org.cloudbus.cloudsim.workload;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BorgMapper {

    static Map cpuCountMap = new HashMap();

    public static class BorgDatasetRow {
        public long time;
        public String instanceEventsType;
        public String collectionId;
        public String schedulingClass;
        public String collectionType;
        public int priority;
        public String allocCollectionId;
        public int instanceIndex;
        public String machineId;
        public double resourceRequest;
        public String constraint;
        public String collectionsEventsType;
        public String user;
        public String collectionName;
        public String collectionLogicalName;
        public String startAfterCollectionIds;
        public boolean verticalScaling;
        public String scheduler;
        public long startTime;
        public long endTime;
        public double averageUsage;
        public double maximumUsage;
        public double randomSampleUsage;
        public double assignedMemory;
        public double pageCacheMemory;
        public double cyclesPerInstruction;
        public double memoryAccessesPerInstruction;
        public double sampleRate;
        public double cpuUsageDistribution;
        public double tailCpuUsageDistribution;
        public String cluster;
        public String event;
        public boolean failed;
        public int cloudletLength;
        public int mips;
        public int cloudletId;
        public int cloudletFileSize;
        public int cloudletOutputSize;
        public double utilizationModelCpu;
        public double utilizationModelRam;
        public double utilizationModelBw;
        public int memory;
        public int pes;
    }

    public static class CloudLet {
        public int cloudletId;
        public int cloudletLength;
        public int pesNumber;
        public int cloudletFileSize;
        public int cloudletOutputSize;
        public double utilizationModelCpu; // utilizationModelCpu
        public double utilizationModelRam; // utilizationModelRam
        public double utilizationModelBw; // utilizationModelBw

        public CloudLet(int cloudletId, int cloudletLength, int pesNumber, int cloudletFileSize, int cloudletOutputSize,
                double utilizationModelCpu, double utilizationModelRam, double utilizationModelBw) {
            this.cloudletId = cloudletId;
            this.cloudletLength = cloudletLength;
            this.pesNumber = pesNumber;
            this.cloudletFileSize = cloudletFileSize;
            this.cloudletOutputSize = cloudletOutputSize;
            this.utilizationModelCpu = utilizationModelCpu;
            this.utilizationModelRam = utilizationModelRam;
            this.utilizationModelBw = utilizationModelBw;
        }
    }

    public static class Vm {

        /** The VM unique id. */
        public final int id;

        /** The user id. */
        public int userId;

        /**
         * A Unique Identifier (UID) for the VM, that is compounded by the user id and
         * VM id.
         */
        public double uid;

        /**
         * The size the VM image size (the amount of storage it will use, at least
         * initially).
         */
        public int size;

        /** The MIPS capacity of each VM's PE. */
        public int mips;

        /** The number of PEs required by the VM. */
        public int numberOfPes;

        /** The required ram. */
        public int ram;

        /** The required bw. */
        public int bw;

        public Vm(int id, int mips, int numberOfPes, int ram, int bw, int size) {
            this.id = id;
            this.userId = (int) Math.round(2 * new Random().nextDouble());
            this.mips = mips;
            this.numberOfPes = numberOfPes;
            this.ram = ram;
            this.bw = bw;
            this.size = size;
        }
    }

    public static String mapRowToJson(BorgDatasetRow row) {

        // Create CloudLet instance
        CloudLet cloudlet = new CloudLet(
                row.cloudletId, // cloudletId
                row.cloudletLength, // cloudletLength
                row.mips, // pesNumber
                row.cloudletFileSize, // cloudletFileSize
                row.cloudletOutputSize, // cloudletOutputSize
                row.utilizationModelCpu, // utilizationModelCpu
                row.utilizationModelRam, // utilizationModelRam
                row.utilizationModelBw // utilizationModelBw
        );
        // System.out.println("Cloudlet: " + cloudlet.cloudletId);
        // Create Vm instance with updated MIPS and CPU utilization
        Vm vm = new Vm(
                row.instanceIndex, // id
                row.mips, // mips
                row.pes, // mips
                row.memory, // numberOfPes
                1000, // ram
                10000);
        // System.out.println("Vm: " + vm.id);
        // Convert to JSON
        Gson gson = new Gson();
        String cloudletJson = gson.toJson(cloudlet);
        String vmJson = gson.toJson(vm);

        // Combine Cloudlet and Vm JSON
        String combinedJson = "{ \"cloudlet\": " + cloudletJson + ", \"vm\": " + vmJson + " }";

        return combinedJson;
    }

    public static void acceptJson(String jsonPayload) {
        // Implement the REST call here
        // For example, using HttpClient to send the JSON payload
    }

    public static void main(String[] args) {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        String filePath = "/borg_traces_data.csv"; // Path to the dataset file
        int batchSize = 5;

        try (InputStream inputStream = BorgMapper.class.getResourceAsStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            List<BorgDatasetRow> batch = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                // System.out.println(line.indexOf("instance_events_type") + " Reading file
                // line" + line);

                // Parse the line into a BorgDatasetRow object
                BorgDatasetRow row = parseLineToRow(line);
                // System.out.println(batch.size() + " Row: " + row);

                if (row != null) {
                    batch.add(row);
                }

                if (batch.size() == batchSize) {
                    // System.out.println("Processing batch of size: " + batch.size());
                    processBatch(batch);
                    batch.clear();
                }

            }
            System.out.println("CPU Count Map: " + cpuCountMap);
            // Process any remaining rows
            if (!batch.isEmpty()) {
                processBatch(batch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BorgDatasetRow parseLineToRow(String line) {
        // Implement the logic to parse a line from the CSV file into a BorgDatasetRow
        // object
        // This is a placeholder implementation
        BorgDatasetRow row = null;
        String[] values = line.split(",");

        Pattern pattern = Pattern.compile("'cpus':\s*([0-9.]+),\s*'memory':\s*([0-9.]+)");

        // System.out.println("Values: " + values.length + line.indexOf("cpu"));
        if (values.length == 12 && line.indexOf("cpu") != -1) {
            row = new BorgDatasetRow();
            row.cloudletId = Integer.parseInt(values[0]);
            row.instanceIndex = values[2].isEmpty() ? 0 : Integer.parseInt(values[2]);
            double cpuValue = 0.0, memoryValue = 0.0;
            int cpu, memory, mips;
            cpuValue = Double.parseDouble(values[10].substring(values[10].indexOf(": ") + 1, values[10].length()));
            memoryValue = Double
                    .parseDouble(values[11].substring(values[11].indexOf(": ") + 1, values[11].length() - 2));
            // System.out.println(values[10].substring(values[10].indexOf(' ')+1,
            // values[10].length()) + " CPU: " + cpuValue + " Memory: " + memoryValue);

            if (cpuValue >= 0.00016 && cpuValue <= 0.19352) {
                cpu = 2;
                memory = 4 * 1024; // 4 GB in MB
                mips = 1000; // Example MIPS value for this range

            } else if (cpuValue > 0.19352 && cpuValue <= 0.38704) {
                cpu = 4;
                memory = 8 * 1024; // 8 GB in MB
                mips = 2000; // Example MIPS value for this range
            } else if (cpuValue > 0.38704 && cpuValue <= 0.58) {
                cpu = 8;
                memory = 16 * 1024; // 16 GB in MB
                mips = 4000; // Example MIPS value for this range
            } else {
                cpu = 1;
                memory = 1024; // Default to assigned memory if out of range
                mips = 500; // Default MIPS value
            }
            cpuCountMap.put(cpu, (Integer) cpuCountMap.getOrDefault(cpu, 0) + 1);
            row.cloudletLength = mips;
            row.pes = cpu;
            row.mips = mips;
            row.memory = memory;
            row.cloudletFileSize = 300;
            row.cloudletOutputSize = 300;
            row.utilizationModelCpu = cpuValue * 1000 / cpu;
            row.utilizationModelRam = memoryValue * 1000 / memory;
            row.utilizationModelBw = 1;
        }
        return row;
    }

    private static void processBatch(List<BorgDatasetRow> batch) {
        for (BorgDatasetRow row : batch) {
            String jsonPayload = mapRowToJson(row);
            System.out.println("JSON Payload: " + jsonPayload);
            acceptJson(jsonPayload);
        }
    }

}
