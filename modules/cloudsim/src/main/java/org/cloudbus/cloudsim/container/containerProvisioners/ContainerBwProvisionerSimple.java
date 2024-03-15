/**
 *
 */
package org.cloudbus.cloudsim.container.containerProvisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

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
        setContainerBwTable(new HashMap<>());
    }

    /**
     * Allocate BW for the container
     *
     * @param guest
     * @param bw    the bw
     * @return
     */

    @Override
    public boolean allocateBwForGuest(GuestEntity guest, long bw) {
        deallocateBwForGuest(guest);
        if (getAvailableVmBw() >= bw) {
            setAvailableVmBw(getAvailableVmBw() - bw);
            getContainerBwTable().put(guest.getUid(), bw);
            guest.setCurrentAllocatedBw(getAllocatedBwForGuest(guest));
            return true;
        }

        guest.setCurrentAllocatedBw(getAllocatedBwForGuest(guest));

        return false;
    }

    /**
     * Get allocated bandwidth for container
     *
     * @param guest
     * @return
     */
    @Override
    public long getAllocatedBwForGuest(GuestEntity guest) {
        if (getContainerBwTable().containsKey(guest.getUid())) {
            return getContainerBwTable().get(guest.getUid());
        }
        return 0;
    }

    /**
     * Release the allocated BW for the container
     *
     * @param guest
     */
    @Override
    public void deallocateBwForGuest(GuestEntity guest) {
        if (getContainerBwTable().containsKey(guest.getUid())) {
            long amountFreed = getContainerBwTable().remove(guest.getUid());
            setAvailableVmBw(getAvailableVmBw() + amountFreed);
            guest.setCurrentAllocatedBw(0);
        }

    }

    /**
     * Release the VM bandwidth that is allocated to the container.
     */
    @Override
    public void deallocateBwForAllGuests() {
        super.deallocateBwForAllGuests();
        getContainerBwTable().clear();
    }

    /**
     * Check if the VM has enough BW to allocate to the container
     *
     * @param guest
     * @param bw    the bw
     * @return
     */
    @Override
    public boolean isSuitableForGuest(GuestEntity guest, long bw) {
        long allocatedBw = getAllocatedBwForGuest(guest);
        boolean result = allocateBwForGuest(guest, bw);
        deallocateBwForGuest(guest);
        if (allocatedBw > 0) {
            allocateBwForGuest(guest, allocatedBw);
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
