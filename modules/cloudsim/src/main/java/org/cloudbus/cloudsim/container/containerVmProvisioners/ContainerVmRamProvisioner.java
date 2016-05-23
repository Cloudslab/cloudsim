package org.cloudbus.cloudsim.container.containerVmProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerVm;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerVmRamProvisioner {

    /**
     * The ram.
     */
    private float ram;

    /**
     * The available availableRam.
     */
    private float availableRam;


    /**
     * Creates new Containervm Ram Provisioner
     *
     * @param availableContainerVmRam the vm ram
     */
    public ContainerVmRamProvisioner(float availableContainerVmRam) {
        setRam(availableContainerVmRam);
        setAvailableRam(availableContainerVmRam);
    }

    /**
     * allocate hosts ram to the VM
     *
     * @param containerVm the containerVm
     * @param ram       the ram
     * @return $true if successful
     */
    public abstract boolean allocateRamForContainerVm(ContainerVm containerVm, float ram);

    /**
     * Get the allocated ram of the containerVm
     *
     * @param containerVm the containerVm
     * @return the allocated ram of the containerVM
     */
    public abstract float getAllocatedRamForContainerVm(ContainerVm containerVm);

    /**
     * Release the allocated ram amount of the containerVm
     *
     * @param containerVm the containerVm
     */
    public abstract void deallocateRamForContainerVm(ContainerVm containerVm);

    /**
     * Release the allocated ram of the vm.
     */
    public void deallocateRamForAllContainerVms() {
        setAvailableRam(getRam());
    }


    /**
     * It checks whether or not the vm have enough ram for the containerVm
     *
     * @param containerVm the containerVm
     * @param ram       the vm's ram
     * @return $ture if it is suitable
     */
    public abstract boolean isSuitableForContainerVm(ContainerVm containerVm, float ram);

    /**
     * get the allocated ram of the Vm
     *
     * @return the used ram of the Vm
     */
    public float getUsedVmRam() {
        return getRam() - availableRam;
    }


    /**
     * get the available ram
     *
     * @return the available ram of the Vm
     */
    public float getAvailableRam() {
        return availableRam;
    }


    /**
     * It sets the available Ram of the virtual machine
     *
     * @param availableRam the availableRam
     */
    public void setAvailableRam(float availableRam) {
        this.availableRam = availableRam;
    }

    /**
     * @return the ram
     */
    public float getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(float ram) {
        this.ram = ram;
    }

}


