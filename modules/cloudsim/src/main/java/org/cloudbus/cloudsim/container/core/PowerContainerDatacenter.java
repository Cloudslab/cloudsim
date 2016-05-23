package org.cloudbus.cloudsim.container.core;

//import cloudSimGr.containerCloudSim.Experiments.HelperEx;
//import cloudSimGr.containerCloudSim.Experiments.Paper1.RunnerAbs;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.utils.CostumeCSVWriter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 20/07/15.
 */
public class PowerContainerDatacenter extends ContainerDatacenter {


    /**
     * The power.
     */
    private double power;

    /**
     * The disable migrations.
     */
    private boolean disableVmMigrations;

    /**
     * The cloudlet submited.
     */
    private double cloudletSubmitted;

    /**
     * The migration count.
     */
    private int vmMigrationCount;

    private List<Double> activeVmList;
    private int numberOfVms;

    private int numberOfContainers;

    private List<Double> activeHostList;
    private List<Double> datacenterEnergyList;
    private List<Double> containerMigrationList;



    private CostumeCSVWriter vmMigrationWriter;
    private CostumeCSVWriter containerMigrationWriter;
    private CostumeCSVWriter datacenterEnergyWriter;

    /**
     * Instantiates a new datacenter.
     *
     * @param name               the name
     * @param characteristics    the res config
     * @param schedulingInterval the scheduling interval
     *                           //         * @param utilizationBound the utilization bound
     * @param vmAllocationPolicy the vm provisioner
     * @param storageList        the storage list
     * @throws Exception the exception
     */
    public PowerContainerDatacenter(
            String name,
            ContainerDatacenterCharacteristics characteristics,
            ContainerVmAllocationPolicy vmAllocationPolicy,
            ContainerAllocationPolicy containerAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval, String experimentName, String logAddress) throws Exception {
        super(name, characteristics, vmAllocationPolicy, containerAllocationPolicy, storageList, schedulingInterval, experimentName, logAddress);
        String containerMigrationAddress;
        String vmMigrationAddress;
        int index = getExperimentName().lastIndexOf("_");
        containerMigrationAddress = String.format("%s/ContainerMigration/%s/%s.csv",getLogAddress(), getExperimentName().substring(0,index) ,getExperimentName());
        String energyConsumptionAddress = String.format("%s/EnergyConsumption/%s/%s.csv", getLogAddress(), getExperimentName().substring(0,index) ,getExperimentName()) ;
        vmMigrationAddress = String.format("%s/ContainerMigration/%s/VM-%s.csv", getLogAddress(), getExperimentName().substring(0,index) ,getExperimentName());
        setContainerMigrationWriter(new CostumeCSVWriter(containerMigrationAddress));
        setVmMigrationWriter(new CostumeCSVWriter(vmMigrationAddress));
        setDatacenterEnergyWriter(new CostumeCSVWriter(energyConsumptionAddress));
        setPower(0.0);
        setDisableVmMigrations(false);
        setCloudletSubmitted(-1);
        setVmMigrationCount(0);
        setActiveHostList(new ArrayList<Double>());
        setActiveVmList(new ArrayList<Double>());
        setDatacenterEnergyList(new ArrayList<Double>());
        setContainerMigrationList(new ArrayList<Double>());
        setNumberOfVms(0);
        setNumberOfContainers(0);
    }

    /**
     * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
     * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
     * updating cloudlets inside them must be called from the outside.
     *
     * @pre $none
     * @post $none
     */
    @Override
    protected void updateCloudletProcessing() {
//        Log.printLine("Power data center is Updating the cloudlet processing");
        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
            schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            return;
        }
        double currentTime = CloudSim.clock();

