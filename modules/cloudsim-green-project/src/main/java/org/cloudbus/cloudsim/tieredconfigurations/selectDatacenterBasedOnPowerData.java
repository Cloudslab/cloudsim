package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

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
            CustomDatacenterBroker broker = null;
            try {
                broker = new CustomDatacenterBroker("Broker");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create VMs and Cloudlets
            List<Vm> vmList = CloudSimExample.createVMs();
            List<Cloudlet> cloudletList = CloudSimExample.createCloudlets();

            // Submit VMs and Cloudlets to the broker
            broker.submitCustomVmList(vmList);
            broker.submitCustomCloudletList(cloudletList);

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
}