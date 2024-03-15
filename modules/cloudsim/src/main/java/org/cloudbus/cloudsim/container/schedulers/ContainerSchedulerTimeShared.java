package org.cloudbus.cloudsim.container.schedulers;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

import java.util.*;

/**
 * Created by sareh on 9/07/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class ContainerSchedulerTimeShared extends VmScheduler {
    /**
     * The mips map requested.
     */
    private Map<String, List<Double>> mipsMapRequested;

    /**
     * The pes in use.
     */
    private int pesInUse;

    /**
     * Instantiates a new container scheduler time shared.
     *
     * @param pelist the pelist
     */
    public ContainerSchedulerTimeShared(List<? extends Pe> pelist) {
        super(pelist);
        setMipsMapRequested(new HashMap<>());
    }


    @Override
    public boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShareRequested) {
        /**
         * TODO: add the same to RAM and BW provisioners
         */
//        Log.printLine("ContainerSchedulerTimeShared: allocatePesForContainer with mips share size......" + mipsShareRequested.size());
//        if (container.isInMigration()) {
//            if (!getContainersMigratingIn().contains(container.getUid()) && !getContainersMigratingOut().contains(container.getUid())) {
//                getContainersMigratingOut().add(container.getUid());
//            }
//        } else {
//            if (getContainersMigratingOut().contains(container.getUid())) {
//                getContainersMigratingOut().remove(container.getUid());
//            }
//        }
        boolean result = allocatePesForGuest(guest.getUid(), mipsShareRequested);
        updatePeProvisioning();
        return result;
    }

    /**
     * Allocate pes for vm.
     *
     * @param containerUid       the containerUid
     * @param mipsShareRequested the mips share requested
     * @return true, if successful
     */
    protected boolean allocatePesForGuest(String containerUid, List<Double> mipsShareRequested) {
//        Log.printLine("ContainerSchedulerTimeShared: allocatePesForContainerVm for containerUid......"+containerUid);
        double totalRequestedMips = 0;
        double peMips = getPeCapacity();
        for (Double mips : mipsShareRequested) {
            // each virtual PE of a VM must require not more than the capacity of a physical PE
            if (mips > peMips) {
                return false;
            }
            totalRequestedMips += mips;
        }

        // This scheduler does not allow over-subscription
        if (getAvailableMips() < totalRequestedMips) {
            return false;
        }

        getMipsMapRequested().put(containerUid, mipsShareRequested);
        setPesInUse(getPesInUse() + mipsShareRequested.size());

        if (getGuestsMigratingIn().contains(containerUid)) {
            // the destination host only experience nothing of the migrating Containers MIPS
            totalRequestedMips = 0;
//            totalRequestedMips *= 0.1;
        }

        List<Double> mipsShareAllocated = new ArrayList<>();
        for (Double mipsRequested : mipsShareRequested) {
//            if (getContainersMigratingOut().contains(containerUid)) {
//                // It should handle the container load fully
////                mipsRequested *= 0.9;
//            } else
            if (getGuestsMigratingIn().contains(containerUid)) {
                // not responsible for those moving in
                mipsRequested = 0.0;
            }
            mipsShareAllocated.add(mipsRequested);
        }

        getMipsMap().put(containerUid, mipsShareAllocated);
        setAvailableMips(getAvailableMips() - totalRequestedMips);

        return true;
    }

    /**
     * Update allocation of VMs on PEs.
     */
    protected void updatePeProvisioning() {
//        Log.printLine("VmSchedulerTimeShared: update the pe provisioning......");
        getPeMap().clear();
        for (Pe pe : getPeList()) {
            pe.getPeProvisioner().deallocateMipsForAllGuests();
        }

        Iterator<Pe> peIterator = getPeList().iterator();
        Pe pe = peIterator.next();
        PeProvisioner containerPeProvisioner = pe.getPeProvisioner();
        double availableMips = containerPeProvisioner.getAvailableMips();

        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
            String containerUid = entry.getKey();
            getPeMap().put(containerUid, new LinkedList<>());

            for (double mips : entry.getValue()) {
                while (mips >= 0.1) {
                    if (availableMips >= mips) {
                        containerPeProvisioner.allocateMipsForGuest(containerUid, mips);
                        getPeMap().get(containerUid).add(pe);
                        availableMips -= mips;
                        break;
                    } else {
                        containerPeProvisioner.allocateMipsForGuest(containerUid, availableMips);
                        if (availableMips != 0) {
                            getPeMap().get(containerUid).add(pe);
                        }
                        mips -= availableMips;
                        if (mips <= 0.1) {
                            break;
                        }
                        if (!peIterator.hasNext()) {
                            Log.printConcatLine("There is no enough MIPS (", mips, ") to accommodate VM ", containerUid);
                            // System.exit(0);
                        }
                        pe = peIterator.next();
                        containerPeProvisioner = pe.getPeProvisioner();
                        availableMips = containerPeProvisioner.getAvailableMips();
                    }
                }
            }
        }
    }

    @Override
    public void deallocatePesForGuest(GuestEntity guest) {
//        Log.printLine("containerSchedulerTimeShared: deallocatePesForContainer.....");
        getMipsMapRequested().remove(guest.getUid());
        setPesInUse(0);
        getMipsMap().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));

        for (Pe pe : getPeList()) {
            pe.getPeProvisioner().deallocateMipsForGuest(guest);
        }
//        Log.printLine("SchedulerTimeShared: deallocatePesForContainerVm. allocates again acording to the left!!!!!!!....");
        for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {
            allocatePesForGuest(entry.getKey(), entry.getValue());
        }

        updatePeProvisioning();
    }

    @Override
    public void deallocatePesForAllGuests() {
        super.deallocatePesForAllGuests();
        getMipsMapRequested().clear();
        setPesInUse(0);
    }

    @Override
    public double getMaxAvailableMips() {
        return getAvailableMips();
    }


    /**
     * Sets the pes in use.
     *
     * @param pesInUse the new pes in use
     */
    protected void setPesInUse(int pesInUse) {
        this.pesInUse = pesInUse;
    }

    /**
     * Gets the pes in use.
     *
     * @return the pes in use
     */
    protected int getPesInUse() {
        return pesInUse;
    }

    /**
     * Gets the mips map requested.
     *
     * @return the mips map requested
     */
    protected Map<String, List<Double>> getMipsMapRequested() {
        return mipsMapRequested;
    }

    /**
     * Sets the mips map requested.
     *
     * @param mipsMapRequested the mips map requested
     */
    protected void setMipsMapRequested(Map<String, List<Double>> mipsMapRequested) {
        this.mipsMapRequested = mipsMapRequested;
    }
}
