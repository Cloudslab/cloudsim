package org.cloudbus.cloudsim.container.containerVmProvisioners;


import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerVmPeProvisionerSimple extends ContainerVmPeProvisioner {


    /** The pe table. */
    private Map<String, List<Double>> peTable;

    /**
     * Creates the PeProvisionerSimple object.
     *
     * @param availableMips the available mips
     *
     * @pre $none
     * @post $none
     */
    public ContainerVmPeProvisionerSimple(double availableMips) {
        super(availableMips);
        setPeTable(new HashMap<String, ArrayList<Double>>());
    }




    @Override
    public boolean allocateMipsForContainerVm(ContainerVm containerVm, double mips) {

        return allocateMipsForContainerVm(containerVm.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForContainerVm(String containerVmUid, double mips) {
        if (getAvailableMips() < mips) {
            return false;
        }

        List<Double> allocatedMips;

        if (getPeTable().containsKey(containerVmUid)) {
            allocatedMips = getPeTable().get(containerVmUid);
        } else {
            allocatedMips = new ArrayList<>();
        }

        allocatedMips.add(mips);

        setAvailableMips(getAvailableMips() - mips);
        getPeTable().put(containerVmUid, allocatedMips);

        return true;
    }

    @Override
    public boolean allocateMipsForContainerVm(ContainerVm containerVm, List<Double> mips) {
        int totalMipsToAllocate = 0;
        for (double _mips : mips) {
            totalMipsToAllocate += _mips;
        }

        if (getAvailableMips() + getTotalAllocatedMipsForContainerVm(containerVm)< totalMipsToAllocate) {
            return false;
        }

        setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForContainerVm(containerVm)- totalMipsToAllocate);

        getPeTable().put(containerVm.getUid(), mips);

        return true;
    }

    @Override
    public List<Double> getAllocatedMipsForContainerVm(ContainerVm containerVm) {
        if (getPeTable().containsKey(containerVm.getUid())) {
            return getPeTable().get(containerVm.getUid());
        }
        return null;
    }

    @Override
    public double getTotalAllocatedMipsForContainerVm(ContainerVm containerVm) {
        if (getPeTable().containsKey( containerVm.getUid())) {
            double totalAllocatedMips = 0.0;
            for (double mips : getPeTable().get(containerVm.getUid())) {
                totalAllocatedMips += mips;
            }
            return totalAllocatedMips;
        }
        return 0;
    }

    @Override
    public double getAllocatedMipsForContainerVmByVirtualPeId(ContainerVm containerVm, int peId) {
        if (getPeTable().containsKey(containerVm.getUid())) {
            try {
                return getPeTable().get(containerVm.getUid()).get(peId);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void deallocateMipsForContainerVm(ContainerVm containerVm) {
        if (getPeTable().containsKey(containerVm.getUid())) {
            for (double mips : getPeTable().get(containerVm.getUid())) {
                setAvailableMips(getAvailableMips() + mips);
            }
            getPeTable().remove(containerVm.getUid());
        }
    }

    @Override
    public void deallocateMipsForAllContainerVms() {
        super.deallocateMipsForAllContainerVms();
        getPeTable().clear();
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
