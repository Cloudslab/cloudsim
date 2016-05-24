package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.containerPlacementPolicies.*;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicyCor;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.*;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstractHostSelection;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationStaticThresholdMC;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationStaticThresholdMCUnderUtilized;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicyRS;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.PowerContainerAllocationPolicySimple;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * The RunnerAbs Class is the modified version of {@link org.cloudbus.cloudsim.examples.power.RunnerAbstract}
 * Created by sareh on 18/08/15.
 */
public abstract class RunnerAbs {
    private static boolean enableOutput;

    protected static ContainerDatacenterBroker broker;
    /**
     * The vm list.
     */
    protected static List<ContainerVm> vmList;
    /**
     * The container list.
     */
    protected static List<Container> containerList;

    /**
     * The host list.
     */
    protected static List<ContainerHost> hostList;

    /**
     * The Cloudlet List
     */
    protected static List<ContainerCloudlet> cloudletList;
    /**
     * The overBooking Factor for containers
     */
    private double overBookingFactor;


    private String experimentName;
    private String logAddress;


    private String runTime;


    public RunnerAbs(boolean enableOutput, boolean outputToFile, String inputFolder, String outputFolder, String vmAllocationPolicy, String containerAllocationPolicy, String vmSelectionPolicy, String containerSelectionPolicy, String hostSelectionPolicy, double overBookingFactor, String runTime, String logAddress) {
        setOverBookingFactor(overBookingFactor);
        setRunTime(runTime);
        setLogAddress(logAddress);
        setExperimentName(this.getExperimentName(hostSelectionPolicy, vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, containerAllocationPolicy, String.valueOf(getOverBookingFactor()), runTime));

        try {
            this.initLogOutput(enableOutput, outputToFile, outputFolder, vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy);
        } catch (Exception var10) {
            var10.printStackTrace();
            System.exit(0);
        }

        this.init(inputFolder + "/", getOverBookingFactor());
        this.start(getExperimentName(), outputFolder, this.getVmAllocationPolicy(vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy), getContainerAllocationPolicy(containerAllocationPolicy));
    }

    public String getLogAddress() {
        return logAddress;
    }

