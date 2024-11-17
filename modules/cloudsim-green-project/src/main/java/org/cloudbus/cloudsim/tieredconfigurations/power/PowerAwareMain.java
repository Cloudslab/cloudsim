// Kevin Le (kevinle2)

package org.cloudbus.cloudsim.tieredconfigurations.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.tieredconfigurations.power.PowerAwareData;
import org.cloudbus.cloudsim.tieredconfigurations.power.PowerDatacenterFactory;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.workload.CloudletDataParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PowerAwareMain {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static PowerDatacenter highResDatacenter;
    private static PowerDatacenter mediumResDatacenter;
    private static PowerDatacenter lowResDatacenter;

    public static void main(String[] args) {
        try {
            // Step 1: Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Load fossil-free percentages
            Queue<Double> powerConsumptionTotals = loadPowerConsumptionTotals(
                    //"modules/cloudsim-green-project/src/main/java/org/cloudbus/cloudsim/energyapi/powerBreakdownData.csv"
                    "/powerBreakdownData.csv"
            );
            double min = powerConsumptionTotals.stream().min(Double::compareTo).orElse(0.0);
            double max = powerConsumptionTotals.stream().max(Double::compareTo).orElse(1.0);
            double avg = powerConsumptionTotals.stream().mapToDouble(Double::doubleValue).average().orElse((min + max) / 2);

            PowerAwareData initialPowerData = new PowerAwareData(powerConsumptionTotals.poll());

            // Step 3: Create fixed power datacenters
            highResDatacenter = PowerDatacenterFactory.createHighResourcePowerDatacenter("High_Resource_PowerDatacenter");
            mediumResDatacenter = PowerDatacenterFactory.createMediumResourcePowerDatacenter("Medium_Resource_PowerDatacenter");
            lowResDatacenter = PowerDatacenterFactory.createLowResourcePowerDatacenter("Low_Resource_PowerDatacenter");

            // Step 4: Create broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Step 5: Create VMs and Cloudlets
            vmList = new ArrayList<>();
            vmList.add(createPowerVM(brokerId, 0));
            vmList.add(createPowerVM(brokerId, 1));
            broker.submitGuestList(vmList);

            cloudletList = new ArrayList<>();
            int id = 0;
            cloudletList.add(createCloudlet(id++, brokerId, 0, 1000000));
            cloudletList.add(createCloudlet(id++, brokerId, 0, 10000000));
            cloudletList.add(createCloudlet(id++, brokerId, 0, 100000000));
            cloudletList.add(createCloudlet(id++, brokerId, 1, 1000000));
            cloudletList.add(createCloudlet(id++, brokerId, 1, 10000000));
            cloudletList.add(createCloudlet(id++, brokerId, 1, 100000000));
            broker.submitCloudletList(cloudletList);

            // Step 6: Simulate for each hour
            Runnable monitor = () -> {
                double temp = 0;
                double hour = 0;
                String tempName = "";
                while (CloudSim.running()) {
                    if (CloudSim.clock() <= temp) {
                        if (!powerConsumptionTotals.isEmpty() && temp / 3600 > hour) {
                            initialPowerData.setPowerConsumptionTotal(powerConsumptionTotals.poll());
                            hour += 1;
                        }
                        Datacenter selectedDatacenter = selectDatacenterBasedOnPowerData(initialPowerData.getPowerConsumptionTotal(), min, avg, max);
                        if (!selectedDatacenter.getName().equals(tempName)) {
                            System.out.println(" ------- Hour: " + temp / 3600 + " Update Datacenter: (" + selectedDatacenter.getName() + ") Simulating based on power consumption: " + initialPowerData.getPowerConsumptionTotal());
                            tempName = selectedDatacenter.getName();
                        } else {
                            System.out.println(" ------- Hour: " + temp / 3600 + " No Datacenter change based on power consumption: " + initialPowerData.getPowerConsumptionTotal());
                        }
                        temp += 1800;
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

    private static Datacenter selectDatacenterBasedOnPowerData(double powerConsumptionTotal, double min, double avg, double max) {
        double lowerThreshold = min + (avg - min) / 2; // Midpoint between min and avg
        double upperThreshold = avg + (max - avg) / 2; // Midpoint between avg and max

        if (powerConsumptionTotal > upperThreshold) {
            return highResDatacenter;
        } else if (powerConsumptionTotal > lowerThreshold) {
            return mediumResDatacenter;
        } else {
            return lowResDatacenter;
        }
    }

    public static PowerVm createPowerVM(int brokerId, int vmid) {
        int mips = 1000;
        long size = 10000; // Image size (MB)
        int ram = 512; // RAM (MB)
        long bw = 1000; // Bandwidth
        int pesNumber = 1; // Number of CPUs
        String vmm = "Xen"; // VMM name
        double schedulingInterval = 0.1;

        // Create the Power VM
        return new PowerVm(vmid, brokerId, mips, pesNumber, ram, bw, size, 1, vmm, new CloudletSchedulerTimeShared(), schedulingInterval);
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

    public static Queue<Double> loadPowerConsumptionTotals(String filePath) {
        Queue<Double> totals = new LinkedList<>();
        try (InputStream inputStream = PowerAwareMain.class.getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] columns = line.split(",");
                double powerConsumptionTotal = Double.parseDouble(columns[30].trim());
                totals.add(powerConsumptionTotal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totals;
    }
}