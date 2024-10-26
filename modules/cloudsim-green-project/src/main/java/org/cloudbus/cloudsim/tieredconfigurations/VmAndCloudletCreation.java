package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VmAndCloudletCreation {

    private static final int VM_COUNT = 5;
    private static final int CLOUDLET_COUNT = 10;

    public static void main(String[] args) {
        // Initialize CloudSim library
        int numUsers = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;

        CloudSim.init(numUsers, calendar, traceFlag);

        // Create Custom DatacenterBroker
        CustomDatacenterBroker broker = null;
        try {
            broker = new CustomDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create VMs and Cloudlets
        List<Vm> vmList = createVMs(broker.getId(), VM_COUNT);
        List<Cloudlet> cloudletList = createCloudlets(broker.getId(), CLOUDLET_COUNT);

        // Submit VMs and Cloudlets using custom broker methods
        broker.submitCustomVmList(vmList);
        broker.submitCustomCloudletList(cloudletList);

        // Bind Cloudlets to VMs
        broker.bindCloudletsToVms(cloudletList, vmList);

        // Start the simulation
        CloudSim.startSimulation();

        // Stop the simulation
        CloudSim.stopSimulation();
    }

    private static List<Vm> createVMs(int userId, int vmCount) {
        List<Vm> vmList = new ArrayList<>();
        int mips = 1000;
        int ram = 2048;
        long storage = 10000;
        int bw = 1000;
        int pesNumber = 1;

        for (int i = 0; i < vmCount; i++) {
            Vm vm = new Vm(i, userId, mips, pesNumber, ram, bw, storage, "Xen", new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        return vmList;
    }

    private static List<Cloudlet> createCloudlets(int userId, int cloudletCount) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;

        for (int i = 0; i < cloudletCount; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(userId);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }
}