    public void setLogAddress(String logAddress) {
        this.logAddress = logAddress;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    protected void initLogOutput(boolean enableOutput, boolean outputToFile, String outputFolder, String vmAllocationPolicy, String vmSelectionPolicy, String containerSelectionPolicy, String hostSelectionPolicy) throws IOException, FileNotFoundException {
        this.setEnableOutput(enableOutput);
        Log.setDisabled(!this.isEnableOutput());
//        OutputStream out = new FileOutputStream("/home/sareh/Dropbox/programming/Results/log.txt");
        //OutputStream out = new BufferedOutputStream(new FileOutputStream("/home/sareh/Dropbox/programming/Results/log1.txt"), 100000);
//        Log.setOutput(out);
        if (this.isEnableOutput() && outputToFile) {
            int index = getExperimentName().lastIndexOf("_");

//            File folder1 = new File(outputFolder + "/stats/"+getExperimentName().substring(0, index));
            File folder = new File(outputFolder);
            File parent = folder.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            if (!folder.exists()) {
                folder.mkdir();
            }
            File folder2 = new File(outputFolder + "/log/" + getExperimentName().substring(0, index));
            File parent2 = folder2.getParentFile();
            if (!parent2.exists() && !parent2.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent2);
            }
            if (!folder2.exists()) {
                folder2.mkdir();
            }
            File folder3 = new File(outputFolder + "/ContainerMigration/" + getExperimentName().substring(0, index));
            File parent3 = folder3.getParentFile();
            if (!parent3.exists() && !parent3.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent3);
            }
            if (!folder3.exists()) {
                folder3.mkdir();
            }

            File folder4 = new File(outputFolder + "/NewlyCreatedVms/" + getExperimentName().substring(0, index));
            File parent4 = folder4.getParentFile();
            if (!parent4.exists() && !parent4.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent4);
            }
            if (!folder4.exists()) {
                folder4.mkdir();
            }
            File folder5 = new File(outputFolder + "/EnergyConsumption/" + getExperimentName().substring(0, index));
            File parent5 = folder5.getParentFile();
            if (!parent5.exists() && !parent5.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent5);
            }

            if (!folder5.exists()) {
                folder5.mkdir();
            }

            File file = new File(outputFolder + "/log/" + getExperimentName().substring(0, index) + "/" + this.getExperimentName(new String[]{hostSelectionPolicy, vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, String.valueOf(getOverBookingFactor()), getRunTime()}) + ".txt");
            file.createNewFile();
            Log.setOutput(new FileOutputStream(file));
        }

    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    protected abstract void init(String var1, double overBookingFactor);

    protected void start(String experimentName, String outputFolder, ContainerVmAllocationPolicy vmAllocationPolicy, ContainerAllocationPolicy containerAllocationPolicy) {
        System.out.println("Starting " + experimentName);

        try {
            PowerContainerDatacenter e = (PowerContainerDatacenter) HelperEx.createDatacenter("datacenter",
                    PowerContainerDatacenterCM.class, hostList, vmAllocationPolicy, containerAllocationPolicy,
                    getExperimentName(), ConstantsExamples.SCHEDULING_INTERVAL, getLogAddress(),
                    ConstantsExamples.VM_STARTTUP_DELAY, ConstantsExamples.CONTAINER_STARTTUP_DELAY);
//            PowerContainerDatacenter e = (PowerContainerDatacenter) HelperEx.createDatacenter("Datacenter", PowerContainerDatacenter.class, hostList, vmAllocationPolicy, containerAllocationPolicy);
            vmAllocationPolicy.setDatacenter(e);
            e.setDisableVmMigrations(false);
            broker.submitVmList(vmList);
            broker.submitContainerList(containerList);
            broker.submitCloudletList(cloudletList.subList(0, containerList.size()));
            ;
            CloudSim.terminateSimulation(86400.0D);
            double lastClock = CloudSim.startSimulation();
            List newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");
            CloudSim.stopSimulation();

//            HelperEx.printResults(e, broker.getVmsCreatedList(),broker.getContainersCreatedList() ,lastClock, experimentName, true, outputFolder);
            HelperEx.printResultsNew(e, broker, lastClock, experimentName, true, outputFolder);
        } catch (Exception var8) {
            var8.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + experimentName);
    }

    protected String getExperimentName(String... args) {
        StringBuilder experimentName = new StringBuilder();

        for (int i = 0; i < args.length; ++i) {
            if (!args[i].isEmpty()) {
                if (i != 0) {
                    experimentName.append("_");
                }

                experimentName.append(args[i]);
            }
        }

        return experimentName.toString();
    }


    protected ContainerVmAllocationPolicy getVmAllocationPolicy(String vmAllocationPolicyName, String vmSelectionPolicyName, String containerSelectionPolicyName, String hostSelectionPolicyName) {
        Object vmAllocationPolicy = null;
        PowerContainerVmSelectionPolicy vmSelectionPolicy = null;
        PowerContainerSelectionPolicy containerSelectionPolicy = null;
        HostSelectionPolicy hostSelectionPolicy = null;
        if (!vmSelectionPolicyName.isEmpty() && !containerSelectionPolicyName.isEmpty() && !hostSelectionPolicyName.isEmpty()) {
            vmSelectionPolicy = this.getVmSelectionPolicy(vmSelectionPolicyName);
            containerSelectionPolicy = this.getContainerSelectionPolicy(containerSelectionPolicyName);
            hostSelectionPolicy = this.getHostSelectionPolicy(hostSelectionPolicyName);
        }


        if (vmAllocationPolicyName.startsWith("MSThreshold-Over_")) {
            double overUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(18));
            vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationStaticThresholdMC(hostList, vmSelectionPolicy,
                    containerSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold,
                    ConstantsExamples.VM_TYPES,ConstantsExamples.VM_PES, ConstantsExamples.VM_RAM, ConstantsExamples.VM_BW,
                    ConstantsExamples.VM_SIZE, ConstantsExamples.VM_MIPS);
        } else if (vmAllocationPolicyName.startsWith("MSThreshold-Under_")) {
            double overUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(18, 22));
            double underUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(24));
            vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationStaticThresholdMCUnderUtilized(hostList,
                    vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold,
                    underUtilizationThreshold,ConstantsExamples.VM_TYPES,ConstantsExamples.VM_PES, ConstantsExamples.VM_RAM, ConstantsExamples.VM_BW,
                    ConstantsExamples.VM_SIZE, ConstantsExamples.VM_MIPS );

        } else if (vmAllocationPolicyName.startsWith("VMThreshold-Under_")) {

            double overUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(18, 22));
            double underUtilizationThreshold = Double.parseDouble(vmAllocationPolicyName.substring(24));
            vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(hostList, vmSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold, underUtilizationThreshold);


        } else {
            System.out.println("Unknown VM allocation policy: " + vmAllocationPolicyName);
            System.exit(0);
        }

        return (ContainerVmAllocationPolicy) vmAllocationPolicy;
    }

    protected ContainerAllocationPolicy getContainerAllocationPolicy(String containerAllocationPolicyName) {
        ContainerAllocationPolicy containerAllocationPolicy;
        if (containerAllocationPolicyName == "Simple") {

            containerAllocationPolicy = new PowerContainerAllocationPolicySimple(); // DVFS policy without VM migrations
        } else {

            ContainerPlacementPolicy placementPolicy = getContainerPlacementPolicy(containerAllocationPolicyName);
            containerAllocationPolicy = new ContainerAllocationPolicyRS(placementPolicy); // DVFS policy without VM migrations
        }

        return containerAllocationPolicy;

    }

    protected ContainerPlacementPolicy getContainerPlacementPolicy(String name) {
        ContainerPlacementPolicy placementPolicy;
        switch (name) {
            case "LeastFull":
                placementPolicy = new ContainerPlacementPolicyLeastFull();
                break;
            case "MostFull":
                placementPolicy = new ContainerPlacementPolicyMostFull();
                break;

            case "FirstFit":
                placementPolicy = new ContainerPlacementPolicyFirstFit();
                break;
            case "Random":
                placementPolicy = new ContainerPlacementPolicyRandomSelection();
                break;
            default:
                placementPolicy = null;
                System.out.println("The container placement policy is not defined");
                break;
        }
        return placementPolicy;
    }

    protected HostSelectionPolicy getHostSelectionPolicy(String hostSelectionPolicyName) {
        Object hostSelectionPolicy = null;
        if (hostSelectionPolicyName == "FirstFit") {

            hostSelectionPolicy = new HostSelectionPolicyFirstFit();


        } else if (hostSelectionPolicyName == "LeastFull") {

            hostSelectionPolicy = new HostSelectionPolicyLeastFull();


        } else if (hostSelectionPolicyName == "MostFull") {

            hostSelectionPolicy = new HostSelectionPolicyMostFull();


        }
//        else if (hostSelectionPolicyName == "MinCor") {

//            hostSelectionPolicy = new HostSelectionPolicyMinimumCorrelation();


//        }
    else if (hostSelectionPolicyName == "RandomSelection") {

            hostSelectionPolicy = new HostSelectionPolicyRandomSelection();


        }
// else if(vmSelectionPolicyName.equals("mmt")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
//        } else if(vmSelectionPolicyName.equals("mu")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
//        } else if(vmSelectionPolicyName.equals("rs")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyRandomSelection();
//        }
        else {
            System.out.println("Unknown Host selection policy: " + hostSelectionPolicyName);
            System.exit(0);
        }

        return (HostSelectionPolicy) hostSelectionPolicy;
    }


    protected PowerContainerSelectionPolicy getContainerSelectionPolicy(String containerSelectionPolicyName) {
        Object containerSelectionPolicy = null;
        if (containerSelectionPolicyName.equals("Cor")) {
            containerSelectionPolicy = new PowerContainerSelectionPolicyCor(new PowerContainerSelectionPolicyMaximumUsage());
        } else if (containerSelectionPolicyName.equals("MaxUsage")) {
            containerSelectionPolicy = new PowerContainerSelectionPolicyMaximumUsage();


        }
// else if(vmSelectionPolicyName.equals("mmt")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
//        } else if(vmSelectionPolicyName.equals("mu")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
//        } else if(vmSelectionPolicyName.equals("rs")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyRandomSelection();
//        }
        else {
            System.out.println("Unknown Container selection policy: " + containerSelectionPolicyName);
            System.exit(0);
        }

        return (PowerContainerSelectionPolicy) containerSelectionPolicy;
    }


    protected PowerContainerVmSelectionPolicy getVmSelectionPolicy(String vmSelectionPolicyName) {
        Object vmSelectionPolicy = null;
        if (vmSelectionPolicyName.equals("VmMaxC")) {
            vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumCorrelation(new PowerContainerVmSelectionPolicyMaximumUsage());
        } else if (vmSelectionPolicyName.equals("VmMaxU")) {
            vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();
        }
// else if(vmSelectionPolicyName.equals("mmt")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
//        } else if(vmSelectionPolicyName.equals("mu")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
//        } else if(vmSelectionPolicyName.equals("rs")) {
//            vmSelectionPolicy = new PowerVmSelectionPolicyRandomSelection();
//        }
        else {
            System.out.println("Unknown VM selection policy: " + vmSelectionPolicyName);
            System.exit(0);
        }

        return (PowerContainerVmSelectionPolicy) vmSelectionPolicy;
    }

    public void setEnableOutput(boolean enableOutput) {
        this.enableOutput = enableOutput;
    }

    public boolean isEnableOutput() {
        return enableOutput;
    }

    public double getOverBookingFactor() {
        return overBookingFactor;
    }

    public void setOverBookingFactor(double overBookingFactor) {
        this.overBookingFactor = overBookingFactor;
    }


}
