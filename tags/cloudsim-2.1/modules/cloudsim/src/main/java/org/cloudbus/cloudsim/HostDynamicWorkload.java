/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * The Class HostDynamicWorkload.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class HostDynamicWorkload extends Host {

	/** The utilization mips. */
	private double utilizationMips;

	/** The under allocated mips. */
	private Map<String, List<List<Double>>> underAllocatedMips;

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
	public HostDynamicWorkload(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setUtilizationMips(0);
		setUnderAllocatedMips(new HashMap<String, List<List<Double>>>());
		setVmsMigratingIn(new ArrayList<Vm>());
	}

	/* (non-Javadoc)
	 * @see cloudsim.Host#updateVmsProcessing(double)
	 */
	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = super.updateVmsProcessing(currentTime);

		setUtilizationMips(0);

		for (Vm vm : getVmList()) {
			getVmScheduler().deallocatePesForVm(vm);
		}

		for (Vm vm : getVmList()) {
			getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
		}

		for (Vm vm : getVmList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();

			if (totalRequestedMips == 0) {
				Log.printLine("VM #" + vm.getId() + " has completed its execution and destroyed");
				continue;
			}

			double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

			if (totalAllocatedMips + 0.1 < totalRequestedMips) {
				Log.printLine("Under allocated MIPS for VM #" + vm.getId() + ": requested " + totalRequestedMips + ", allocated " + totalAllocatedMips);
			}

			updateUnderAllocatedMips(vm, totalRequestedMips, totalAllocatedMips);

			Log.formatLine("%.2f: Total allocated MIPS for VM #" + vm.getId() + " (Host #" + vm.getHost().getId() + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)", CloudSim.clock(), totalAllocatedMips, totalRequestedMips, vm.getMips(), totalRequestedMips / vm.getMips() * 100);

			if (vm.isInMigration()) {
				Log.printLine("VM #" + vm.getId() + " is in migration");
				totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
			}

			setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
		}

		return smallerTime;
	}

	/**
	 * Gets the completed vms.
	 *
	 * @return the completed vms
	 */
	public List<Vm> getCompletedVms() {
		List<Vm> vmsToRemove = new ArrayList<Vm>();
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				continue;
			}
			if (vm.getCurrentRequestedTotalMips() == 0) {
				vmsToRemove.add(vm);
			}
		}
		return vmsToRemove;
	}

	/**
	 * Gets the max utilization among by all PEs.
	 *
	 * @return the utilization
	 */
	@SuppressWarnings("unchecked")
	public double getMaxUtilization() {
		return PeList.getMaxUtilization((List<Pe>) getPeList());
	}

	/**
	 * Gets the max utilization among by all PEs
	 * allocated to the VM.
	 *
	 * @param vm the vm
	 *
	 * @return the utilization
	 */
	@SuppressWarnings("unchecked")
	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes((List<Pe>) getPeList(), vm);
	}

	/**
	 * Gets the utilization of memory.
	 *
	 * @return the utilization of memory
	 */
	public double getUtilizationOfRam() {
		return getRamProvisioner().getUsedRam();
	}

	/**
	 * Gets the utilization of bw.
	 *
	 * @return the utilization of bw
	 */
	public double getUtilizationOfBw() {
		return getBwProvisioner().getUsedBw();
	}

	/**
	 * Update under allocated mips.
	 *
	 * @param vm the vm
	 * @param requested the requested
	 * @param allocated the allocated
	 */
	protected void updateUnderAllocatedMips(Vm vm, double requested, double allocated) {
		List<List<Double>> underAllocatedMipsArray;
		List<Double> underAllocatedMips = new ArrayList<Double>();
		underAllocatedMips.add(requested);
		underAllocatedMips.add(allocated);

		if (getUnderAllocatedMips().containsKey(vm.getUid())) {
			underAllocatedMipsArray = getUnderAllocatedMips().get(vm.getUid());
		} else {
			underAllocatedMipsArray = new ArrayList<List<Double>>();
		}

		underAllocatedMipsArray.add(underAllocatedMips);
		getUnderAllocatedMips().put(vm.getUid(), underAllocatedMipsArray);
	}

	/**
	 * Get current utilization of CPU in percents.
	 *
	 * @return current utilization of CPU in percents
	 */
	public double getUtilizationOfCpu() {
		return getUtilizationMips() / getTotalMips();
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
	protected double getUtilizationMips() {
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
     * Gets the under allocated mips.
     *
     * @return the under allocated mips
     */
    public Map<String, List<List<Double>>> getUnderAllocatedMips() {
		return underAllocatedMips;
	}

	/**
	 * Sets the under allocated mips.
	 *
	 * @param underAllocatedMips the under allocated mips
	 */
	protected void setUnderAllocatedMips(Map<String, List<List<Double>>> underAllocatedMips) {
		this.underAllocatedMips = underAllocatedMips;
	}

}
