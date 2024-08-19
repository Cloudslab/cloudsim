/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.VirtualEntity;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 14/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class ContainerHostDynamicWorkload extends Host {


        /** The utilization mips. */
        private double utilizationMips;

        /** The previous utilization mips. */
        private double previousUtilizationMips;

        /** The state history. */
        private final List<HostStateHistoryEntry> stateHistory = new ArrayList<>();

        /**
         * Instantiates a new host.
         *
         * @param id the id
         * @param ramProvisioner the ram provisioner
         * @param bwProvisioner the bw provisioner
         * @param storage the storage
         * @param peList the pe list
         * @param vmScheduler the VM scheduler
         */
        public ContainerHostDynamicWorkload(
                int id,
                RamProvisioner ramProvisioner,
                BwProvisioner bwProvisioner,
                long storage,
                List<? extends Pe> peList,
                VmScheduler vmScheduler) {
            super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
            setUtilizationMips(0);
            setPreviousUtilizationMips(0);
        }

        /*
         * (non-Javadoc)
         * @see cloudsim.Host#updateVmsProcessing(double)
         */
        @Override
        public double updateCloudletsProcessing(double currentTime) {
            double smallerTime = super.updateCloudletsProcessing(currentTime);
            setPreviousUtilizationMips(getUtilizationMips());
            setUtilizationMips(0);
            double hostTotalRequestedMips = 0;

            List<GuestEntity> containerVms = getGuestList();

            for (GuestEntity containerVm : containerVms) {
                getGuestScheduler().deallocatePesForGuest(containerVm);
            }

            for (GuestEntity  containerVm : containerVms) {
                getGuestScheduler().allocatePesForGuest(containerVm, containerVm.getCurrentRequestedMips());
            }

            for (GuestEntity containerVm : containerVms) {
                double totalRequestedMips = containerVm.getCurrentRequestedTotalMips();
                double totalAllocatedMips = getGuestScheduler().getTotalAllocatedMipsForGuest(containerVm);

                if (!Log.isDisabled()) {
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + containerVm.getId()
                                    + " (Host #" + containerVm.getHost().getId()
                                    + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                            CloudSim.clock(),
                            totalAllocatedMips,
                            totalRequestedMips,
                            containerVm.getMips(),
                            totalRequestedMips / containerVm.getMips() * 100);

                    List<Pe> pes = getGuestScheduler().getPesAllocatedForGuest(containerVm);
                    StringBuilder pesString = new StringBuilder();
                    for (Pe pe : pes) {
                        pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
                                .getTotalAllocatedMipsForGuest(containerVm)));
                    }
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] MIPS for VM #" + containerVm.getId() + " by PEs ("
                                    + getNumberOfPes() + " * " + getGuestScheduler().getPeCapacity() + ")."
                                    + pesString,
                            CloudSim.clock());
                }

                if (getGuestsMigratingIn().contains(containerVm)) {
                    Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + containerVm.getId()
                            + " is being migrated to Host #" + getId(), CloudSim.clock());
                } else {
                    if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                        Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + containerVm.getId()
                                + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                    }

                    containerVm.addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            (containerVm.isInMigration() && !getGuestsMigratingIn().contains(containerVm)));

                    if (containerVm.isInMigration()) {
                        Log.formatLine(
                                "%.2f: [Host #" + getId() + "] VM #" + containerVm.getId() + " is in migration",
                                CloudSim.clock());
                        totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                    }
                }

                setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
                hostTotalRequestedMips += totalRequestedMips;
            }

            addStateHistoryEntry(
                    currentTime,
                    getUtilizationMips(),
                    hostTotalRequestedMips,
                    (getUtilizationMips() > 0));

            return smallerTime;
        }

        /**
         * Gets the completed vms.
         *
         * @return the completed vms
         */
        public List<VirtualEntity> getCompletedVms() {
            List<VirtualEntity> vmsToRemove = new ArrayList<>();
            List<VirtualEntity> containerVms = getGuestList();

            for (VirtualEntity containerVm : containerVms) {
                if (containerVm.isInMigration()) {
                    continue;
                }
//                if the  vm is in waiting state then dont kill it just waite !!!!!!!!!
                 if(containerVm.isInWaiting()){
                     continue;
                 }
//              if (containerVm.getCurrentRequestedTotalMips() == 0) {
//                    vmsToRemove.add(containerVm);
//                }
                if(containerVm.getNumberOfGuests()==0 ){
                    vmsToRemove.add(containerVm);
                }
            }
            return vmsToRemove;
        }



    /**
     * Gets the completed vms.
     *
     * @return the completed vms
     */
    public int getNumberOfGuests() {
        int numberofContainers = 0;

        for (Vm containerVm : this.<Vm>getGuestList()) {
            numberofContainers += containerVm.getNumberOfGuests();
            Log.print("The number of containers in VM# " + containerVm.getId()+"is: "+ containerVm.getNumberOfGuests());
            Log.println();
        }
        return numberofContainers;
    }




        /**
         * Gets the max utilization among by all PEs.
         *
         * @return the utilization
         */
        public double getMaxUtilization() {
            return PeList.getMaxUtilization(getPeList());
        }

        /**
         * Gets the utilization of memory.
         *
         * @return the utilization of memory
         */
        public int getUtilizationOfRam() {
            return getGuestRamProvisioner().getUsedRam();
        }

        /**
         * Gets the utilization of bw.
         *
         * @return the utilization of bw
         */
        public double getUtilizationOfBw() {
            return getGuestBwProvisioner().getUsedBw();
        }

        /**
         * Get current utilization of CPU in percentage.
         *
         * @return current utilization of CPU in percents
         */
        public double getUtilizationOfCpu() {
            double utilization = getUtilizationMips() / getTotalMips();
            if (utilization > 1 && utilization < 1.01) {
                utilization = 1;
            }
            return utilization;
        }

        /**
         * Gets the previous utilization of CPU in percentage.
         *
         * @return the previous utilization of cpu
         */
        public double getPreviousUtilizationOfCpu() {
            double utilization = getPreviousUtilizationMips() / getTotalMips();
            if (utilization > 1 && utilization < 1.01) {
                utilization = 1;
            }
            return utilization;
        }

        /**
         * Get current utilization of CPU in MIPS.
         *
         * @return current utilization of CPU in MIPS
         */
        public double getUtilizationOfCpuMips() {
            return getUtilizationMips();
        }

        /**
         * Gets the utilization mips.
         *
         * @return the utilization mips
         */
        public double getUtilizationMips() {
            return utilizationMips;
        }

        /**
         * Sets the utilization mips.
         *
         * @param utilizationMips the new utilization mips
         */
        protected void setUtilizationMips(double utilizationMips) {
            this.utilizationMips = utilizationMips;
        }

        /**
         * Gets the previous utilization mips.
         *
         * @return the previous utilization mips
         */
        public double getPreviousUtilizationMips() {
            return previousUtilizationMips;
        }

        /**
         * Sets the previous utilization mips.
         *
         * @param previousUtilizationMips the new previous utilization mips
         */
        protected void setPreviousUtilizationMips(double previousUtilizationMips) {
            this.previousUtilizationMips = previousUtilizationMips;
        }

        /**
         * Gets the state history.
         *
         * @return the state history
         */
        public List<HostStateHistoryEntry> getStateHistory() {
            return stateHistory;
        }

        /**
         * Adds the state history entry.
         *
         * @param time the time
         * @param allocatedMips the allocated mips
         * @param requestedMips the requested mips
         * @param isActive the is active
         */
        public
        void
        addStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isActive) {

            HostStateHistoryEntry newState = new HostStateHistoryEntry(
                    time,
                    allocatedMips,
                    requestedMips,
                    isActive);
            if (!getStateHistory().isEmpty()) {
                HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
                if (previousState.getTime() == time) {
                    getStateHistory().set(getStateHistory().size() - 1, newState);
                    return;
                }
            }
            getStateHistory().add(newState);
        }

    }

