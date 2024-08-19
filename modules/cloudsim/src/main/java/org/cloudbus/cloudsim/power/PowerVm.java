/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.util.HistoryStat;

/**
 * A class of VM that stores its CPU utilization percentage history. The history is used by VM allocation
 * and selection policies.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley &amp; Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerVm extends Vm implements PowerGuestEntity {
	/** The CPU utilization percentage history. */
	private final List<Double> utilizationHistory = new ArrayList<>();
	private final HistoryStat utilizationHistoryStat = new HistoryStat(HISTORY_LENGTH);

	/** The previous time that cloudlets were processed. */
	private double previousTime;

	/** The scheduling interval to update the processing of cloudlets
         * running in this VM. */
	private double schedulingInterval;

	/**
	 * Instantiates a new PowerVm.
	 * 
	 * @param id the id
	 * @param userId the user id
	 * @param mips the mips
	 * @param pesNumber the pes number
	 * @param ram the ram
	 * @param bw the bw
	 * @param size the size
	 * @param priority the priority
	 * @param vmm the vmm
	 * @param cloudletScheduler the cloudlet scheduler
	 * @param schedulingInterval the scheduling interval
	 */
	public PowerVm(
			final int id,
			final int userId,
			final double mips,
			final int pesNumber,
			final int ram,
			final long bw,
			final long size,
			final int priority,
			final String vmm,
			final CloudletScheduler cloudletScheduler,
			final double schedulingInterval) {
		super(id, userId, mips, pesNumber, ram, bw, size, vmm, cloudletScheduler);
		setSchedulingInterval(schedulingInterval);
	}

	@Override
	public double updateCloudletsProcessing(final double currentTime, final List<Double> mipsShare) {
		double time = super.updateCloudletsProcessing(currentTime, mipsShare);
		if (currentTime - getPreviousTime() >= getSchedulingInterval()) {
			double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
			if (CloudSim.clock() != 0 || utilization != 0) {
				addUtilizationHistoryValue(utilization);
			}
			setPreviousTime(currentTime);
		}
		return time;
	}

	/**
	 * Gets the CPU utilization percentage history.
	 * 
	 * @return the CPU utilization percentage history
	 */
	public HistoryStat getUtilizationHistory() {
		return utilizationHistoryStat;
	}

	/**
	 * Gets the previous time.
	 * 
	 * @return the previous time
	 */
	public double getPreviousTime() {
		return previousTime;
	}

	/**
	 * Sets the previous time.
	 * 
	 * @param previousTime the new previous time
	 */
	public void setPreviousTime(final double previousTime) {
		this.previousTime = previousTime;
	}

	/**
	 * Gets the scheduling interval.
	 * 
	 * @return the schedulingInterval
	 */
	public double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the scheduling interval.
	 * 
	 * @param schedulingInterval the schedulingInterval to set
	 */
	protected void setSchedulingInterval(final double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}
}
