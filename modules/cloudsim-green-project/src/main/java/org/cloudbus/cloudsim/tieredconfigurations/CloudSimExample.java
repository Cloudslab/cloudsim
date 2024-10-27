package org.cloudbus.cloudsim.tieredconfigurations;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class CloudSimExample {

    public static void main(String[] args) {
        try {
            // Step 1: Initialize the CloudSim library
            int numUsers = 1; // Number of cloud users
            CloudSim.init(numUsers, null, false);

            // Step 2: Create Datacenter, Broker, and VMs (as done in previous steps)

            // Create VMs (using the method from the previous steps)
            List<Vm> vmList = createVMs();

            // Step 3: Create Cloudlets
            List<Cloudlet> cloudletList = createCloudlets();

            // Step 4: Submit VMs and Cloudlets to the broker
            CustomDatacenterBroker broker = new CustomDatacenterBroker("Broker");

            // Submit the VMs to the broker
            broker.submitCustomVmList(vmList);

            // Submit the cloudlets to the broker
            broker.submitCustomCloudletList(cloudletList);

            // Bind Cloudlets to VMs
            // broker.bindCloudletsToVms(cloudletList, vmList);

            // load balancing
            EnergyAwareLoadBalancer loadBalancer = new EnergyAwareLoadBalancer();
            loadBalancer.balanceLoad(cloudletList, vmList);

            // Start the simulation
            CloudSim.startSimulation();

            // Retrieve the results after the simulation
            List<Cloudlet> resultList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            // Print the results
            printCloudletResults(resultList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create a list of VMs
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

    // Method to create a list of Cloudlets
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

    // Method to print Cloudlet results after the simulation
    private static void printCloudletResults(List<Cloudlet> list) {
        String indent = "    ";
        System.out.println();
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                System.out.println("SUCCESS" + indent + indent + cloudlet.getResourceId() + indent + indent + cloudlet.getVmId() + indent + indent + cloudlet.getActualCPUTime() + indent + indent + cloudlet.getExecStartTime() + indent + indent + cloudlet.getFinishTime());
            }
        }
    }
}
