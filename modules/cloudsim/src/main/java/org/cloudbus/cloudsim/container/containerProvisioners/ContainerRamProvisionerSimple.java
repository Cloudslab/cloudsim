package org.cloudbus.cloudsim.container.containerProvisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * @author sareh
 * @author Remo Andreoli
 */
public class ContainerRamProvisionerSimple extends ContainerRamProvisioner {
    /**
     * The RAM table.
     */
    private Map<String, Integer> containerRamTable;

    /**
     * @param availableRam the available ram
     */
    public ContainerRamProvisionerSimple(int availableRam) {
        super(availableRam);
        setContainerRamTable(new HashMap<>());
    }

    /**
     * @param guest the container
     * @param ram   the ram
     * @return
     * @see ContainerRamProvisioner#allocateRamForContainer(Container, int)
     */
    @Override
    public boolean allocateRamForGuest(GuestEntity guest,
                                       int ram) {
        int maxRam = guest.getRam();

        if (ram >= maxRam) {
            ram = maxRam;
        }

        deallocateRamForGuest(guest);

        if (getAvailableVmRam() >= ram) {
            setAvailableVmRam(getAvailableVmRam() - ram);
            getContainerRamTable().put(guest.getUid(), ram);
            guest.setCurrentAllocatedRam(getAllocatedRamForGuest(guest));
            return true;
        }

        guest.setCurrentAllocatedRam(getAllocatedRamForGuest(guest));

        return false;
    }

    /**
     * @param guest the container
     * @return
     * @link ContainerRamProvisioner#getAllocatedRamForContainer(Container)
     */
    @Override
    public int getAllocatedRamForGuest(GuestEntity guest) {
        if (getContainerRamTable().containsKey(guest.getUid())) {
            return getContainerRamTable().get(guest.getUid());
        }
        return 0;
    }

    /**
     * @param guest the container
     * @see ContainerRamProvisioner#deallocateRamForContainer(Container)
     */
    @Override
    public void deallocateRamForGuest(GuestEntity guest) {
        if (getContainerRamTable().containsKey(guest.getUid())) {
            int amountFreed = getContainerRamTable().remove(guest.getUid());
            setAvailableVmRam(getAvailableVmRam() + amountFreed);
            guest.setCurrentAllocatedRam(0);
        }
    }

    /**
     * @see ContainerRamProvisioner#deallocateRamForAllGuests()
     */
    @Override
    public void deallocateRamForAllGuests() {
        super.deallocateRamForAllGuests();
        getContainerRamTable().clear();
    }


    /**
     * @param guest the container
     * @param ram   the vm's ram
     * @return
     * @see ContainerRamProvisioner#isSuitableForContainer(Container, int)
     */
    @Override
    public boolean isSuitableForGuest(GuestEntity guest,
                                      int ram) {
        int allocatedRam = getAllocatedRamForGuest(guest);
        boolean result = allocateRamForGuest(guest, ram);
        deallocateRamForGuest(guest);
        if (allocatedRam > 0) {
            allocateRamForGuest(guest, allocatedRam);
        }
        return result;
    }


    /**
     *
     * @return the containerRamTable
     */
    protected Map<String, Integer> getContainerRamTable() {
        return containerRamTable;
    }

    /**
     * @param containerRamTable the containerRamTable to set
     */
    protected void setContainerRamTable(Map<String, Integer> containerRamTable) {
        this.containerRamTable = containerRamTable;
    }

}
