package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisioner;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.lists.ContainerVmPeList;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisioner;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmScheduler;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sareh on 14/07/15.
 */
public class ContainerHostDynamicWorkload extends ContainerHost{


        /** The utilization mips. */
        private double utilizationMips;

        /** The previous utilization mips. */
        private double previousUtilizationMips;

        /** The state history. */
        private final List<HostStateHistoryEntry> stateHistory = new LinkedList<HostStateHistoryEntry>();

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
                ContainerVmRamProvisioner ramProvisioner,
                ContainerVmBwProvisioner bwProvisioner,
                long storage,
                List<? extends ContainerVmPe> peList,
                ContainerVmScheduler vmScheduler) {
            super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
            setUtilizationMips(0);
            setPreviousUtilizationMips(0);
        }

        /*
         * (non-Javadoc)
         * @see cloudsim.Host#updateVmsProcessing(double)
         */
        @Override
        public double updateContainerVmsProcessing(double currentTime) {
            double smallerTime = super.updateContainerVmsProcessing(currentTime);
            setPreviousUtilizationMips(getUtilizationMips());
            setUtilizationMips(0);
            double hostTotalRequestedMips = 0;

            for (ContainerVm containerVm : getVmList()) {
                getContainerVmScheduler().deallocatePesForVm(containerVm);
            }

            for (ContainerVm  containerVm : getVmList()) {
                getContainerVmScheduler().allocatePesForVm(containerVm, containerVm.getCurrentRequestedMips());
            }

            for (ContainerVm  containerVm : getVmList()) {
                double totalRequestedMips = containerVm.getCurrentRequestedTotalMips();
                double totalAllocatedMips = getContainerVmScheduler().getTotalAllocatedMipsForContainerVm(containerVm);

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

                    List<ContainerVmPe> pes = getContainerVmScheduler().getPesAllocatedForContainerVM(containerVm);
                    StringBuilder pesString = new StringBuilder();
                    for (ContainerVmPe pe : pes) {
                        pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getContainerVmPeProvisioner()
                                .getTotalAllocatedMipsForContainerVm(containerVm)));
                    }
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] MIPS for VM #" + containerVm.getId() + " by PEs ("
                                    + getNumberOfPes() + " * " + getContainerVmScheduler().getPeCapacity() + ")."
                                    + pesString,
                            CloudSim.clock());
                }

                if (getVmsMigratingIn().contains(containerVm)) {
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
                            (containerVm.isInMigration() && !getVmsMigratingIn().contains(containerVm)));

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
        public List<ContainerVm> getCompletedVms() {
            List<ContainerVm> vmsToRemove = new ArrayList<>();
            for (ContainerVm containerVm : getVmList()) {
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
                if(containerVm.getNumberOfContainers()==0 ){
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
    public int getNumberofContainers() {
        int numberofContainers = 0;
        for (ContainerVm containerVm : getVmList()) {
            numberofContainers += containerVm.getNumberOfContainers();
            Log.print("The number of containers in VM# " + containerVm.getId()+"is: "+ containerVm.getNumberOfContainers());
            Log.printLine();
        }
        return numberofContainers;
    }




        /**
         * Gets the max utilization among by all PEs.
         *
         * @return the utilization
         */
        public double getMaxUtilization() {
            return ContainerVmPeList.getMaxUtilization(getPeList());
        }

        /**
         * Gets the max utilization among by all PEs allocated to the VM.
         *
         * @param vm the vm
         * @return the utilization
         */
        public double getMaxUtilizationAmongVmsPes(ContainerVm vm) {
            return ContainerVmPeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
        }

        /**
         * Gets the utilization of memory.
         *
         * @return the utilization of memory
         */
        public double getUtilizationOfRam() {
            return getContainerVmRamProvisioner().getUsedVmRam();
        }

        /**
         * Gets the utilization of bw.
         *
         * @return the utilization of bw
         */
        public double getUtilizationOfBw() {
            return getContainerVmBwProvisioner().getUsedBw();
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