        // if some time passed since last processing
        if (currentTime > getLastProcessTime()) {
            System.out.print(currentTime + " ");

            double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

            if (!isDisableVmMigrations()) {
                List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
                        getContainerVmList());
                int previousMigrationCount = getVmMigrationCount();
                if (migrationMap != null) {
                    for (Map<String, Object> migrate : migrationMap) {
                        ContainerVm vm = (ContainerVm) migrate.get("vm");
                        PowerContainerHost targetHost = (PowerContainerHost) migrate.get("host");
                        PowerContainerHost oldHost = (PowerContainerHost) vm.getHost();

                        if (oldHost == null) {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d to Host #%d is started",
                                    currentTime,
                                    vm.getId(),
                                    targetHost.getId());
                        } else {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                                    currentTime,
                                    vm.getId(),
                                    oldHost.getId(),
                                    targetHost.getId());
                        }
                        targetHost.addMigratingInContainerVm(vm);
                        incrementMigrationCount();

                        /** VM migration delay = RAM / bandwidth **/
                        // we use BW / 2 to model BW available for migration purposes, the other
                        // half of BW is for VM communication
                        // around 16 seconds for 1024 MB using 1 Gbit/s network
                        send(
                                getId(),
                                vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
                                CloudSimTags.VM_MIGRATE,
                                migrate);
                    }

                }
                Log.printConcatLine(CloudSim.clock(), ": The number of Migrations is:  ", getVmMigrationCount() - previousMigrationCount);
//                String[] msg={Double.toString (CloudSim.clock()), Integer.toString (getVmMigrationCount() - previousMigrationCount)  } ;                   // <--declared statement
//                try {
//                    getVmMigrationWriter().writeTofile(msg);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Log.printConcatLine(CloudSim.clock(),": The total number of Migrations is:  ",getVmMigrationCount());
            }

            // schedules an event to the next time
            if (minTime != Double.MAX_VALUE) {
                CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            }

            setLastProcessTime(currentTime);
        }
    }

    /**
     * Update cloudet processing without scheduling future events.
     *
     * @return the double
     */
    protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
//        Log.printLine("Power data center is Updating CloudetProcessingWithoutSchedulingFutureEvents");
        if (CloudSim.clock() > getLastProcessTime()) {
            return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
        }
        return 0;
    }

    /**
     * Update cloudet processing without scheduling future events.
     *
     * @return the double
     */
    protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
