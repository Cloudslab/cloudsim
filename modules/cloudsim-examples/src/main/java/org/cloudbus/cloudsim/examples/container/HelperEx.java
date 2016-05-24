package org.cloudbus.cloudsim.examples.container;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.CotainerPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisionerSimple;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.util.MathUtil;

import java.io.*;
import java.io.File;
import java.util.*;

/**
 * This is the modified version of {@link org.cloudbus.cloudsim.examples.power.Helper} class in cloudsim.
 * Created by sareh on 15/07/15.
 */

public class HelperEx {

    public HelperEx() {
//        System.out.print();

    }


    public static List<ContainerCloudlet> createContainerCloudletList(int brokerId, String inputFolderName, int numberOfCloudlets)
            throws FileNotFoundException {
        ArrayList cloudletList = new ArrayList();
        long fileSize = 300L;
        long outputSize = 300L;
        UtilizationModelNull utilizationModelNull = new UtilizationModelNull();
        File inputFolder1 = new File(inputFolderName);
        File[] files1 = inputFolder1.listFiles();
        int createdCloudlets = 0;
        for (File aFiles1 : files1) {
            File inputFolder = new File(aFiles1.toString());
            File[] files = inputFolder.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (createdCloudlets < numberOfCloudlets) {
                    ContainerCloudlet cloudlet = null;

                    try {
                        cloudlet = new ContainerCloudlet(IDs.pollId(ContainerCloudlet.class), 216000000L * 1000, 1, fileSize, outputSize,
                                new UtilizationModelPlanetLabInMemoryExtended(files[i].getAbsolutePath(), 300.0D),
                                utilizationModelNull, utilizationModelNull);
                    } catch (Exception var13) {
                        var13.printStackTrace();
                        System.exit(0);
                    }

                    cloudlet.setUserId(brokerId);
                    //            cloudlet.setVmId(i);
                    cloudletList.add(cloudlet);
                    createdCloudlets += 1;
                } else {

                    return cloudletList;
                }
            }

        }
        return cloudletList;
    }

    // create the containers for hosting the cloudlets and binding them together.
    public static List<Container> createContainerList(int brokerId, int containersNumber) {
        ArrayList containers = new ArrayList();

        for (int i = 0; i < containersNumber; ++i) {
//            int containerType = new RandomGen().getNum(ConstantsExamples.CONTAINER_TYPES);
            int containerType = i / (int) Math.ceil((double) containersNumber / 3.0D);
//            int containerType = 0;

            containers.add(new PowerContainer(IDs.pollId(Container.class), brokerId, (double) ConstantsExamples.CONTAINER_MIPS[containerType], ConstantsExamples.
                    CONTAINER_PES[containerType], ConstantsExamples.CONTAINER_RAM[containerType], ConstantsExamples.CONTAINER_BW, 0L, "Xen",
                    new ContainerCloudletSchedulerDynamicWorkload(ConstantsExamples.CONTAINER_MIPS[containerType],
                            ConstantsExamples.CONTAINER_PES[containerType]), ConstantsExamples.SCHEDULING_INTERVAL));
        }

        return containers;
    }

    // create the containers for hosting the cloudlets and binding them together.
    public static List<ContainerVm> createVmList(int brokerId, int containerVmsNumber) {
        ArrayList containerVms = new ArrayList();

        for (int i = 0; i < containerVmsNumber; ++i) {
            ArrayList peList = new ArrayList();
//            int vmType = new RandomGen().getNum(ConstantsExamples.VM_TYPES);
//            Log.print(vmType);
//            Log.print("\n");
            int vmType = i / (int) Math.ceil((double) containerVmsNumber / 4.0D);
//            int vmType = 1;
            //            int vmType = 1;

            for (int j = 0; j < ConstantsExamples.VM_PES[vmType]; ++j) {
                peList.add(new ContainerPe(j, new CotainerPeProvisionerSimple((double) ConstantsExamples.VM_MIPS[vmType])));
            }
            containerVms.add(new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId, (double) ConstantsExamples.VM_MIPS[vmType], (float) ConstantsExamples.VM_RAM[vmType],
                    ConstantsExamples.VM_BW, ConstantsExamples.VM_SIZE, "Xen", new ContainerSchedulerTimeSharedOverSubscription(peList),
                    new ContainerRamProvisionerSimple(ConstantsExamples.VM_RAM[vmType]),
                    new ContainerBwProvisionerSimple(ConstantsExamples.VM_BW), peList, ConstantsExamples.SCHEDULING_INTERVAL));


        }

        return containerVms;
    }


    public static List<ContainerHost> createHostList(int hostsNumber) {
        ArrayList hostList = new ArrayList();
        for (int i = 0; i < hostsNumber; ++i) {
//            int hostType =  new RandomGen().getNum(ConstantsExamples.HOST_TYPES);
            int hostType = i / (int) Math.ceil((double) hostsNumber / 3.0D);
//            int hostType = i % 2;
//            int hostType = 2;
            ArrayList peList = new ArrayList();

            for (int j = 0; j < ConstantsExamples.HOST_PES[hostType]; ++j) {
                peList.add(new ContainerVmPe(j, new ContainerVmPeProvisionerSimple((double) ConstantsExamples.HOST_MIPS[hostType])));
            }

//            hostList.add(new PowerContainerHost(i, new ContainerVmRamProvisionerSimple(ConstantsExamples.HOST_RAM[hostType]),
//                    new ContainerVmBwProvisionerSimple(1000000L), 1000000L, peList, new ContainerVmSchedulerTimeSharedOverSubscription(peList), ConstantsExamples.HOST_POWER[hostType]));
            hostList.add(new PowerContainerHostUtilizationHistory(IDs.pollId(ContainerHost.class), new ContainerVmRamProvisionerSimple(ConstantsExamples.HOST_RAM[hostType]),
                    new ContainerVmBwProvisionerSimple(1000000L), 1000000L, peList, new ContainerVmSchedulerTimeSharedOverSubscription(peList), ConstantsExamples.HOST_POWER[hostType]));
        }

        return hostList;
    }


    // Broker

    public static ContainerDatacenterBroker createBroker(double overBookingFactor) {
        ContainerDatacenterBroker broker = null;

        try {
            broker = new ContainerDatacenterBroker("Broker", overBookingFactor);
        } catch (Exception var2) {
            var2.printStackTrace();
            System.exit(0);
        }

        return broker;
    }

    //    // Data Center
