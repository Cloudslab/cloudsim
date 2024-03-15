/**
 *
 */
package org.cloudbus.cloudsim.container.containerProvisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * @author Sareh Fotuhi Piraghaj
 */
public class ContainerPeProvisionerSimple extends ContainerPeProvisioner {

    /**
     * The pe table.
     */
    private Map<String, List<Double>> peTable;
    /**
     * @param mips
     */
    /**
     * Creates the PeProvisionerSimple object.
     *
     * @param availableMips the available mips
     * @pre $none
     * @post $none
     */
    public ContainerPeProvisionerSimple(double availableMips) {
        super(availableMips);
        setPeTable(new HashMap<String, ArrayList<Double>>());
    }

    @Override
    public boolean allocateMipsForGuest(GuestEntity guest, double mips) {
        // TODO Auto-generated method stub

        return allocateMipsForGuest(guest.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForGuest(String containerUid, double mips) {
        if (getAvailableMips() < mips) {
            return false;
        }

        List<Double> allocatedMips;

        if (getPeTable().containsKey(containerUid)) {
            allocatedMips = getPeTable().get(containerUid);
        } else {
            allocatedMips = new ArrayList<>();
        }

        allocatedMips.add(mips);

        setAvailableMips(getAvailableMips() - mips);
        getPeTable().put(containerUid, allocatedMips);

        return true;
    }

    @Override
    public boolean allocateMipsForGuest(GuestEntity guest, List<Double> mips) {
        int totalMipsToAllocate = 0;
        for (double _mips : mips) {
            totalMipsToAllocate += _mips;
        }

        if (getAvailableMips() + getTotalAllocatedMipsForGuest(guest) < totalMipsToAllocate) {
            return false;
        }

        setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForGuest(guest) - totalMipsToAllocate);

        getPeTable().put(guest.getUid(), mips);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see containerProvisioners.PeProvisioner#deallocateMipsForAllContainers()
     */
    @Override
    public void deallocateMipsForAllGuests() {
        super.deallocateMipsForAllGuests();
        getPeTable().clear();
    }

    /* (non-Javadoc)
     * @see ContainerPeProvisioner#getAllocatedMipsForContainer
     */
    @Override
    public List<Double> getAllocatedMipsForGuest(GuestEntity guest) {
        if (getPeTable().containsKey(guest.getUid())) {
            return getPeTable().get(guest.getUid());
        }
        return null;
    }

    @Override
    public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
        if (getPeTable().containsKey(guest.getUid())) {
            double totalAllocatedMips = 0.0;
            for (double mips : getPeTable().get(guest.getUid())) {
                totalAllocatedMips += mips;
            }
            return totalAllocatedMips;
        }
        return 0;
    }

    @Override
    public double getAllocatedMipsForGuestByVirtualPeId(GuestEntity guest,
                                                        int peId) {
        if (getPeTable().containsKey(guest.getUid())) {
            try {
                return getPeTable().get(guest.getUid()).get(peId);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void deallocateMipsForGuest(GuestEntity guest) {
        if (getPeTable().containsKey(guest.getUid())) {
            for (double mips : getPeTable().get(guest.getUid())) {
                setAvailableMips(getAvailableMips() + mips);
            }
            getPeTable().remove(guest.getUid());
        }
    }

    /**
     * Gets the pe table.
     *
     * @return the peTable
     */
    protected Map<String, List<Double>> getPeTable() {
        return peTable;
    }

    /**
     * Sets the pe table.
     *
     * @param peTable the peTable to set
     */
    @SuppressWarnings("unchecked")
    protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
        this.peTable = (Map<String, List<Double>>) peTable;
    }


}
