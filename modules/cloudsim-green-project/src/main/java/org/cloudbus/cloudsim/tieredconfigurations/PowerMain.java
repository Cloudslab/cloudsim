package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.workload.CloudletDataParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PowerMain {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static RealTimeSimulationManager simulationManager;

    public static void main(String[] args) {
        try {
            // First step: Initialize the CloudSim package.
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(numUsers, calendar, trace_flag);

            // Example power data, in real scenario this will come from the API
            PowerData initialPowerData = new PowerData();
            simulationManager = new RealTimeSimulationManager("SimulationTimeManager",initialPowerData);

            // Second step: Create Datacenters based on fossil fuel percentage
            Datacenter selectedDatacenter = selectDatacenterBasedOnPowerData(initialPowerData);
            CloudSim.addEntity(simulationManager);
            // simulationManager.startRealTimeUpdates();

            // Third step: Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Fourth step: Create one virtual machine
            vmList = new ArrayList<>();
            // List<Vm> vmList = createVMs();
            // List<Cloudlet> cloudletList = createCloudlets();

            // create VM and add vm to the vmList
            vmList.add(createVM(brokerId, 0));
            vmList.add(createVM(brokerId, 1));

            // submit vm list to the broker
            broker.submitGuestList(vmList);

            // Fifth step: Create one Cloudlet
            cloudletList = new ArrayList<>();

            // add the cloudlet to the list ######### To be replaced ########
            int id = 0;
            cloudletList.add(createCloudlet(id, brokerId, 0, 1000000));
            id++;
            cloudletList.add(createCloudlet(id, brokerId, 0, 10000000));
            id++;
            cloudletList.add(createCloudlet(id, brokerId, 0, 100000000));
            id++;
            cloudletList.add(createCloudlet(id, brokerId, 1, 1000000));
            id++;
            cloudletList.add(createCloudlet(id, brokerId, 1, 10000000));
            id++;
            cloudletList.add(createCloudlet(id, brokerId, 1, 100000000));

            //######### To be replaced ########
            //cloudletList = CloudletDataParser.parseCloudletData(filePath);


            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);

            // Set the amount of time to simulate here, it is in seconds, but it doesn't run for that time, it
            // simulates that amount of time instead, so 3600 is 1 hour, but your code will run in an instant
//            CloudSim.terminateSimulation(84000);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> resultList = broker.getCloudletReceivedList();
            printCloudletResults(resultList);

//            simulationManager.stopRealTimeUpdates();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Datacenter selectDatacenterBasedOnPowerData(PowerData powerData) throws Exception {
        double fossilFreePercentage = powerData.getFossilFreePercentage();
        Datacenter highResDatacenter = DatacenterFactory.createHighResourceDatacenter("High_Resource_Datacenter");
        Datacenter mediumResDatacenter = DatacenterFactory.createMediumResourceDatacenter("Medium_Resource_Datacenter");
        Datacenter lowResDatacenter = DatacenterFactory.createLowResourceDatacenter("Low_Resource_Datacenter");

        if (fossilFreePercentage > 70) {
            System.out.println("Selecting High Resource Datacenter...");
            return highResDatacenter;
        } else if (fossilFreePercentage > 35) {
            System.out.println("Selecting Medium Resource Datacenter...");
            return mediumResDatacenter;
        } else {
            System.out.println("Selecting Low Resource Datacenter...");
            return lowResDatacenter;
        }
    }

    //same as the one above, except this one creates the datacenters only when needed
//    public static Datacenter selectDatacenterBasedOnPowerData(PowerData powerData) throws Exception {
//        double fossilFreePercentage = powerData.getFossilFreePercentage();
//
//        if (fossilFreePercentage > 70) {
//            System.out.println("Selecting High Resource Datacenter...");
//            return DatacenterFactory.createHighResourceDatacenter("High_Resource_Datacenter");
//        } else if (fossilFreePercentage > 35) {
//            System.out.println("Selecting Medium Resource Datacenter...");
//            return DatacenterFactory.createMediumResourceDatacenter("Medium_Resource_Datacenter");
//        } else {
//            System.out.println("Selecting Low Resource Datacenter...");
//            return DatacenterFactory.createLowResourceDatacenter("Low_Resource_Datacenter");
//        }
//    }

    public static void printCloudletResults(List<Cloudlet> cloudlets) {
        String indent = "    ";
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data Center ID" + indent + "VM ID" + indent
                + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Memory Utilization");

        for (Cloudlet cloudlet : cloudlets) {
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent + "  ");

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                UtilizationModel memoryUtilizationModel = cloudlet.getUtilizationModelRam();
                double memoryUtilization = memoryUtilizationModel.getUtilization(CloudSim.clock());
                System.out.println("SUCCESS" + indent + indent + cloudlet.getResourceId() + indent + indent
                        + indent + cloudlet.getVmId() + indent + indent + Double.parseDouble(String.format("%.3f",cloudlet.getActualCPUTime())) + indent + indent
                        + cloudlet.getExecStartTime() + indent + indent + Double.parseDouble(String.format("%.3f",cloudlet.getFinishTime()))
                        + indent + indent + Double.parseDouble(String.format("%.3f",memoryUtilization)));
            }
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
        Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

        return vm;
    }

    public static Cloudlet createCloudlet(int id, int brokerId, int vmid, long length) {
        int cloudletId = id;
        // Cloudlet properties
//        long length = 400000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        // Full utilization model for CPU, RAM, and bandwidth
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet = new Cloudlet(cloudletId, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet.setUserId(brokerId); // Set the user ID for the cloudlet
        cloudlet.setGuestId(vmid);

        return cloudlet;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
}