//        Log.printLine("Power data center is Updating CloudetProcessingWithoutSchedulingFutureEventsForce");
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;

        Log.printLine("\n\n--------------------------------------------------------------\n\n");
        Log.formatLine("Power data center: New resource usage for the time frame starting at %.2f:", currentTime);

        for (PowerContainerHost host : this.<PowerContainerHost>getHostList()) {
            Log.printLine();

            double time = host.updateContainerVmsProcessing(currentTime); // inform VMs to update processing
            if (time < minTime) {
                minTime = time;
            }

            Log.formatLine(
                    "%.2f: [Host #%d] utilization is %.2f%%",
                    currentTime,
                    host.getId(),
                    host.getUtilizationOfCpu() * 100);
        }

        if (timeDiff > 0) {
            Log.formatLine(
                    "\nEnergy consumption for the last time frame from %.2f to %.2f:",
                    getLastProcessTime(),
                    currentTime);

            for (PowerContainerHost host : this.<PowerContainerHost>getHostList()) {
                double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
                double utilizationOfCpu = host.getUtilizationOfCpu();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu,
                        utilizationOfCpu,
                        timeDiff);
                timeFrameDatacenterEnergy += timeFrameHostEnergy;

                Log.printLine();
                Log.formatLine(
                        "%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
                        currentTime,
                        host.getId(),
                        getLastProcessTime(),
                        previousUtilizationOfCpu * 100,
                        utilizationOfCpu * 100);
                Log.formatLine(
                        "%.2f: [Host #%d] energy is %.2f W*sec",
                        currentTime,
                        host.getId(),
                        timeFrameHostEnergy);
            }

            Log.formatLine(
                    "\n%.2f: Data center's energy is %.2f W*sec\n",
                    currentTime,
                    timeFrameDatacenterEnergy);
            getDatacenterEnergyList().add(timeFrameDatacenterEnergy);

        }

        setPower(getPower() + timeFrameDatacenterEnergy);

        String[]msg ={Double.toString(currentTime),Double.toString(getPower())};
        try {
            getDatacenterEnergyWriter().writeTofile(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }


        checkCloudletCompletion();

        int numberOfActiveHosts =0;
        /** Remove completed VMs **/
        for (PowerContainerHost host : this.<PowerContainerHost>getHostList()) {
            for (ContainerVm vm : host.getCompletedVms()) {
                getVmAllocationPolicy().deallocateHostForVm(vm);
                getContainerVmList().remove(vm);
                Log.printLine(String.format("VM #%d has been deallocated from host #%d", vm.getId(), host.getId()));
            }
            if(host.getVmList().size() !=0){

                numberOfActiveHosts ++;
            }
        }
        updateNumberOfVmsContainers();
        getActiveHostList().add((double) numberOfActiveHosts);
        int numberOfActiveVms = getNumberOfVms();
        getActiveVmList().add((double) numberOfActiveVms);
        int numberOfContainers = getNumberOfContainers();
        /** Check how many containers are in the system up and running*/
        Log.print(String.format("The number of Containers Up and running is %d", numberOfContainers));
        Log.printLine();
        Log.print(String.format("The number of Vms Up and running is %d", numberOfActiveVms));
        Log.printLine();
        Log.print(String.format("The number of Hosts Up and running is %d", numberOfActiveHosts));
        Log.printLine();

        setLastProcessTime(currentTime);
        return minTime;
    }

    /*
     * (non-Javadoc)
     * @see org.cloudbus.cloudsim.Datacenter#processVmMigrate(org.cloudbus.cloudsim.core.SimEvent,
     * boolean)
     */
    @Override
    protected void processVmMigrate(SimEvent ev, boolean ack) {
        updateCloudetProcessingWithoutSchedulingFutureEvents();
        super.processVmMigrate(ev, ack);
        SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
        if (event == null || event.eventTime() > CloudSim.clock()) {
            updateCloudetProcessingWithoutSchedulingFutureEventsForce();
        }
    }
//    /*
//         * (non-Javadoc)
//         * @see org.cloudbus.cloudsim.Datacenter#processVmMigrate(org.cloudbus.cloudsim.core.SimEvent,
//         * boolean)
//         */
//    @Override
//    protected void processContainerMigrate(SimEvent ev, boolean ack) {
//        updateCloudetProcessingWithoutSchedulingFutureEvents();
//        super.processContainerMigrate(ev, ack);
//        SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(containerCloudSimCloudTags.CONTAINER_MIGRATE));
//        if (event == null || event.eventTime() > CloudSim.clock()) {
//            updateCloudetProcessingWithoutSchedulingFutureEventsForce();
//        }
//    }
    /*
     * (non-Javadoc)
     * @see cloudsim.Datacenter#processCloudletSubmit(cloudsim.core.SimEvent, boolean)
     */
    @Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
//        Log.printLine("Power data center is processing cloudlet submit");
        super.processCloudletSubmit(ev, ack);
