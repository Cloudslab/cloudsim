package org.cloudbus.cloudsim.container.containerVmProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerVmPeProvisioner {


    /** The mips. */
    private double mips;

    /** The available mips. */
    private double availableMips;

    /**
     * Creates the new PeProvisioner.
     *
     * @param mips overall amount of MIPS available in the Pe
     *
     * @pre mips>=0
     * @post $none
     */
    public ContainerVmPeProvisioner(double mips) {
        // TODO Auto-generated constructor stub
        setMips(mips);
        setAvailableMips(mips);
    }
    /**
     * Allocates MIPS for a given containerVm.
     *
     * @param containerVm virtual machine for which the MIPS are being allocated
     * @param mips the mips
     *
     * @return $true if the MIPS could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateMipsForContainerVm(ContainerVm containerVm, double mips);

    /**
     * Allocates MIPS for a given VM.
     *
     * @param containerVmUid the containerVmUid
     * @param mips the mips
     *
     * @return $true if the MIPS could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateMipsForContainerVm(String containerVmUid, double mips);

    /**
     * Allocates MIPS for a given VM.
     *
     * @param containerVm virtual machine for which the MIPS are being allocated
     * @param mips the mips for each virtual Pe
     *
     * @return $true if the MIPS could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateMipsForContainerVm(ContainerVm containerVm, List<Double> mips);

    /**
     * Gets allocated MIPS for a given VM.
     *
     * @param containerVm virtual machine for which the MIPS are being allocated
     *
     * @return array of allocated MIPS
     *
     * @pre $none
     * @post $none
     */
    public abstract List<Double> getAllocatedMipsForContainerVm(ContainerVm containerVm);

    /**
     * Gets total allocated MIPS for a given VM for all PEs.
     *
     * @param containerVm virtual machine for which the MIPS are being allocated
     *
     * @return total allocated MIPS
     *
     * @pre $none
     * @post $none
     */
    public abstract double getTotalAllocatedMipsForContainerVm(ContainerVm containerVm);

    /**
     * Gets allocated MIPS for a given VM for a given virtual Pe.
     *
     * @param containerVm virtual machine for which the MIPS are being allocated
     * @param peId the pe id
     *
     * @return allocated MIPS
     *
     * @pre $none
     * @post $none
     */
    public abstract double getAllocatedMipsForContainerVmByVirtualPeId(ContainerVm containerVm, int peId);

    /**
     * Releases MIPS used by a VM.
     *
     * @param containerVm the containerVm
     *
     * @pre $none
     * @post none
     */
    public abstract void deallocateMipsForContainerVm(ContainerVm containerVm);

    /**
     * Releases MIPS used by all VMs.
     *
     * @pre $none
     * @post none
     */
    public void deallocateMipsForAllContainerVms() {
        setAvailableMips(getMips());
    }

    /**
     * Gets the MIPS.
     *
     * @return the MIPS
     */
    public double getMips() {
        return mips;
    }

    /**
     * Sets the MIPS.
     *
     * @param mips the MIPS to set
     */
    public void setMips(double mips) {
        this.mips = mips;
    }

    /**
     * Gets the available MIPS in the PE.
     *
     * @return available MIPS
     *
     * @pre $none
     * @post $none
     */
    public double getAvailableMips() {
        return availableMips;
    }

    /**
     * Sets the available MIPS.
     *
     * @param availableMips the availableMips to set
     */
    protected void setAvailableMips(double availableMips) {
        this.availableMips = availableMips;
    }

    /**
     * Gets the total allocated MIPS.
     *
     * @return the total allocated MIPS
     */
    public double getTotalAllocatedMips() {
        double totalAllocatedMips = getMips() - getAvailableMips();
        if (totalAllocatedMips > 0) {
            return totalAllocatedMips;
        }
        return 0;
    }

    /**
     * Gets the utilization of the Pe in percents.
     *
     * @return the utilization
     */
    public double getUtilization() {
        return getTotalAllocatedMips() / getMips();
    }


}
