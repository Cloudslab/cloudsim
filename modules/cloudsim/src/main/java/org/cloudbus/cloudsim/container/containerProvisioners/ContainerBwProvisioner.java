package org.cloudbus.cloudsim.container.containerProvisioners;

import org.cloudbus.cloudsim.container.core.Container;

/**
 * ContainerBwProvisioner is an abstract class that represents the provisioning policy of bandwidth to
 * containers inside a vm. When extending this class, care must be taken to guarantee that
 * the field availableBw will always contain the amount of free bandwidth available for future
 * allocations.
 *
 * @author Rodrigo N. Calheiros
 * @author Sareh Fotuhi Piraghaj
 */
public abstract class ContainerBwProvisioner {


    /**
     * The vmBw.
     */
    private long vmBw;

    /**
     * The available availableVmBw.
     */
    private long availableVmBw;


    /**
     * Creates the new ContainerBwProvisioner.
     *
     * @param vmBw the Vm BW
     * @pre bw >= 0
     * @post $none
     */
    public ContainerBwProvisioner(long vmBw) {
        // TODO Auto-generated constructor stub
        setVmBw(vmBw);
        setAvailableVmBw(vmBw);
    }

    /**
     * Allocates BW for a given container.
     *
     * @param container containercontainer for which the bw are being allocated
     * @param bw        the bw
     * @return $true if the bw could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateBwForContainer(Container container, long bw);

    /**
     * Gets the allocated BW for container.
     *
     * @param container the container
     * @return the allocated BW for container
     */
    public abstract long getAllocatedBwForContainer(Container container);

    /**
     * Releases BW used by a container.
     *
     * @param container the container
     * @pre $none
     * @post none
     */
    public abstract void deallocateBwForContainer(Container container);

    /**
     * Releases BW used by a all containers.
     *
     * @pre $none
     * @post none
     */
    public void deallocateBwForAllContainers() {
        setAvailableVmBw(getBw());
    }


    /**
     * Checks if BW is suitable for container.
     *
     * @param container the container
     * @param bw        the bw
     * @return true, if BW is suitable for container
     */
    public abstract boolean isSuitableForContainer(Container container, long bw);


    /**
     * Gets the amount of used BW in the vm.
     *
     * @return used bw
     * @pre $none
     * @post $none
     */
    public long getUsedVmBw() {
        return vmBw - availableVmBw;
    }


    /**
     * @return the vmBw
     */
    public long getBw() {
        return vmBw;
    }

    /**
     * @param vmBw the vmBw to set
     */
    public void setVmBw(long vmBw) {
        this.vmBw = vmBw;
    }

    /**
     * @return the availableVmBw
     */
    public long getAvailableVmBw() {
        return availableVmBw;
    }

    /**
     * @param availableVmBw the availableVmBw to set
     */
    public void setAvailableVmBw(long availableVmBw) {
        this.availableVmBw = availableVmBw;
    }

}