//        Log.printLine("Power Data Center : Set Cloudlet Submited to " + CloudSim.clock());
        setCloudletSubmitted(CloudSim.clock());
    }

    /**
     * Gets the power.
     *
     * @return the power
     */
    public double getPower() {
        return power;
    }

    /**
     * Sets the power.
     *
     * @param power the new power
     */
    protected void setPower(double power) {
        this.power = power;
    }

    /**
     * Checks if PowerDatacenter is in migration.
     *
     * @return true, if PowerDatacenter is in migration
     */
    protected boolean isInMigration() {
        boolean result = false;
        for (ContainerVm vm : getContainerVmList()) {
            if (vm.isInMigration()) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Checks if is disable migrations.
     *
     * @return true, if is disable migrations
     */
    public boolean isDisableVmMigrations() {
        return disableVmMigrations;
    }

    /**
     * Sets the disable migrations.
     *
     * @param disableVmMigrations the new disable migrations
     */
    public void setDisableVmMigrations(boolean disableVmMigrations) {
        this.disableVmMigrations = disableVmMigrations;
    }

    /**
     * Checks if is cloudlet submited.
     *
     * @return true, if is cloudlet submited
     */
    protected double getCloudletSubmitted() {
        return cloudletSubmitted;
    }

    /**
     * Sets the cloudlet submited.
     *
     * @param cloudletSubmitted the new cloudlet submited
     */
    protected void setCloudletSubmitted(double cloudletSubmitted) {
        this.cloudletSubmitted = cloudletSubmitted;
    }

    /**
     * Gets the migration count.
     *
     * @return the migration count
     */
    public int getVmMigrationCount() {
        return vmMigrationCount;
    }

    /**
     * Sets the migration count.
     *
     * @param vmMigrationCount the new migration count
     */
    protected void setVmMigrationCount(int vmMigrationCount) {
        this.vmMigrationCount = vmMigrationCount;
    }

    /**
     * Increment migration count.
     */
    protected void incrementMigrationCount() {
        setVmMigrationCount(getVmMigrationCount() + 1);
    }

    public CostumeCSVWriter getContainerMigrationWriter() {
        return containerMigrationWriter;
    }

    public void setContainerMigrationWriter(CostumeCSVWriter containerMigrationWriter) {
        this.containerMigrationWriter = containerMigrationWriter;
    }


    public CostumeCSVWriter getDatacenterEnergyWriter() {
        return (CostumeCSVWriter) datacenterEnergyWriter;
    }

    public void setDatacenterEnergyWriter(CostumeCSVWriter datacenterEnergyWriter) {
        this.datacenterEnergyWriter = datacenterEnergyWriter;
    }

    public List<Double> getActiveVmList() {
        return activeVmList;
    }

    public void setActiveVmList(List<Double> activeVmList) {
        this.activeVmList = activeVmList;
    }

    public List<Double> getActiveHostList() {
        return activeHostList;
    }

    public void setActiveHostList(ArrayList<Double> activeHostList) {
        this.activeHostList = activeHostList;
    }

    public List<Double> getDatacenterEnergyList() {
        return datacenterEnergyList;
    }

    public void setDatacenterEnergyList(List<Double> datacenterEnergyList) {
        this.datacenterEnergyList = datacenterEnergyList;
    }
    public List<Double> getContainerMigrationList() {
        return containerMigrationList;
    }

    public void setContainerMigrationList(List<Double> containerMigrationList) {
        this.containerMigrationList = containerMigrationList;
    }

    public CostumeCSVWriter getVmMigrationWriter() {
        return vmMigrationWriter;
    }

    public void setVmMigrationWriter(CostumeCSVWriter vmMigrationWriter) {
        this.vmMigrationWriter = vmMigrationWriter;
    }


    public void updateNumberOfVmsContainers() {
        setNumberOfVms(0);
        setNumberOfContainers(0);
        List<ContainerVm> temp= new ArrayList<>();
        for(ContainerHost host:getHostList()) {
            for (ContainerVm vm : host.getVmList()) {
                if (!temp.contains(vm)) {
                    int tempNumbers = this.getNumberOfVms() + 1;
                    setNumberOfVms(tempNumbers);
                    tempNumbers = this.getNumberOfContainers() + vm.getNumberOfContainers();
                    setNumberOfContainers(tempNumbers);
                    temp.add(vm);

                }
            }
            }
        temp.clear();
    }
    public int getNumberOfVms() {

        return numberOfVms;
    }
    public void setNumberOfVms(int numberOfVms) {
        this.numberOfVms = numberOfVms;
    }
    public int getNumberOfContainers() {

        return numberOfContainers;
    }
    public void setNumberOfContainers(int numberOfVms) {
        this.numberOfContainers = numberOfVms;
    }







}



