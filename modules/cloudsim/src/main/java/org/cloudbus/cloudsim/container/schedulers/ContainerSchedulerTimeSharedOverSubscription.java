package org.cloudbus.cloudsim.container.schedulers;

import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.lists.ContainerPeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 22/07/15.
 */
public class ContainerSchedulerTimeSharedOverSubscription extends ContainerSchedulerTimeShared {
    /**
     * Instantiates a new container scheduler time shared.
     *
     * @param pelist the pelist
     */
    public ContainerSchedulerTimeSharedOverSubscription(List<? extends ContainerPe> pelist) {
        super(pelist);
    }


    @Override
    public boolean allocatePesForContainer(String containerUid, List<Double> mipsShareRequested) {
        double totalRequestedMips = 0;

        // if the requested mips is bigger than the capacity of a single PE, we cap
        // the request to the PE's capacity
        List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
        double peMips = getPeCapacity();
        for (Double mips : mipsShareRequested) {
            if (mips > peMips) {
                mipsShareRequestedCapped.add(peMips);
                totalRequestedMips += peMips;
            } else {
                mipsShareRequestedCapped.add(mips);
                totalRequestedMips += mips;
            }
        }



        if (getContainersMigratingIn().contains(containerUid)) {
            // the destination host only experience 10% of the migrating VM's MIPS
            totalRequestedMips = 0.0;
        }else {

            getMipsMapRequested().put(containerUid, mipsShareRequested);
            setPesInUse(getPesInUse() + mipsShareRequested.size());

        }

        if (getAvailableMips() >= totalRequestedMips) {
            List<Double> mipsShareAllocated = new ArrayList<Double>();
            for (Double mipsRequested : mipsShareRequestedCapped) {
//                if (getContainersMigratingOut().contains(containerUid)) {
//                    // performance degradation due to migration = 10% MIPS
//                    mipsRequested *= 0.9;
//                } else
                if (!getContainersMigratingIn().contains(containerUid)) {
                    // the destination host only experience 10% of the migrating VM's MIPS
                    mipsShareAllocated.add(mipsRequested);
                }
            }

            getMipsMap().put(containerUid, mipsShareAllocated);
//            getMipsMap().put(containerUid, mipsShareRequestedCapped);
            setAvailableMips(getAvailableMips() - totalRequestedMips);
        } else {
            redistributeMipsDueToOverSubscription();
        }

        return true;
    }

    /**
     * This method recalculates distribution of MIPs among VMs considering eventual shortage of MIPS
     * compared to the amount requested by VMs.
     */
    protected void redistributeMipsDueToOverSubscription() {
        // First, we calculate the scaling factor - the MIPS allocation for all VMs will be scaled
        // proportionally
        double totalRequiredMipsByAllVms = 0;

        Map<String, List<Double>> mipsMapCapped = new HashMap<String, List<Double>>();
        for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {

            double requiredMipsByThisContainer = 0.0;
            String vmId = entry.getKey();
            List<Double> mipsShareRequested = entry.getValue();
            List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
            double peMips = getPeCapacity();
            for (Double mips : mipsShareRequested) {
                if (mips > peMips) {
                    mipsShareRequestedCapped.add(peMips);
                    requiredMipsByThisContainer += peMips;
                } else {
                    mipsShareRequestedCapped.add(mips);
                    requiredMipsByThisContainer += mips;
                }
            }

            mipsMapCapped.put(vmId, mipsShareRequestedCapped);

//            if (getContainersMigratingIn().contains(entry.getKey())) {
//                // the destination host only experience 10% of the migrating VM's MIPS
//                requiredMipsByThisContainer *= 0.1;
//            }
            totalRequiredMipsByAllVms += requiredMipsByThisContainer;
        }

        double totalAvailableMips = ContainerPeList.getTotalMips(getPeList());
        double scalingFactor = totalAvailableMips / totalRequiredMipsByAllVms;

        // Clear the old MIPS allocation
        getMipsMap().clear();

        // Update the actual MIPS allocated to the VMs
        for (Map.Entry<String, List<Double>> entry : mipsMapCapped.entrySet()) {
            String vmUid = entry.getKey();
            List<Double> requestedMips = entry.getValue();

            List<Double> updatedMipsAllocation = new ArrayList<Double>();
            for (Double mips : requestedMips) {
//                if (getContainersMigratingOut().contains(vmUid)) {
                    // the original amount is scaled
//                    mips *= scalingFactor;
                    // performance degradation due to migration = 10% MIPS
//                    mips *= 0.9;
//                } else
                if (!getContainersMigratingIn().contains(vmUid)) {
                    // the destination host only experiences 10% of the migrating VM's MIPS
                    mips *= scalingFactor;

                    updatedMipsAllocation.add(Math.floor(mips));
                    // the final 10% of the requested MIPS are scaled
//                    mips *= scalingFactor;
                }


            }

            requestedMips.clear();

            // add in the new map
            getMipsMap().put(vmUid, updatedMipsAllocation);

        }

        mipsMapCapped.clear();
        // As the host is oversubscribed, there no more available MIPS
        setAvailableMips(0);
    }

}



