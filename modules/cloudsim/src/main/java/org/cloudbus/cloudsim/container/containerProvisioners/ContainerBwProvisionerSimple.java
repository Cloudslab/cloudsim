/**
 *
 */
package org.cloudbus.cloudsim.container.containerProvisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.container.core.Container;

/**
 * ContainerBwProvisionerSimple is a class that implements a simple best effort allocation policy: if there
 * is bw available to request, it allocates; otherwise, it fails.
 *
 * @author Sareh Fotuhi Piraghaj
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 */
public class ContainerBwProvisionerSimple extends ContainerBwProvisioner {
    /**
     * The container Bw table.
     */
    private Map<String, Long> containerBwTable;

    /**
     * Instantiates a new container bw provisioner simple.
     *
     * @param containerBw the containerBw
     */
    public ContainerBwProvisionerSimple(long containerBw) {
        super(containerBw);
        setContainerBwTable(new HashMap<String, Long>());
    }

    /**
     * Allocate BW for the container
     *
     * @param container
     * @param bw       the bw
     * @return
     */

    @Override
    public boolean allocateBwForContainer(Container container, long bw) {
        deallocateBwForContainer(container);
        if (getAvailableVmBw() >= bw) {
            setAvailableVmBw(getAvailableVmBw() - bw);
            getContainerBwTable().put(container.getUid(), bw);
            container.setCurrentAllocatedBw(getAllocatedBwForContainer(container));
            return true;
        }

        container.setCurrentAllocatedBw(getAllocatedBwForContainer(container));

        return false;
    }

    /**
     * Get allocated bandwidth for container
     * @param container
     * @return
     */
    @Override
    public long getAllocatedBwForContainer(Container container) {
        if (getContainerBwTable().containsKey(container.getUid())) {
            return getContainerBwTable().get(container.getUid());
        }
        return 0;
    }

    /**
     * Release the allocated BW for the container
     * @param container
     */
    @Override
    public void deallocateBwForContainer(Container container) {
        if (getContainerBwTable().containsKey(container.getUid())) {
            long amountFreed = getContainerBwTable().remove(container.getUid());
            setAvailableVmBw(getAvailableVmBw() + amountFreed);
            container.setCurrentAllocatedBw(0);
        }

    }

    /**
     * Release the VM bandwidth that is allocated to the container.
     */
    @Override
    public void deallocateBwForAllContainers() {
        super.deallocateBwForAllContainers();
        getContainerBwTable().clear();
    }

    /**
     * Check if the VM has enough BW to allocate to the container
     * @param container
     * @param bw        the bw
     * @return
     */
    @Override
    public boolean isSuitableForContainer(Container container, long bw) {
        long allocatedBw = getAllocatedBwForContainer(container);
        boolean result = allocateBwForContainer(container, bw);
        deallocateBwForContainer(container);
        if (allocatedBw > 0) {
            allocateBwForContainer(container, allocatedBw);
        }
        return result;
    }

    /**
     * @return the containerBwTable
     */
    protected Map<String, Long> getContainerBwTable() {
        return containerBwTable;
    }

    /**
     * @param containerBwTable the containerBwTable to set
     */
    protected void setContainerBwTable(Map<String, Long> containerBwTable) {
        this.containerBwTable = containerBwTable;
    }

}