//    public static PowerContainerDatacenter createDatacenter(String name, List<ContainerHost> hostList, ContainerVmAllocationPolicy vmAllocationPolicy, ContainerAllocationPolicy containerAllocationPolicy) throws Exception {
//        String arch = "x86";
//        String os = "Linux";
//        String vmm = "Xen";
//        double time_zone = 10.0D;
//        double cost = 3.0D;
//        double costPerMem = 0.05D;
//        double costPerStorage = 0.001D;
//        double costPerBw = 0.0D;
//        ContainerDatacenterCharacteristics characteristics = new ContainerDatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
//        PowerContainerDatacenter datacenter = null;
////        datacenter = new PowerContainerDatacenter(name,characteristics, vmAllocationPolicy, containerAllocationPolicy , new LinkedList(),Double.valueOf(300.0D));
//        datacenter = new PowerContainerDatacenterCM(name,characteristics, vmAllocationPolicy, containerAllocationPolicy , new LinkedList(),Double.valueOf(300.0D));
//
//        return datacenter;
//    }
    // Data Center
//    public static ContainerDatacenter createDatacenter(String name, Class<? extends ContainerDatacenter> datacenterClass, List<ContainerHost> hostList, ContainerVmAllocationPolicy vmAllocationPolicy, ContainerAllocationPolicy containerAllocationPolicy, String experimentName, String logAddress) throws Exception {
//        String arch = "x86";
//        String os = "Linux";
//        String vmm = "Xen";
//        double time_zone = 10.0D;
//        double cost = 3.0D;
//        double costPerMem = 0.05D;
//        double costPerStorage = 0.001D;
//        double costPerBw = 0.0D;
//        ContainerDatacenterCharacteristics characteristics = new ContainerDatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
//        ContainerDatacenter datacenter = null;
//        try {
//            datacenter = datacenterClass.getConstructor(
//                    String.class,
//                    ContainerDatacenterCharacteristics.class,
//                    ContainerVmAllocationPolicy.class,
//                    ContainerAllocationPolicy.class,
//                    List.class,
//                    Double.TYPE, String.class, String.class
//            ).newInstance(
//                    name,
//                    characteristics,
//                    vmAllocationPolicy,
//                    containerAllocationPolicy,
//                    new LinkedList<Storage>(),
//                    ConstantsExamples.SCHEDULING_INTERVAL, experimentName, logAddress);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(0);
//        }
////        datacenter = new PowerContainerDatacenter(name,characteristics, vmAllocationPolicy, containerAllocationPolicy , new LinkedList(),Double.valueOf(300.0D));
////        datacenter = new PowerContainerDatacenterCM(name,characteristics, vmAllocationPolicy, containerAllocationPolicy , new LinkedList(),Double.valueOf(300.0D));
//
//        return datacenter;
//    }


    /**
     * Create the data center
     *
     * @param name
     * @param datacenterClass
     * @param hostList
     * @param vmAllocationPolicy
     * @param containerAllocationPolicy
     * @param experimentName
     * @param logAddress
     * @return
     * @throws Exception
     */

    public static ContainerDatacenter createDatacenter(String name, Class<? extends ContainerDatacenter> datacenterClass,
                                                       List<ContainerHost> hostList,
                                                       ContainerVmAllocationPolicy vmAllocationPolicy,
                                                       ContainerAllocationPolicy containerAllocationPolicy,
                                                       String experimentName, double schedulingInterval, String logAddress, double VMStartupDelay,
                                                       double ContainerStartupDelay) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0D;
        double cost = 3.0D;
        double costPerMem = 0.05D;
        double costPerStorage = 0.001D;
        double costPerBw = 0.0D;
        ContainerDatacenterCharacteristics characteristics = new
                ContainerDatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage,
                costPerBw);
        ContainerDatacenter datacenter = new PowerContainerDatacenterCM(name, characteristics, vmAllocationPolicy,
                containerAllocationPolicy, new LinkedList<Storage>(), schedulingInterval, experimentName, logAddress,
                VMStartupDelay, ContainerStartupDelay);

        return datacenter;
    }
    /**
     * Prints the results.
     *
     * @param datacenter     the datacenter
     * @param lastClock      the last clock
     * @param experimentName the experiment name
     * @param outputInCsv    the output in csv
     * @param outputFolder   the output folder
     */
    public static void printResults(
            PowerContainerDatacenter datacenter,
            List<ContainerVm> vms,
            List<Container> containers,
            double lastClock,
            String experimentName,
            boolean outputInCsv,
            String outputFolder) {
        Log.enable();
        List<ContainerHost> hosts = datacenter.getHostList();

        int numberOfHosts = hosts.size();
        int numberOfVms = vms.size();

        double totalSimulationTime = lastClock;
        double energy = datacenter.getPower() / (3600 * 1000);
        int numberOfVmMigrations = datacenter.getVmMigrationCount();

        Map<String, Double> slaMetrics = getSlaMetrics(vms);

        double slaOverall = slaMetrics.get("overall");
        double slaAverage = slaMetrics.get("average");
        double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
        // double slaTimePerVmWithMigration = slaMetrics.get("sla_time_per_vm_with_migration");
        // double slaTimePerVmWithoutMigration =
        // slaMetrics.get("sla_time_per_vm_without_migration");
        // double slaTimePerHost = getSlaTimePerHost(hosts);
        double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

        double sla = slaTimePerActiveHost * slaDegradationDueToMigration;

        List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);

        int numberOfHostShutdowns = timeBeforeHostShutdown.size();

        double meanTimeBeforeHostShutdown = Double.NaN;
        double stDevTimeBeforeHostShutdown = Double.NaN;
        if (!timeBeforeHostShutdown.isEmpty()) {
            meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
            stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
        }

        List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
        double meanTimeBeforeVmMigration = Double.NaN;
        double stDevTimeBeforeVmMigration = Double.NaN;
        if (!timeBeforeVmMigration.isEmpty()) {
            meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
            stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
        }
        List<Double> timeBeforeContainerMigration = getTimesBeforeContainerMigration(containers);
        double meanTimeBeforeContainerMigration = Double.NaN;
        double stDevTimeBeforeContainerMigration = Double.NaN;
        if (!timeBeforeContainerMigration.isEmpty()) {
            meanTimeBeforeContainerMigration = MathUtil.mean(timeBeforeContainerMigration);
            stDevTimeBeforeContainerMigration = MathUtil.stDev(timeBeforeContainerMigration);
        }

        if (outputInCsv) {
            // We create the logging folders
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }
            File folder1 = new File(outputFolder + "/stats");
            if (!folder1.exists()) {
                folder1.mkdir();
            }
            File folder2 = new File(outputFolder + "/time_before_host_shutdown");
            if (!folder2.exists()) {
                folder2.mkdir();
            }
            File folder3 = new File(outputFolder + "/time_before_vm_migration");
            if (!folder3.exists()) {
                folder3.mkdir();
            }
            File folder4 = new File(outputFolder + "/metrics");
            if (!folder4.exists()) {
                folder4.mkdir();
            }


            StringBuilder data = new StringBuilder();
            String delimeter = ",";

            data.append(experimentName + delimeter);
            data.append(parseExperimentName(experimentName));
            data.append(String.format("%d", numberOfHosts) + delimeter);
            data.append(String.format("%d", numberOfVms) + delimeter);
            data.append(String.format("%.2f", totalSimulationTime) + delimeter);
            data.append(String.format("%.5f", energy) + delimeter);
