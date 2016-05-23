package org.cloudbus.cloudsim.container.schedulers;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.lists.ContainerPeList;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 9/07/15.
 */
public abstract class  ContainerScheduler {
    /** The peList. */
    private List<? extends ContainerPe> peList;

    /** The map of VMs to PEs. */
    private Map<String, List<ContainerPe>> peMap;

    /** The MIPS that are currently allocated to the VMs. */
    private Map<String, List<Double>> mipsMap;

    /** The total available mips. */
    private double availableMips;

    /** The VMs migrating in. */
    private List<String> containersMigratingIn;

    /** The VMs migrating out. */
    private List<String> containersMigratingOut;


    /**
     * Creates a new HostAllocationPolicy.
     *
     * @param pelist the pelist
     * @pre peList != $null
     * @post $none
     */
    public ContainerScheduler(List<? extends ContainerPe> pelist) {
        setPeList(pelist);
        setPeMap(new HashMap<String, List<ContainerPe>>());
        setMipsMap(new HashMap<String, List<Double>>());
        setAvailableMips(ContainerPeList.getTotalMips(getPeList()));
        setContainersMigratingIn(new ArrayList<String>());
        setContainersMigratingOut(new ArrayList<String>());

    }

    /**
     * Allocates PEs for a VM.
     *
     * @param container the container
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocatePesForContainer(Container container, List<Double> mipsShare);

    /**
     * Releases PEs allocated to a VM.
     *
     * @param container the container
     * @pre $none
     * @post $none
     */
    public abstract void deallocatePesForContainer(Container container);

    /**
     * Releases PEs allocated to all the VMs.
     *
     * @pre $none
     * @post $none
     */
    public void deallocatePesForAllContainers() {
        getMipsMap().clear();
        setAvailableMips(ContainerPeList.getTotalMips(getPeList()));
        for (ContainerPe pe : getPeList()) {
             pe.getContainerPeProvisioner().deallocateMipsForAllContainers();
        }
    }

    /**
     * Gets the pes allocated for container.
     *
     * @param container the container
     * @return the pes allocated for container
     */
    public List<ContainerPe> getPesAllocatedForVM(Container container) {
        return getPeMap().get(container.getUid());
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given VM.
     *
     * @param container the container
     * @return an array containing the amount of MIPS of each pe that is available to the VM
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForContainer(Container container) {
        return getMipsMap().get(container.getUid());
    }

    /**
     * Gets the total allocated MIPS for a VM over all the PEs.
     *
     * @param container the container
     * @return the allocated mips for container
     */
    public double getTotalAllocatedMipsForContainer(Container container) {
        double allocated = 0;
        List<Double> mipsMap = getAllocatedMipsForContainer(container);
        if (mipsMap != null) {
            for (double mips : mipsMap) {
                allocated += mips;
            }
        }
        return allocated;
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     *
     * @return max mips
     */
    public double getMaxAvailableMips() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }

        double max = 0.0;
        for (ContainerPe pe : getPeList()) {
            double tmp = (pe.getContainerPeProvisioner().getAvailableMips());
            if (tmp > max) {
                max = tmp;
            }
        }

        return max;
    }

    /**
     * Returns PE capacity in MIPS.
     *
     * @return mips
     */
    public double getPeCapacity() {
        if (getPeList() == null) {
            Log.printLine("Pe list is empty");
            return 0;
        }
        return getPeList().get(0).getMips();
    }

    /**
     * Gets the container list.
     *
     * @param <T> the generic type
     * @return the container list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerPe> List<T> getPeList() {
        return (List<T>) peList;
    }

    /**
     * Sets the container list.
     *
     * @param <T> the generic type
     * @param peList the pe list
     */
    protected <T extends ContainerPe> void setPeList(List<T> peList) {
        this.peList = peList;
    }

    /**
     * Gets the mips map.
     *
     * @return the mips map
     */
    protected Map<String, List<Double>> getMipsMap() {
        return mipsMap;
    }

    /**
     * Sets the mips map.
     *
     * @param mipsMap the mips map
     */
    protected void setMipsMap(Map<String, List<Double>> mipsMap) {
        this.mipsMap = mipsMap;
    }

    /**
     * Gets the free mips.
     *
     * @return the free mips
     */
    public double getAvailableMips() {
//        Log.printConcatLine("The available Mips is ", availableMips);
        return availableMips;
    }

    /**
     * Sets the free mips.
     *
     * @param availableMips the new free mips
     */
    protected void setAvailableMips(double availableMips) {
        this.availableMips = availableMips;
    }

    /**
     * Gets the containers in migration.
     *
     * @return the containers in migration
     */
    public List<String> getContainersMigratingOut() {
        return containersMigratingOut;
    }

    /**
     * Sets the containers in migration.
     *
     * @param containersInMigration the new containers migrating out
     */
    protected void setContainersMigratingOut(List<String> containersInMigration) {
        containersMigratingOut = containersInMigration;
    }

    /**
     * Gets the containers migrating in.
     *
     * @return the containers migrating in
     */
    public List<String> getContainersMigratingIn() {
        return containersMigratingIn;
    }

    /**
     * Sets the containers migrating in.
     *
     * @param containersMigratingIn the new containers migrating in
     */
    protected void setContainersMigratingIn(List<String> containersMigratingIn) {
        this.containersMigratingIn = containersMigratingIn;
    }

    /**
     * Gets the pe map.
     *
     * @return the pe map
     */
    public Map<String, List<ContainerPe>> getPeMap() {
        return peMap;
    }

    /**
     * Sets the pe map.
     *
     * @param peMap the pe map
     */
    protected void setPeMap(Map<String, List<ContainerPe>> peMap) {
        this.peMap = peMap;
    }




//
//
//
//    public abstract double getTotalUtilizationOfCpu(double time);
//    public abstract double getCurrentRequestedUtilizationOfRam();
//    public abstract double getCurrentRequestedUtilizationOfBw();
//    public abstract List<Double> getCurrentRequestedMips();
//    public abstract double updateContainerProcessing(double currentTime, List<Double> mipsShare);

}
