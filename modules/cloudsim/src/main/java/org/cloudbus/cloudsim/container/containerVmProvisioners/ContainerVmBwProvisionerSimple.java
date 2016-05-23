package org.cloudbus.cloudsim.container.containerVmProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerVmBwProvisionerSimple extends ContainerVmBwProvisioner {

    /**
     * The bw table.
     */
    private Map<String, Long> bwTable;

    /**
     * Instantiates a new bw provisioner simple.
     *
     * @param bw the bw
     */
    public ContainerVmBwProvisionerSimple(long bw) {
        super(bw);
        setBwTable(new HashMap<String, Long>());
    }


    @Override
    public boolean allocateBwForContainerVm(ContainerVm containerVm, long bw) {
        deallocateBwForContainerVm(containerVm);

        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getBwTable().put(containerVm.getUid(), bw);
            containerVm.setCurrentAllocatedBw(getAllocatedBwForContainerVm(containerVm));
            return true;
        }

        containerVm.setCurrentAllocatedBw(getAllocatedBwForContainerVm(containerVm));
        return false;
    }

    @Override
    public long getAllocatedBwForContainerVm(ContainerVm containerVm) {
        if (getBwTable().containsKey(containerVm.getUid())) {
            return getBwTable().get(containerVm.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateBwForContainerVm(ContainerVm containerVm) {
        if (getBwTable().containsKey(containerVm.getUid())) {
            long amountFreed = getBwTable().remove(containerVm.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            containerVm.setCurrentAllocatedBw(0);
        }

    }

    /*
         * (non-Javadoc)
         * ContainerVmBwProvisioner#deallocateBwForAllContainerVms
         */
    @Override
    public void deallocateBwForAllContainerVms() {
        super.deallocateBwForAllContainerVms();
        getBwTable().clear();
    }

    @Override
    public boolean isSuitableForContainerVm(ContainerVm containerVm, long bw) {
        long allocatedBw = getAllocatedBwForContainerVm(containerVm);
        boolean result = allocateBwForContainerVm(containerVm, bw);
        deallocateBwForContainerVm(containerVm);
        if (allocatedBw > 0) {
            allocateBwForContainerVm(containerVm, allocatedBw);
        }
        return result;
    }


    /**
     * Gets the bw table.
     *
     * @return the bw table
     */
    protected Map<String, Long> getBwTable() {
        return bwTable;
    }

    /**
     * Sets the bw table.
     *
     * @param bwTable the bw table
     */
    protected void setBwTable(Map<String, Long> bwTable) {
        this.bwTable = bwTable;
    }
}
