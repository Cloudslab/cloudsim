package org.cloudbus.cloudsim.examples.container;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.containerProvisioners.*;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisionerSimple;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.auction.bidder.BidderDatacenterBroker;
import org.cloudbus.cloudsim.auction.bidder.BidderContainerVM;

import java.io.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This is the modified version of {@link org.cloudbus.cloudsim.examples.power.Helper} class in cloudsim.
 * Created by sareh on 15/07/15.
 */

public class HelperEx {

    public HelperEx() {
//        System.out.print();

    }

    /**
     * Creating the cloudlet list that are going to run on containers
     *
     * @param brokerId
     * @param numberOfCloudlets
     * @param inputFolderName
     * @return
     * @throws FileNotFoundException
     */
    public static List<ContainerCloudlet> createContainerCloudletList(int brokerId, String inputFolderName, int numberOfCloudlets)
            throws FileNotFoundException {
        ArrayList cloudletList = new ArrayList();
        long fileSize = 300L;
        long outputSize = 300L;
        UtilizationModelNull utilizationModelNull = new UtilizationModelNull();
        File inputFolder1 = new File(inputFolderName);
        File[] directories = inputFolder1.listFiles();
        int createdCloudlets = 0;
        for (File directory : directories) {
            if (!directory.isDirectory()) // Make sure to check it's a directory.
                continue;
            File inputFolder = new File(directory.toString());
            File[] files = inputFolder.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (createdCloudlets < numberOfCloudlets) {
                    ContainerCloudlet cloudlet = null;

                    try {
                        cloudlet = new ContainerCloudlet(IDs.pollId(ContainerCloudlet.class), ConstantsExamples.CLOUDLET_LENGTH, 1,
                                fileSize, outputSize,
                                new UtilizationModelPlanetLabInMemoryExtended(files[i].getAbsolutePath(), 300.0D),
                                utilizationModelNull, utilizationModelNull);

                        // QUESTION: Is this cloudlenght so high because of overbooking????

//                        cloudlet = new ContainerCloudlet(IDs.pollId(ContainerCloudlet.class), 216000000L * 1000, 1,
//                                fileSize, outputSize,
//                                new UtilizationModelPlanetLabInMemoryExtended(files[i].getAbsolutePath(), 300.0D),
//                                utilizationModelNull, utilizationModelNull);

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
    public static List<ContainerVm> createVmList(int brokerId, int containerVmsNumber, String containerType) {
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
            ContainerVm cVM = null;
            switch (containerType) {
                case "PowerContainerVm" :
                    cVM = new PowerContainerVm(IDs.pollId(ContainerVm.class),
                            brokerId, (double) ConstantsExamples.VM_MIPS[vmType], (float) ConstantsExamples.VM_RAM[vmType],
                            ConstantsExamples.VM_BW, ConstantsExamples.VM_SIZE, "Xen",
                            new ContainerSchedulerTimeSharedOverSubscription(peList),
                            new ContainerRamProvisionerSimple(ConstantsExamples.VM_RAM[vmType]),
                            new ContainerBwProvisionerSimple(ConstantsExamples.VM_BW), peList,
                            ConstantsExamples.SCHEDULING_INTERVAL);

                    break;
                case "BidderContainerVM" :
                    cVM = new BidderContainerVM(IDs.pollId(ContainerVm.class),
                            brokerId, (double) ConstantsExamples.VM_MIPS[vmType], (float) ConstantsExamples.VM_RAM[vmType],
                            ConstantsExamples.VM_BW, ConstantsExamples.VM_SIZE, "Xen",
                            new ContainerSchedulerTimeSharedOverSubscription(peList),
                            new ContainerRamProvisionerSimple(ConstantsExamples.VM_RAM[vmType]),
                            new ContainerBwProvisionerSimple(ConstantsExamples.VM_BW), peList,
                            ConstantsExamples.SCHEDULING_INTERVAL);


                    break;
            }
            containerVms.add(cVM);


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

    public static BidderDatacenterBroker createAuctionBroker(double overBookingFactor) {
        BidderDatacenterBroker broker = null;

        try {
            broker = new BidderDatacenterBroker("Broker");
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

    public static void printResults(PowerContainerDatacenter datacenter,
                                       ContainerDatacenterBroker broker,
                                       double lastClock,
                                       String experimentName,
                                       boolean outputInCsv,
                                       String outputFolder,
                                    int numberOfRequestedContainers) throws IOException {
        List<ContainerVm> vms = broker.getVmsCreatedList();
        boolean writeHeader = false;
        List<Container>  createdContainers = broker.getContainersCreatedList();
        List<ContainerCloudlet> cloudlets = broker.getCloudletReceivedList();

        Log.enable();
        List<ContainerHost> hosts = datacenter.getHostList();
        Map<String, Double> slaMetrics = getSlaMetrics(vms);
        String[] msg = { "ExperimentName","hostSelectionPolicy","vmAllocationPolicy", "OLThreshold","ULThreshold",  "VMSPolicy","ContainerSpolicy","ContainerPlacement","Percentile",
                "numberOfHosts",
                "numberOfVms",
                "numberOfContainers",
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
                "CreatedVms",
                "meanTimeCloudletFinishTime",
                "stDevTimeCloudletFinishTime",
                "medTimeCloudletFinishTime",
        };

        int numberOfHosts = hosts.size();
        int numberOfCreatedVms = vms.size();
        int numberOfRequestedVms = broker.getVmList().size();
        int numberOfCreatedContainers = createdContainers.size();
//        int numberOfRequestedContainers = broker.getContainerList().size();

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

        List<Double> timeBeforeContainerMigration = getTimesBeforeContainerMigration(createdContainers);
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
        int totalContainersCreated=0;

        if (datacenter instanceof PowerContainerDatacenterCM) {
            totalContainerMigration = ((PowerContainerDatacenterCM) datacenter).getContainerMigrationCount();
            totalVmMigration = ((PowerContainerDatacenterCM) datacenter).getVmMigrationCount();
            totalVmCreated = ((PowerContainerDatacenterCM) datacenter).getNewlyCreatedVms();

            totalVmCreated = datacenter.getNumberOfVms();
            totalContainersCreated = datacenter.getNumberOfContainers();

//            data.append(String.format("%d", broker.getContainersCreated()) + delimeter);
//            data.append(String.format("%d", broker.getNumberOfCreatedVMs()) + delimeter);

        }

        PowerContainerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerContainerVmAllocationPolicyMigrationAbstract) datacenter
                .getVmAllocationPolicy();
        int numberOfOverUtilization = getNumberofOverUtilization(hosts, vmAllocationPolicy);

        double energy = datacenter.getPower() / (3600 * 1000);

        // Calculate the Successfull returned finishtimes for cloudlets
        List<Double> cloudletFinishTimes = getCloudletFinishTimes(cloudlets);
        double meanTimeCloudletFinishTime = Double.NaN;
        double stDevCloudletFinishTime = Double.NaN;
        double medCloudletFinishTime = Double.NaN;
        if (!cloudletFinishTimes.isEmpty()) {
            meanTimeCloudletFinishTime = MathUtil.mean(cloudletFinishTimes);
            stDevCloudletFinishTime = MathUtil.stDev(cloudletFinishTimes);
            medCloudletFinishTime = MathUtil.median(cloudletFinishTimes);
        }


        // Now we create the log we need
        StringBuilder data = new StringBuilder();
        String delimeter = ",";

        data.append(experimentName + delimeter);
        data.append(parseExperimentName(experimentName));
        data.append(String.format("%d", numberOfHosts) + delimeter);
        data.append(String.format("%d", numberOfRequestedVms) + delimeter);
        data.append(String.format("%d", numberOfRequestedContainers) + delimeter);
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
        data.append(String.format("%.10f", meanTimeCloudletFinishTime) + delimeter);
        data.append(String.format("%.10f", stDevCloudletFinishTime) + delimeter);
        data.append(String.format("%.10f", medCloudletFinishTime) + delimeter);

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
        String beforeShutDown = outputFolder + "/time_before_host_shutdown/"+experimentName.substring(0,index);
        File folder2 = new File(beforeShutDown);
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
        if(!f.exists()) {
            f.createNewFile();
            writeHeader = true;
        }
        CSVWriter writer = new CSVWriter(new FileWriter(fileAddress, true), ',',CSVWriter.NO_QUOTE_CHARACTER);
        File parent = f.getParentFile();
        if(!parent.exists() && !parent.mkdirs()){
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }

        int temp = index;
        // It's assumed that runTime number at the end indicates the need to write the header file
        // instead if the file did not exist that should indicate that the file already exist and add the header
        // NOTE: This can result into having multiple entries with the same experiment name, however, at least the
        // header does not get duplicated.
//        if(experimentName.substring(index).startsWith("_1") && experimentName.length()-2 == temp)
        if (writeHeader) {
            writer.writeNext(msg);
        }


        String[] entries = data.toString().split(",");
//        writer.writeNext(new String[]{data.toString()});
        writer.writeNext(entries);
        writer.flush();
        writer.close();

        writeDataColumn(timeBeforeHostShutdown, beforeShutDown+"/"+ experimentName + "_time_before_host_shutdown.csv");
        writeDataColumn(timeBeforeContainerMigration, beforeMigrate+"/"+experimentName+ "_time_before_vm_migration.csv");

        printCloudletList(cloudlets);


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

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    public static void printCloudletList(List<ContainerCloudlet> list) {
        int size = list.size();

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");

        list.forEach(cloudlet->{
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatusString() == "Success") {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        });

    }


    public static List<Double> getCloudletFinishTimes(List<ContainerCloudlet> list) {
        int size = list.size();
        List<Double> result = new ArrayList<>();;

        // Iterate through each and build a list of successfull finish times.
        list.forEach(cloudlet->{
            if (cloudlet.getCloudletStatusString() == "Success")
                result.add(cloudlet.getFinishTime());
        });

        return result;
    }

}
