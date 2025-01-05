package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstractHostSelection;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationStaticThresholdMC;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationStaticThresholdMCUnderUtilized;
import org.cloudbus.cloudsim.VmAllocationWithSelectionPolicy;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.selectionPolicies.PowerSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.selectionPolicies.PowerSelectionPolicyMaximumCorrelation2;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.selectionPolicies.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * The RunnerAbs Class is the modified version of {@link org.cloudbus.cloudsim.examples.power.RunnerAbstract}
 * Created by sareh on 18/08/15.
 * Modified by Remo Andreoli (Feb 2024)
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
    protected static List<HostEntity> hostList;

    /**
     * The Cloudlet List
     */
    protected static List<Cloudlet> cloudletList;
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

            File file = new File(outputFolder + "/log/" + getExperimentName().substring(0, index) + "/" + this.getExperimentName(hostSelectionPolicy, vmAllocationPolicy, vmSelectionPolicy, containerSelectionPolicy, String.valueOf(getOverBookingFactor()), getRunTime()) + ".txt");
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

    protected void start(String experimentName, String outputFolder, VmAllocationPolicy vmAllocationPolicy, VmAllocationPolicy containerAllocationPolicy) {
        System.out.println("Starting " + experimentName);

        try {
            PowerContainerDatacenter e = (PowerContainerDatacenter) HelperEx.createDatacenter("datacenter",
                    PowerContainerDatacenterCM.class, hostList, vmAllocationPolicy, containerAllocationPolicy,
                    getExperimentName(), ConstantsExamples.SCHEDULING_INTERVAL, getLogAddress(),
                    ConstantsExamples.VM_STARTTUP_DELAY, ConstantsExamples.CONTAINER_STARTTUP_DELAY);
//            PowerContainerDatacenter e = (PowerContainerDatacenter) HelperEx.createDatacenter("Datacenter", PowerContainerDatacenter.class, hostList, vmAllocationPolicy, containerAllocationPolicy);
            vmAllocationPolicy.setDatacenter(e);

            e.setDisableVmMigrations(false);
            broker.submitGuestList(vmList);
            broker.submitContainerList(containerList);
            broker.submitCloudletList(cloudletList.subList(0, containerList.size()));
            CloudSim.terminateSimulation(86400.0D);
            double lastClock = CloudSim.startSimulation();
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.println("Received " + newList.size() + " cloudlets");
            CloudSim.stopSimulation();

//            HelperEx.printResults(e, broker.getVmsCreatedList(),broker.getContainersCreatedList() ,lastClock, experimentName, true, outputFolder);
            HelperEx.printResultsNew(e, broker, lastClock, experimentName, true, outputFolder);
        } catch (Exception var8) {
            var8.printStackTrace();
            Log.println("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.println("Finished " + experimentName);
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


    protected VmAllocationPolicy getVmAllocationPolicy(String vmAllocationPolicyName, String vmSelectionPolicyName, String containerSelectionPolicyName, String hostSelectionPolicyName) {
        VmAllocationPolicy vmAllocationPolicy = null;
        SelectionPolicy<GuestEntity> vmSelectionPolicy = null;
        SelectionPolicy<PowerGuestEntity> containerSelectionPolicy = null;
        SelectionPolicy<HostEntity> hostSelectionPolicy = null;
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

        return vmAllocationPolicy;
    }

    protected VmAllocationPolicy getContainerAllocationPolicy(String containerAllocationPolicyName) {
        VmAllocationPolicy containerAllocationPolicy;
        if (Objects.equals(containerAllocationPolicyName, "Simple")) {
            containerAllocationPolicy = new VmAllocationPolicySimple(vmList); // DVFS policy without VM migrations
        } else {
            SelectionPolicy<HostEntity> selectionPolicy = getContainerPlacementPolicy(containerAllocationPolicyName);
            containerAllocationPolicy = new VmAllocationWithSelectionPolicy(vmList, selectionPolicy); // DVFS policy without VM migrations
        }

        return containerAllocationPolicy;

    }

    protected SelectionPolicy<HostEntity> getContainerPlacementPolicy(String name) {
        SelectionPolicy<HostEntity> selectionPolicy;
        switch (name) {
            case "LeastFull":
                selectionPolicy = new SelectionPolicyLeastFull<>();
                break;
            case "MostFull":
                selectionPolicy = new SelectionPolicyMostFull<>();
                break;

            case "FirstFit":
                selectionPolicy = new SelectionPolicyFirstFit<>();
                break;
            case "Random":
                selectionPolicy = new SelectionPolicyRandomSelection<>();
                break;
            default:
                selectionPolicy = null;
                System.out.println("The container placement policy is not defined");
                break;
        }
        return selectionPolicy;
    }

    protected SelectionPolicy<HostEntity> getHostSelectionPolicy(String hostSelectionPolicyName) {
        SelectionPolicy<HostEntity> hostSelectionPolicy = null;
        if (Objects.equals(hostSelectionPolicyName, "FirstFit")) {
            hostSelectionPolicy = new SelectionPolicyFirstFit<>();
        } else if (Objects.equals(hostSelectionPolicyName, "LeastFull")) {
            hostSelectionPolicy = new SelectionPolicyLeastFull<>();
        } else if (Objects.equals(hostSelectionPolicyName, "MostFull")) {
            hostSelectionPolicy = new SelectionPolicyMostFull<>();
        }
//        else if (hostSelectionPolicyName == "MinCor") {
    //            hostSelectionPolicy = new PowerSelectionPolicyMinimumCorrelation();
//        }
    else if (Objects.equals(hostSelectionPolicyName, "RandomSelection")) {
            hostSelectionPolicy = new SelectionPolicyRandomSelection<>();
        }
// else if(vmSelectionPolicyName.equals("mmt")) {
//            vmSelectionPolicy = new SelectionPolicyMinimumMigrationTime();
//        } else if(vmSelectionPolicyName.equals("mu")) {
//            vmSelectionPolicy = new SelectionPolicyMinimumUtilization();
//        } else if(vmSelectionPolicyName.equals("rs")) {
//            vmSelectionPolicy = new SelectionPolicyRandomSelection<>();
//        }
        else {
            System.out.println("Unknown Host selection policy: " + hostSelectionPolicyName);
            System.exit(0);
        }
        return hostSelectionPolicy;
    }

    
    protected SelectionPolicy<PowerGuestEntity> getContainerSelectionPolicy(String containerSelectionPolicyName) {
        SelectionPolicy<PowerGuestEntity> containerSelectionPolicy = null;
        if (containerSelectionPolicyName.equals("Cor")) {
            containerSelectionPolicy = new PowerSelectionPolicyMaximumCorrelation2(new SelectionPolicyMaximumUsage<>());
        } else if (containerSelectionPolicyName.equals("MaxUsage")) {
            containerSelectionPolicy = new SelectionPolicyMaximumUsage<>();
        }
// else if(vmSelectionPolicyName.equals("mmt")) {
//            vmSelectionPolicy = new SelectionPolicyMinimumMigrationTime();
//        } else if(vmSelectionPolicyName.equals("mu")) {
//            vmSelectionPolicy = new SelectionPolicyMinimumUtilization();
//        } else if(vmSelectionPolicyName.equals("rs")) {
//            vmSelectionPolicy = new SelectionPolicyRandomSelection<>();
//        }
        else {
            System.out.println("Unknown Container selection policy: " + containerSelectionPolicyName);
            System.exit(0);
        }

        return containerSelectionPolicy;
    }

    // @TODO: is this use of generics correct?
    protected <T extends GuestEntity> SelectionPolicy<T> getVmSelectionPolicy(String vmSelectionPolicyName) {
        SelectionPolicy<T> vmSelectionPolicy = null;
        if (vmSelectionPolicyName.equals("VmMaxC")) {
            vmSelectionPolicy = (SelectionPolicy<T>) new PowerSelectionPolicyMaximumCorrelation(new SelectionPolicyMaximumUsage());
        } else if (vmSelectionPolicyName.equals("VmMaxU")) {
            vmSelectionPolicy = new SelectionPolicyMaximumUsage<>();
        }
// else if(vmSelectionPolicyName.equals("mmt")) {
//            vmSelectionPolicy = new SelectionPolicyMinimumMigrationTime();
//        } else if(vmSelectionPolicyName.equals("mu")) {
//            vmSelectionPolicy = new SelectionPolicyMinimumUtilization();
//        } else if(vmSelectionPolicyName.equals("rs")) {
//            vmSelectionPolicy = new SelectionPolicyRandomSelection<>();
//        }
        else {
            System.out.println("Unknown VM selection policy: " + vmSelectionPolicyName);
            System.exit(0);
        }

        return vmSelectionPolicy;
    }

    public void setEnableOutput(boolean enableOutput) {
        RunnerAbs.enableOutput = enableOutput;
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
