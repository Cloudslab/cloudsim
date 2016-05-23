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
    private float ram;

    /**
     * The available availableContainerRam.
     */
    private float availableVmRam;


    /**
     * Creates new Container Ram Provisioner
     *
     * @param availableContainerRam the vm ram
     */
    public ContainerRamProvisioner(float availableContainerRam) {
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
    public abstract boolean allocateRamForContainer(Container container, float ram);

    /**
     * Get the allocated ram of the container
     *
     * @param container the container
     * @return the allocated ram of the container
     */
    public abstract float getAllocatedRamForContainer(Container container);

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
    public abstract boolean isSuitableForContainer(Container container, float ram);

    /**
     * get the allocated ram of the Vm
     *
     * @return the used ram of the Vm
     */
    public float getUsedVmRam() {
        return getRam() - availableVmRam;
    }


    /**
     * get the available ram
     *
     * @return the available ram of the Vm
     */
    public float getAvailableVmRam() {
        return availableVmRam;
    }


    /**
     * It sets the available Ram of the virtual machine
     *
     * @param availableRam the availableVmRam
     */
    public void setAvailableVmRam(float availableRam) {
        this.availableVmRam = availableRam;
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
