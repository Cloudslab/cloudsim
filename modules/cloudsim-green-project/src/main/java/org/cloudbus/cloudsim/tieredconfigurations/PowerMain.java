package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.List;

class PowerMain {
    public static Datacenter selectDatacenterBasedOnPowerData(PowerData powerData) throws Exception {
        double fossilFreePercentage = powerData.getFossilFreePercentage();

        if (fossilFreePercentage > 70) {
            System.out.println("Selecting High Resource Datacenter...");
            return DatacenterFactory.createHighResourceDatacenter("High_Resource_Datacenter");
        } else if (fossilFreePercentage > 35) {
            System.out.println("Selecting Medium Resource Datacenter...");
            return DatacenterFactory.createMediumResourceDatacenter("Medium_Resource_Datacenter");
        } else {
            System.out.println("Selecting Low Resource Datacenter...");
            return DatacenterFactory.createLowResourceDatacenter("Low_Resource_Datacenter");
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize the CloudSim library
            int numUsers = 1;
            CloudSim.init(numUsers, null, false);

            // Example power data, in real scenario this will come from the API
            PowerData powerData = new PowerData();

            // Select the datacenter based on fossilFreePercentage
            Datacenter selectedDatacenter = selectDatacenterBasedOnPowerData(powerData);

            // Create broker
            DatacenterBroker broker = null;
            try {
                broker = new DatacenterBroker("Broker");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create VMs and Cloudlets
            List<Vm> vmList = createVMs();
            List<Cloudlet> cloudletList = createCloudlets();

            // Submit VMs and Cloudlets to the broker
            broker.submitGuestList(vmList);
            broker.submitCloudletList(cloudletList);

            // Start simulation
            CloudSim.startSimulation();

            // Get and print the results
            List<Cloudlet> resultList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();
            printCloudletResults(resultList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printCloudletResults(List<Cloudlet> cloudlets) {
        String indent = "    ";
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent
                + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : cloudlets) {
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                System.out.println("SUCCESS" + indent + indent + cloudlet.getResourceId() + indent + indent
                        + cloudlet.getVmId() + indent + indent + cloudlet.getActualCPUTime() + indent + indent
                        + cloudlet.getExecStartTime() + indent + indent + cloudlet.getFinishTime());
            }
        }
    }

    public static List<Vm> createVMs() {
        int vmid = 0;
        int numVMs = 4;
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numVMs; i++) {
            int mips = 1000;
            long size = 10000; // Image size (MB)
            int ram = 512; // RAM (MB)
            long bw = 1000; // Bandwidth
            int pesNumber = 1; // Number of CPUs
            String vmm = "Xen"; // VMM name

            // Create the VM
            Vm vm = new Vm(vmid++, 0, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }

        return vmList;
    }

    public static List<Cloudlet> createCloudlets() {
        int cloudletId = 0;
        int numCloudlets = 10;
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Cloudlet properties
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;

        // Full utilization model for CPU, RAM, and bandwidth
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < numCloudlets; i++) {
            Cloudlet cloudlet = new Cloudlet(cloudletId++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(0); // Set the user ID for the cloudlet
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
}
