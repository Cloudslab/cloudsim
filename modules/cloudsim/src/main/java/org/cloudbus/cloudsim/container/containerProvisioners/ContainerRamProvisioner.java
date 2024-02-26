package org.cloudbus.cloudsim.container.containerProvisioners;

import org.cloudbus.cloudsim.container.core.Container;

/**
 * This class takes care of the provisioning of Container's ram .
 * @author sareh
 */
public abstract class ContainerRamProvisioner {

    /**
     * The ram.
     */
    private int ram;

    /**
     * The available availableContainerRam.
     */
    private int availableVmRam;


    /**
     * Creates new Container Ram Provisioner
     *
     * @param availableContainerRam the vm ram
     */
    public ContainerRamProvisioner(int availableContainerRam) {
        setRam(availableContainerRam);
        setAvailableVmRam(availableContainerRam);
    }

    /**
     * allocate vms ram to the container
     *
     * @param container the container
     * @param ram       the ram
     * @return $true if successful
     */
    public abstract boolean allocateRamForContainer(Container container, int ram);

    /**
     * Get the allocated ram of the container
     *
     * @param container the container
     * @return the allocated ram of the container
     */
    public abstract int getAllocatedRamForContainer(Container container);

    /**
     * Release the allocated ram amount of the container
     *
     * @param container the container
     */
    public abstract void deallocateRamForContainer(Container container);

    /**
     * Release the allocated ram of the vm.
     */
    public void deallocateRamForAllContainers() {
        setAvailableVmRam(getRam());
    }

    /**
     * It checks whether or not the vm have enough ram for the container
     *
     * @param container the container
     * @param ram       the vm's ram
     * @return $ture if it is suitable
     */
    public abstract boolean isSuitableForContainer(Container container, int ram);

    /**
     * @return the ram
     */
    public int getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(int ram) {
        this.ram = ram;
    }

    /**
     * get the allocated ram of the Vm
     *
     * @return the used ram of the Vm
     */
    public int getUsedVmRam() {
        return getRam() - availableVmRam;
    }

    /**
     * get the available ram
     *
     * @return the available ram of the Vm
     */
    public int getAvailableVmRam() {
        return availableVmRam;
    }


    /**
     * It sets the available Ram of the virtual machine
     *
     * @param availableRam the availableVmRam
     */
    public void setAvailableVmRam(int availableRam) {
        this.availableVmRam = availableRam;
    }

}
