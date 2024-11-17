package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.cloudbus.cloudsim.workload.BorgMapper; 

public class BorgPowerMain {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static Datacenter highResDatacenter;
    private static Datacenter mediumResDatacenter;
    private static Datacenter lowResDatacenter;

    public static void main(String[] args) {
        try {
            // Step 1: Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Load fossil-free percentages
            Queue<Double> fossilFreePercentages = loadFossilFreePercentages("/powerBreakdownData.csv");
            System.out.println(fossilFreePercentages);
            PowerData initialPowerData = new PowerData(fossilFreePercentages.poll(), 0);

            // Step 3: Create fixed datacenters
            highResDatacenter = DatacenterFactory.createHighResourceDatacenter("High_Resource_Datacenter");
            mediumResDatacenter = DatacenterFactory.createMediumResourceDatacenter("Medium_Resource_Datacenter");
            lowResDatacenter = DatacenterFactory.createLowResourceDatacenter("Low_Resource_Datacenter");

            // Step 4: Create broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Step 5: Initialize VM and Cloudlet lists
            vmList = new ArrayList<>();
            cloudletList = new ArrayList<>();

            broker.submitGuestList(vmList);
            broker.submitCloudletList(cloudletList);

            // Step 6: Load Borg dataset
            String borgFilePath = "/borg_traces_data.csv";
            int batchSize = 5;

            try (InputStream inputStream = BorgMapper.class.getResourceAsStream(borgFilePath);
                 BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                List<BorgMapper.BorgDatasetRow> batch = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    // Parse the line into a BorgDatasetRow object
                    BorgMapper.BorgDatasetRow row = BorgMapper.parseLineToRow(line);

                    if (row != null) {
                        batch.add(row);
                    }

                    if (batch.size() == batchSize) {
                        processBatch(batch, broker, brokerId);
                        batch.clear();
                    }
                }
                // Process any remaining rows
                if (!batch.isEmpty()) {
                    processBatch(batch, broker, brokerId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Step 7: Simulate for each hour
            Runnable monitor = () -> {
                double temp = 0;
                double hour = 0;
                String tempName = "";
                while (CloudSim.running()) {
                    if (CloudSim.clock() <= temp) {
//                        System.out.println("Clock: " + CloudSim.clock());
                        if(!fossilFreePercentages.isEmpty() && temp/3600 > hour) {
                            initialPowerData.setFossilFreePercentage(fossilFreePercentages.poll());
                            hour += 1;
                        }
                        initialPowerData.setFossilFreePercentage(initialPowerData.getFossilFreePercentage());
                        Datacenter selectedDatacenter = selectDatacenterBasedOnPowerData(initialPowerData);
                        if (!selectedDatacenter.getName().equals(tempName)) {
                            System.out.println(" ------- Hour: " + temp/3600 + " Update Datacenter: ("
                                    + selectedDatacenter.getName() + ") Simulating based on fossil-free percentage: "
                                    + initialPowerData.getFossilFreePercentage());
                            tempName = selectedDatacenter.getName();
                        }
                        else {
                            System.out.println(" ------- Hour: " + temp/3600 + " No Datacenter change based on " +
                                    "fossil-free percentage: " + initialPowerData.getFossilFreePercentage());
                        }
                        temp += 1800;
//                        if(temp > CloudSim.clock()) {temp = CloudSim.clock();}
                    }
                }
            };

            new Thread(monitor).start();

            // Step 7: Start Simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // Step 8: Print Results
            printCloudletResults(broker.getCloudletReceivedList());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processBatch(List<BorgMapper.BorgDatasetRow> batch, DatacenterBroker broker, int brokerId) {
        for (BorgMapper.BorgDatasetRow row : batch) {
            // Create VM and Cloudlet from the row
            Vm vm = new Vm(row.instanceIndex, brokerId, row.mips, row.pes, row.memory, 1000, 10000, "Xen", new CloudletSchedulerTimeShared());
            Cloudlet cloudlet = new Cloudlet(row.instanceIndex, row.cloudletLength, row.pes, row.cloudletFileSize, row.cloudletOutputSize,
                    new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(vm.getId());

            // Add VM and Cloudlet to the lists
            vmList.add(vm);
            cloudletList.add(cloudlet);
        }

        // Submit VMs and Cloudlets to the broker
        broker.submitGuestList(vmList);
        broker.submitCloudletList(cloudletList);
    }


    private static Datacenter selectDatacenterBasedOnPowerData(PowerData powerData) {
        double fossilFreePercentage = powerData.getFossilFreePercentage();
        if (fossilFreePercentage > 70) {
            return highResDatacenter;
        } else if (fossilFreePercentage > 35) {
            return mediumResDatacenter;
        } else {
            return lowResDatacenter;
        }
    }

    public static Vm createVM(int brokerId, int vmid) {
        int mips = 1000;
        long size = 10000; // Image size (MB)
        int ram = 512; // RAM (MB)
        long bw = 1000; // Bandwidth
        int pesNumber = 1; // Number of CPUs
        String vmm = "Xen"; // VMM name

        // Create the VM
        return new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
    }

    public static Cloudlet createCloudlet(int id, int brokerId, int vmid, long length) {
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet.setUserId(brokerId);
        cloudlet.setGuestId(vmid);
        return cloudlet;
    }

    private static void printCloudletResults(List<Cloudlet> cloudlets) {
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID    STATUS    Datacenter ID    VM ID    Time    Start Time    Finish Time");
        for (Cloudlet cloudlet : cloudlets) {
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                System.out.printf("%10d    SUCCESS    %12d    %5d    %4.2f    %9.2f    %10.2f%n",
                        cloudlet.getCloudletId(), cloudlet.getResourceId(), cloudlet.getVmId(),
                        cloudlet.getActualCPUTime(), cloudlet.getExecStartTime(), cloudlet.getFinishTime());
            }
        }
    }

    private static DatacenterBroker createBroker() throws Exception {
        return new DatacenterBroker("Broker");
    }

    public static Queue<Double> loadFossilFreePercentages(String filePath) {
        Queue<Double> percentages = new LinkedList<>();
        try (InputStream inputStream = PowerMain.class.getResourceAsStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] columns = line.split(",");
                double fossilFreePercentage = Double.parseDouble(columns[28].trim());
                percentages.add(fossilFreePercentage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return percentages;
    }
}
