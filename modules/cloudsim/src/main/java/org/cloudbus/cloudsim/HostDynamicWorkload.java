/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.VirtualEntity;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * A host supporting dynamic workloads and performance degradation.
 *
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 2.0
 */
public class HostDynamicWorkload extends Host {

	/** The utilization mips. */
	private double utilizationMips;

	/** The previous utilization mips. */
	private double previousUtilizationMips;

	/** The host utilization state history. */
	private final List<HostStateHistoryEntry> stateHistory = new LinkedList<>();

	/**
	 * Instantiates a new host.
	 * 
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage capacity
	 * @param peList the host's PEs list
	 * @param vmScheduler the VM scheduler
	 */
	public HostDynamicWorkload(
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

	@Override
	public double updateCloudletsProcessing(double currentTime) {
		double smallerTime = super.updateCloudletsProcessing(currentTime);
		setPreviousUtilizationMips(getUtilizationMips());
		setUtilizationMips(0);
		double hostTotalRequestedMips = 0;

		for (GuestEntity vm : getGuestList()) {
			getGuestScheduler().deallocatePesForGuest(vm);
		}

		for (GuestEntity vm : getGuestList()) {
			getGuestScheduler().allocatePesForGuest(vm, vm.getCurrentRequestedMips());
		}

		for (GuestEntity vm : getGuestList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getGuestScheduler().getTotalAllocatedMipsForGuest(vm);

			if (!Log.isDisabled()) {
				Log.printlnConcat(CloudSim.clock(),
						": [Host #", getId(), "] Total allocated MIPS for VM #", vm.getId()
								, " (Host #", vm.getHost().getId()
								, ") is ", totalAllocatedMips,", was requested ", totalRequestedMips
								, " out of total ",vm.getMips(), " (", totalRequestedMips / vm.getMips() * 100,"%.2f%%)");

				List<Pe> pes = getGuestScheduler().getPesAllocatedForGuest(vm);
				StringBuilder pesString = new StringBuilder();
				for (Pe pe : pes) {
					pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
							.getTotalAllocatedMipsForGuest(vm)));
				}
				Log.printlnConcat(CloudSim.clock(),
						": [Host #", getId(), "] MIPS for VM #", vm.getId(), " by PEs ("
								, getNumberOfPes(), " * ", getGuestScheduler().getPeCapacity() + ")."
								, pesString);
			}

			if (getGuestsMigratingIn().contains(vm)) {
				Log.printlnConcat(CloudSim.clock(), ": [Host #", getId(), "] VM #" + vm.getId()
						, " is being migrated to Host #", getId());
			} else {
				if (totalAllocatedMips + 0.1 < totalRequestedMips) {
					Log.printlnConcat(CloudSim.clock(), ": [Host #", getId(), "] Under allocated MIPS for VM #", vm.getId()
							, ": ", totalRequestedMips - totalAllocatedMips);
				}

				vm.addStateHistoryEntry(
						currentTime,
						totalAllocatedMips,
						totalRequestedMips,
						(vm.isInMigration() && !getGuestsMigratingIn().contains(vm)));

				if (vm.isInMigration()) {
					Log.printlnConcat(CloudSim.clock(),
							": [Host #", getId(), "] VM #", vm.getId(), " is in migration");
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
	public List<GuestEntity> getCompletedVms() {
		List<GuestEntity> vmsToRemove = new ArrayList<>();
		for (GuestEntity guest : getGuestList()) {
			if (guest.isInMigration()) {
				continue;
			}
			// If vm is in waiting state then don't kill it yet !!!!!!!!!
			if (guest instanceof VirtualEntity vm && vm.isInWaiting()) {
				continue;
			}

			if (guest.getCurrentRequestedTotalMips() == 0) {
				vmsToRemove.add(guest);
			}

			if (guest instanceof VirtualEntity vm && vm.getNumberOfGuests()==0) {
				vmsToRemove.add(vm);
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

		for (GuestEntity guest : getGuestList()) {
			if (guest instanceof VirtualEntity vm) {
				numberofContainers += vm.getNumberOfGuests();
				Log.print("The number of containers in VM# " + vm.getId() + "is: " + vm.getNumberOfGuests());
				Log.println();
			}
		}
		return numberofContainers;
	}

	/**
	 * Gets the max utilization percentage among by all PEs.
	 * 
	 * @return the maximum utilization percentage
	 */
	public double getMaxUtilization() {
		return PeList.getMaxUtilization(getPeList());
	}

	/**
	 * Gets the utilization of memory (in absolute values).
	 * 
	 * @return the utilization of memory
	 */
	public int getUtilizationOfRam() {
		return getGuestRamProvisioner().getUsedRam();
	}

	/**
	 * Gets the utilization of bw (in absolute values).
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
	 * @return the previous utilization of cpu in percents
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
         * //@TODO This method only calls the  {@link #getUtilizationMips()}.
         * getUtilizationMips may be deprecated and its code copied here.
	 */
	public double getUtilizationOfCpuMips() {
		return getUtilizationMips();
	}

	/**
	 * Gets the utilization of CPU in MIPS.
	 * 
	 * @return current utilization of CPU in MIPS
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
	 * Gets the previous utilization of CPU in mips.
	 * 
	 * @return the previous utilization of CPU in mips
	 */
	public double getPreviousUtilizationMips() {
		return previousUtilizationMips;
	}

	/**
	 * Sets the previous utilization of CPU in mips.
	 * 
	 * @param previousUtilizationMips the new previous utilization of CPU in mips
	 */
	protected void setPreviousUtilizationMips(double previousUtilizationMips) {
		this.previousUtilizationMips = previousUtilizationMips;
	}

	/**
	 * Gets the host state history.
	 * 
	 * @return the state history
	 */
	public List<HostStateHistoryEntry> getStateHistory() {
		return stateHistory;
	}

	/**
	 * Adds a host state history entry.
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
			HostStateHistoryEntry previousState = getStateHistory().getLast();
			if (previousState.getTime() == time) {
				getStateHistory().set(getStateHistory().size() - 1, newState);
				return;
			}
		}
		getStateHistory().add(newState);
	}
}