//            data.append(String.format("%d", numberOfMigrations) + delimeter);
            data.append(String.format("%.10f", sla) + delimeter);
            data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
            data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
            data.append(String.format("%.10f", slaOverall) + delimeter);
            data.append(String.format("%.10f", slaAverage) + delimeter);
//             data.append(String.format("%.5f", slaTimePerVmWithMigration) + delimeter);
            // data.append(String.format("%.5f", slaTimePerVmWithoutMigration) + delimeter);
            // data.append(String.format("%.5f", slaTimePerHost) + delimeter);
            data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
            data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
            data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
            data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
            data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);

            if (datacenter.getVmAllocationPolicy() instanceof PowerContainerVmAllocationPolicyMigrationAbstract) {
                PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerContainerVmAllocationPolicyMigrationAbstract) datacenter
                        .getVmAllocationPolicy();

                double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryVmSelection());
                double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryVmSelection());
                double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryHostSelection());
                double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryHostSelection());
                double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryVmReallocation());
                double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryVmReallocation());
                double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryTotal());
                double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryTotal());

                data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
                data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
                data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
                data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
                data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
                data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
                data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
                data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);

                writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName
                        + "_metric");
            }

            data.append("\n");

            writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
            writeDataColumn(timeBeforeHostShutdown, outputFolder + "/time_before_host_shutdown/"
                    + experimentName + "_time_before_host_shutdown.csv");
            writeDataColumn(timeBeforeContainerMigration, outputFolder + "/time_before_vm_migration/"
                    + experimentName + "_time_before_vm_migration.csv");

        } else {
            Log.setDisabled(false);
            Log.printLine();
            Log.printLine(String.format("Experiment name: " + experimentName));
            Log.printLine(String.format("Number of hosts: " + numberOfHosts));
            Log.printLine(String.format("Number of VMs: " + numberOfVms));
            Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
            Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
//            Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
            Log.printLine(String.format("SLA: %.5f%%", sla * 100));
            Log.printLine(String.format(
                    "SLA perf degradation due to migration: %.2f%%",
                    slaDegradationDueToMigration * 100));
            Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
            Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
            Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
            // Log.printLine(String.format("SLA time per VM with migration: %.2f%%",
            // slaTimePerVmWithMigration * 100));
            // Log.printLine(String.format("SLA time per VM without migration: %.2f%%",
            // slaTimePerVmWithoutMigration * 100));
            // Log.printLine(String.format("SLA time per host: %.2f%%", slaTimePerHost * 100));
            Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
            Log.printLine(String.format(
                    "Mean time before a host shutdown: %.2f sec",
                    meanTimeBeforeHostShutdown));
            Log.printLine(String.format(
                    "StDev time before a host shutdown: %.2f sec",
                    stDevTimeBeforeHostShutdown));
            Log.printLine(String.format(
                    "Mean time before a VM migration: %.2f sec",
                    meanTimeBeforeVmMigration));
            Log.printLine(String.format(
                    "StDev time before a VM migration: %.2f sec",
                    stDevTimeBeforeVmMigration));
            Log.printLine(String.format(
                    "Mean time before a Container migration: %.2f sec",
                    meanTimeBeforeContainerMigration));
            Log.printLine(String.format(
                    "StDev time before a Container migration: %.2f sec",
                    stDevTimeBeforeContainerMigration));

            if (datacenter.getVmAllocationPolicy() instanceof PowerContainerVmAllocationPolicyMigrationAbstract) {
                PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerContainerVmAllocationPolicyMigrationAbstract) datacenter
                        .getVmAllocationPolicy();

                double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryVmSelection());
                double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryVmSelection());
                double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryHostSelection());
                double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryHostSelection());
                double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryVmReallocation());
                double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryVmReallocation());
                double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
                        .getExecutionTimeHistoryTotal());
                double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
                        .getExecutionTimeHistoryTotal());

                Log.printLine(String.format(
                        "Execution time - VM selection mean: %.5f sec",
                        executionTimeVmSelectionMean));
                Log.printLine(String.format(
                        "Execution time - VM selection stDev: %.5f sec",
                        executionTimeVmSelectionStDev));
                Log.printLine(String.format(
                        "Execution time - host selection mean: %.5f sec",
                        executionTimeHostSelectionMean));
                Log.printLine(String.format(
                        "Execution time - host selection stDev: %.5f sec",
                        executionTimeHostSelectionStDev));
                Log.printLine(String.format(
                        "Execution time - VM reallocation mean: %.5f sec",
                        executionTimeVmReallocationMean));
                Log.printLine(String.format(
                        "Execution time - VM reallocation stDev: %.5f sec",
                        executionTimeVmReallocationStDev));
                Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
                Log.printLine(String
                        .format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
            }
            Log.printLine();
        }

        Log.setDisabled(true);
    }

    /**
     * Gets the times before vm migration.
     *
     * @param vms the vms
     * @return the times before vm migration
     */
    public static List<Double> getTimesBeforeVmMigration(List<ContainerVm> vms) {
        List<Double> timeBeforeVmMigration = new LinkedList<Double>();
        for (ContainerVm vm : vms) {
            boolean previousIsInMigration = false;
            double lastTimeMigrationFinished = 0;
            for (VmStateHistoryEntry entry : vm.getStateHistory()) {
                if (previousIsInMigration == true && entry.isInMigration() == false) {
                    timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
                }
                if (previousIsInMigration == false && entry.isInMigration() == true) {
                    lastTimeMigrationFinished = entry.getTime();
                }
                previousIsInMigration = entry.isInMigration();
            }
        }
        return timeBeforeVmMigration;
    }

    /**
     * Gets the times before vm migration.
     *
     * @param containers the vms
     * @return the times before vm migration
     */
    public static List<Double> getTimesBeforeContainerMigration(List<Container> containers) {
        List<Double> timeBeforeVmMigration = new LinkedList<Double>();
        for (Container container : containers) {
            boolean previousIsInMigration = false;
            double lastTimeMigrationFinished = 0;
            for (VmStateHistoryEntry entry : container.getStateHistory()) {
                if (previousIsInMigration == true && entry.isInMigration() == false) {
                    timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
                }
                if (previousIsInMigration == false && entry.isInMigration() == true) {
                    lastTimeMigrationFinished = entry.getTime();
                }
                previousIsInMigration = entry.isInMigration();
            }
        }
        return timeBeforeVmMigration;
    }

    /**
     * Gets the times before host shutdown.
     *
     * @param hosts the hosts
     * @return the times before host shutdown
     */
    public static List<Double> getTimesBeforeHostShutdown(List<ContainerHost> hosts) {
        List<Double> timeBeforeShutdown = new LinkedList<Double>();
        for (ContainerHost host : hosts) {
            boolean previousIsActive = true;
            double lastTimeSwitchedOn = 0;
            for (HostStateHistoryEntry entry : ((ContainerHostDynamicWorkload) host).getStateHistory()) {
                if (previousIsActive == true && entry.isActive() == false) {
                    timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
                }
                if (previousIsActive == false && entry.isActive() == true) {
                    lastTimeSwitchedOn = entry.getTime();
                }
                previousIsActive = entry.isActive();
            }
        }
        return timeBeforeShutdown;
    }


    /**
     * Parses the experiment name.
     *
     * @param name the name
     * @return the string
     */
    public static String parseExperimentName(String name) {
        Scanner scanner = new Scanner(name);
        StringBuilder csvName = new StringBuilder();
        scanner.useDelimiter("_");
        for (int i = 0; i < 8; i++) {
            if (scanner.hasNext()) {
                csvName.append(scanner.next() + ",");
            } else {
                csvName.append(",");
            }
        }
        scanner.close();
        return csvName.toString();
    }

    /**
     * Gets the sla time per active host.
     *
     * @param hosts the hosts
     * @return the sla time per active host
     */
    protected static double getSlaTimePerActiveHost(List<ContainerHost> hosts) {
        double slaViolationTimePerHost = 0;
        double totalTime = 0;

        for (ContainerHost _host : hosts) {
            ContainerHostDynamicWorkload host = (ContainerHostDynamicWorkload) _host;
            double previousTime = -1;
            double previousAllocated = 0;
            double previousRequested = 0;
            boolean previousIsActive = true;

            for (HostStateHistoryEntry entry : host.getStateHistory()) {
                if (previousTime != -1 && previousIsActive) {
                    double timeDiff = entry.getTime() - previousTime;
                    totalTime += timeDiff;
                    if (previousAllocated < previousRequested) {
                        slaViolationTimePerHost += timeDiff;
                    }
                }

                previousAllocated = entry.getAllocatedMips();
                previousRequested = entry.getRequestedMips();
                previousTime = entry.getTime();
                previousIsActive = entry.isActive();
            }
        }

        return slaViolationTimePerHost / totalTime;
    }

    /**
     * Gets the sla time per host.
     *
     * @param hosts the hosts
     * @return the sla time per host
     */
    protected static double getSlaTimePerHost(List<ContainerHost> hosts) {
        double slaViolationTimePerHost = 0;
        double totalTime = 0;

        for (ContainerHost _host : hosts) {
            ContainerHostDynamicWorkload host = (ContainerHostDynamicWorkload) _host;
            double previousTime = -1;
            double previousAllocated = 0;
            double previousRequested = 0;

            for (HostStateHistoryEntry entry : host.getStateHistory()) {
                if (previousTime != -1) {
                    double timeDiff = entry.getTime() - previousTime;
                    totalTime += timeDiff;
                    if (previousAllocated < previousRequested) {
                        slaViolationTimePerHost += timeDiff;
                    }
                }

                previousAllocated = entry.getAllocatedMips();
                previousRequested = entry.getRequestedMips();
                previousTime = entry.getTime();
            }
        }

        return slaViolationTimePerHost / totalTime;
    }

    /**
     * Gets the sla metrics.
     *
     * @param vms the vms
     * @return the sla metrics
     */
    protected static Map<String, Double> getSlaMetrics(List<ContainerVm> vms) {
        Map<String, Double> metrics = new HashMap<String, Double>();
        List<Double> slaViolation = new LinkedList<Double>();
        double totalAllocated = 0;
        double totalRequested = 0;
        double totalUnderAllocatedDueToMigration = 0;

        for (ContainerVm vm : vms) {
            double vmTotalAllocated = 0;
            double vmTotalRequested = 0;
            double vmUnderAllocatedDueToMigration = 0;
            double previousTime = -1;
            double previousAllocated = 0;
            double previousRequested = 0;
            boolean previousIsInMigration = false;

            for (VmStateHistoryEntry entry : vm.getStateHistory()) {
                if (previousTime != -1) {
                    double timeDiff = entry.getTime() - previousTime;
                    vmTotalAllocated += previousAllocated * timeDiff;
                    vmTotalRequested += previousRequested * timeDiff;

                    if (previousAllocated < previousRequested) {
                        slaViolation.add((previousRequested - previousAllocated) / previousRequested);
                        if (previousIsInMigration) {
                            vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated)
                                    * timeDiff;
                        }
                    }
                }

                previousAllocated = entry.getAllocatedMips();
                previousRequested = entry.getRequestedMips();
                previousTime = entry.getTime();
                previousIsInMigration = entry.isInMigration();
            }

            totalAllocated += vmTotalAllocated;
            totalRequested += vmTotalRequested;
            totalUnderAllocatedDueToMigration += vmUnderAllocatedDueToMigration;
        }

        metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
        if (slaViolation.isEmpty()) {
            metrics.put("average", 0.);
        } else {
            metrics.put("average", MathUtil.mean(slaViolation));
        }
        metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);
        // metrics.put("sla_time_per_vm_with_migration", slaViolationTimePerVmWithMigration /
        // totalTime);
        // metrics.put("sla_time_per_vm_without_migration", slaViolationTimePerVmWithoutMigration /
        // totalTime);

        return metrics;
    }

    /**
     * Write data column.
     *
     * @param data       the data
     * @param outputPath the output path
     */
    public static void writeDataColumn(List<? extends Number> data, String outputPath) {
        File file = new File(outputPath);
        File parent = file.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        try {
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(0);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Number value : data) {
                writer.write(value.toString() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Write data row.
     *
     * @param data       the data
     * @param outputPath the output path
     */
    public static void writeDataRow(String data, String outputPath) {
        File file = new File(outputPath);
        try {
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(0);
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Write metric history.
     *
     * @param hosts              the hosts
     * @param vmAllocationPolicy the vm allocation policy
     * @param outputPath         the output path
     */
    public static void writeMetricHistory(
            List<? extends ContainerHost> hosts,
            PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy,
            String outputPath) {
        // for (Host host : hosts) {
        for (int j = 0; j < 10; j++) {
            ContainerHost host = hosts.get(j);

            if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
                continue;
            }
            File file = new File(outputPath + "_" + host.getId() + ".csv");
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
                List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
                List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

                for (int i = 0; i < timeData.size(); i++) {
                    writer.write(String.format(
                            "%.2f,%.2f,%.2f\n",
                            timeData.get(i),
                            utilizationData.get(i),
                            metricData.get(i)));
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    /**
     * Prints the metric history.
     *
     * @param hosts              the hosts
     * @param vmAllocationPolicy the vm allocation policy
     */
    public static void printMetricHistory(
            List<? extends ContainerHost> hosts,
            PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) {
        for (int i = 0; i < 10; i++) {
            ContainerHost host = hosts.get(i);

            Log.printLine("Host #" + host.getId());
            Log.printLine("Time:");
            if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
                continue;
            }
            for (Double time : vmAllocationPolicy.getTimeHistory().get(host.getId())) {
                Log.format("%.2f, ", time);
            }
            Log.printLine();

            for (Double utilization : vmAllocationPolicy.getUtilizationHistory().get(host.getId())) {
                Log.format("%.2f, ", utilization);
            }
            Log.printLine();

            for (Double metric : vmAllocationPolicy.getMetricHistory().get(host.getId())) {
                Log.format("%.2f, ", metric);
            }
            Log.printLine();
        }
    }

    public static void printResultsNew(PowerContainerDatacenter datacenter,
                                       ContainerDatacenterBroker broker,
                                       double lastClock,
                                       String experimentName,
                                       boolean outputInCsv,
                                       String outputFolder) throws IOException {
        List<ContainerVm> vms = broker.getVmsCreatedList();
        List<Container>  containers = broker.getContainersCreatedList();
        Log.enable();
        List<ContainerHost> hosts = datacenter.getHostList();
        Map<String, Double> slaMetrics = getSlaMetrics(vms);
        String[] msg = { "ExperimentName","hostSelectionPolicy","vmAllocationPolicy", "OLThreshold","ULThreshold",  "VMSPolicy","ContainerSpolicy","ContainerPlacement","Percentile",
                "numberOfHosts",
                "numberOfVms",
                "totalSimulationTime",
                "slaOverall",
                "slaAverage",
                "slaTimePerActiveHost",
                "meanTimeBeforeHostShutdown",
                "stDevTimeBeforeHostShutdown",
                "medTimeBeforeHostShutdown",
                "meanTimeBeforeContainerMigration",
                "stDevTimeBeforeContainerMigration",
                "medTimeBeforeContainerMigration",
                "meanActiveVm",
                "stDevActiveVm",
                "medActiveVm",
                "meanActiveHosts",
                "stDevActiveHosts",
                "medActiveHosts",
                "meanNumberOfContainerMigrations",
                "stDevNumberOfContainerMigrations",
                "medNumberOfContainerMigrations",
                "meanDatacenterEnergy",
                "stDevDatacenterEnergy",
                "medDatacenterEnergy",
                "totalContainerMigration",
                "totalVmMigration",
                "totalVmCreated",
                "numberOfOverUtilization",
                "energy",
                "CreatedContainers",
                "CreatedVms"


        };

        int numberOfHosts = hosts.size();
        int numberOfVms = vms.size();
        double totalSimulationTime = lastClock;
        double slaOverall = slaMetrics.get("overall");
        double slaAverage = slaMetrics.get("average");
        double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);
        List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);
        int numberOfHostShutdowns = timeBeforeHostShutdown.size();
        double meanTimeBeforeHostShutdown = Double.NaN;
        double stDevTimeBeforeHostShutdown = Double.NaN;
        double medTimeBeforeHostShutdown = Double.NaN;
        if (!timeBeforeHostShutdown.isEmpty()) {
            meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
            stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
            medTimeBeforeHostShutdown = MathUtil.median(timeBeforeHostShutdown);
        }

        List<Double> timeBeforeContainerMigration = getTimesBeforeContainerMigration(containers);
        double meanTimeBeforeContainerMigration = Double.NaN;
        double stDevTimeBeforeContainerMigration = Double.NaN;
        double medTimeBeforeContainerMigration = Double.NaN;
        if (!timeBeforeContainerMigration.isEmpty()) {
            meanTimeBeforeContainerMigration = MathUtil.mean(timeBeforeContainerMigration);
            stDevTimeBeforeContainerMigration = MathUtil.stDev(timeBeforeContainerMigration);
            medTimeBeforeContainerMigration = MathUtil.median(timeBeforeContainerMigration);
        }
        List<Double> activeVm = datacenter.getActiveVmList();
        double meanActiveVm = Double.NaN;
        double stDevActiveVm = Double.NaN;
        double medActiveVm = Double.NaN;
        if (!activeVm.isEmpty()) {
            meanActiveVm = MathUtil.mean(activeVm);
            stDevActiveVm = MathUtil.stDev(activeVm);
            medActiveVm = MathUtil.median(activeVm);
        }
        List<Double> activeHost = datacenter.getActiveHostList();
        double meanActiveHosts = Double.NaN;
        double stDevActiveHosts = Double.NaN;
        double medActiveHosts = Double.NaN;
        if (!activeHost.isEmpty()) {
            meanActiveHosts = MathUtil.mean(activeHost);
            stDevActiveHosts = MathUtil.stDev(activeHost);
            medActiveHosts = MathUtil.median(activeHost);
        }


        List<Double> numberOfContainerMigrations = datacenter.getContainerMigrationList();
        double meanNumberOfContainerMigrations = Double.NaN;
        double stDevNumberOfContainerMigrations = Double.NaN;
        double medNumberOfContainerMigrations = Double.NaN;
        if (!numberOfContainerMigrations.isEmpty()) {
            meanNumberOfContainerMigrations = MathUtil.mean(numberOfContainerMigrations);
            stDevNumberOfContainerMigrations = MathUtil.stDev(numberOfContainerMigrations);
            medNumberOfContainerMigrations = MathUtil.median(numberOfContainerMigrations);
        }
        List<Double> datacenterEnergy = datacenter.getDatacenterEnergyList();
        double meanDatacenterEnergy = Double.NaN;
        double stDevDatacenterEnergy = Double.NaN;
        double medDatacenterEnergy = Double.NaN;
        if (!datacenterEnergy.isEmpty()) {
            meanDatacenterEnergy = MathUtil.mean(datacenterEnergy);
            stDevDatacenterEnergy = MathUtil.stDev(datacenterEnergy);
            medDatacenterEnergy = MathUtil.median(datacenterEnergy);
        }
        int totalContainerMigration =0;
        int totalVmMigration =0;
        int totalVmCreated =0;
        if (datacenter instanceof PowerContainerDatacenterCM) {
            totalContainerMigration = ((PowerContainerDatacenterCM) datacenter).getContainerMigrationCount();
            totalVmMigration = ((PowerContainerDatacenterCM) datacenter).getVmMigrationCount();
            totalVmCreated = ((PowerContainerDatacenterCM) datacenter).getNewlyCreatedVms();


        }
        PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerContainerVmAllocationPolicyMigrationAbstract) datacenter
                .getVmAllocationPolicy();
        int numberOfOverUtilization = getNumberofOverUtilization(hosts, vmAllocationPolicy);

        double energy = datacenter.getPower() / (3600 * 1000);

   //Now we create the log we need
        StringBuilder data = new StringBuilder();
        String delimeter = ",";

        data.append(experimentName + delimeter);
        data.append(parseExperimentName(experimentName));
        data.append(String.format("%d", numberOfHosts) + delimeter);
        data.append(String.format("%d", numberOfVms) + delimeter);
        data.append(String.format("%.2f", totalSimulationTime) + delimeter);
        data.append(String.format("%.10f", slaOverall) + delimeter);
        data.append(String.format("%.10f", slaAverage) + delimeter);
        data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
        data.append(String.format("%.10f", meanTimeBeforeHostShutdown) + delimeter);
        data.append(String.format("%.10f", stDevTimeBeforeHostShutdown) + delimeter);
        data.append(String.format("%.10f", medTimeBeforeHostShutdown) + delimeter);
        data.append(String.format("%.10f", meanTimeBeforeContainerMigration) + delimeter);
        data.append(String.format("%.10f", stDevTimeBeforeContainerMigration) + delimeter);
        data.append(String.format("%.10f", medTimeBeforeContainerMigration) + delimeter);
        data.append(String.format("%.10f", meanActiveVm) + delimeter);
        data.append(String.format("%.10f", stDevActiveVm) + delimeter);
        data.append(String.format("%.10f", medActiveVm) + delimeter);
        data.append(String.format("%.10f", meanActiveHosts) + delimeter);
        data.append(String.format("%.10f", stDevActiveHosts) + delimeter);
        data.append(String.format("%.10f", medActiveHosts) + delimeter);
        data.append(String.format("%.10f", meanNumberOfContainerMigrations) + delimeter);
        data.append(String.format("%.10f", stDevNumberOfContainerMigrations) + delimeter);
        data.append(String.format("%.10f", medNumberOfContainerMigrations) + delimeter);
        data.append(String.format("%.10f", meanDatacenterEnergy) + delimeter);
        data.append(String.format("%.10f", stDevDatacenterEnergy) + delimeter);
        data.append(String.format("%.10f", medDatacenterEnergy) + delimeter);
        data.append(String.format("%d", totalContainerMigration) + delimeter);
        data.append(String.format("%d", totalVmMigration) + delimeter);
        data.append(String.format("%d", totalVmCreated) + delimeter);
        data.append(String.format("%d", numberOfOverUtilization) + delimeter);
        data.append(String.format("%.5f", energy) + delimeter);
        data.append(String.format("%d", broker.getContainersCreated()) + delimeter);
        data.append(String.format("%d", broker.getNumberOfCreatedVMs()) + delimeter);

//        data.append(String.format("%.10f", sla) + delimeter);
//        data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);

        int index = experimentName.lastIndexOf("_");

        File folder1 = new File(outputFolder + "/stats/");
        File parent1 = folder1.getParentFile();
        if(!parent1.exists() && !parent1.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent1);
        }

        if (!folder1.exists()) {
            folder1.mkdir();
        }
        String beforShutDown = outputFolder + "/time_before_host_shutdown/"+experimentName.substring(0,index);
        File folder2 = new File(beforShutDown);
        File parent2 = folder2.getParentFile();
        if(!parent2.exists() && !parent2.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent2);
        }


        if (!folder2.exists()) {
            folder2.mkdir();
        }


        String beforeMigrate = outputFolder + "/time_before_vm_migration/"+experimentName.substring(0,index);
        File folder3 = new File(beforeMigrate);

        File parent3 = folder3.getParentFile();
        if(!parent3.exists() && !parent3.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent3);
        }

        if (!folder3.exists()) {
            folder3.mkdir();
        }



//        int index = experimentName.lastIndexOf("_");

        String fileAddress = String.format("%s/stats/%s_stats.csv", outputFolder, experimentName.substring(0,index));


        File f = new File(fileAddress);
        CSVWriter writer = new CSVWriter(new FileWriter(fileAddress, true), ',',CSVWriter.NO_QUOTE_CHARACTER);
        File parent = f.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }

        if(!f.exists()) {
            f.createNewFile();
//            writer.writeNext("\n")
        }
        int temp = index;
        if(experimentName.substring(index).startsWith("_1") && experimentName.length()-2 == temp){
//            CSVWriter writer1 = new CSVWriter(new FileWriter(fileAddress, true), ',',CSVWriter.NO_QUOTE_CHARACTER);
            writer.writeNext(msg);
        }
        writer.writeNext(new String[]{data.toString()});
        writer.flush();
        writer.close();




        writeDataColumn(timeBeforeHostShutdown, beforShutDown+"/"+ experimentName + "_time_before_host_shutdown.csv");
        writeDataColumn(timeBeforeContainerMigration, beforeMigrate+"/"+experimentName+ "_time_before_vm_migration.csv");




    }


    public static int getNumberofOverUtilization(List<? extends ContainerHost> hosts,
                                                 PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) {
        int numberOfOverUtilization = 0;
        for (int j = 0; j < hosts.size(); j++) {
            ContainerHost host = hosts.get(j);

            if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
                continue;
            }


            List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
            List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
            List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

            for (int i = 0; i < timeData.size(); i++) {
                if (utilizationData.get(i) > metricData.get(i)) {
                    numberOfOverUtilization++;
                }
            }
        }
        return numberOfOverUtilization;

    }



}
