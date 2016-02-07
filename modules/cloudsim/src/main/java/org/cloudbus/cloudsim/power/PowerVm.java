/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.MathUtil;

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
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerVm extends Vm {

	/** The Constant HISTORY_LENGTH. */
	public static final int HISTORY_LENGTH = 30;

	/** The CPU utilization percentage history. */
	private final List<Double> utilizationHistory = new LinkedList<Double>();

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
	public double updateVmProcessing(final double currentTime, final List<Double> mipsShare) {
		double time = super.updateVmProcessing(currentTime, mipsShare);
		if (currentTime > getPreviousTime() && (currentTime - 0.1) % getSchedulingInterval() == 0) {
			double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
			if (CloudSim.clock() != 0 || utilization != 0) {
				addUtilizationHistoryValue(utilization);
			}
			setPreviousTime(currentTime);
		}
		return time;
	}

	/**
	 * Gets the utilization MAD in MIPS.
	 * 
	 * @return the utilization MAD in MIPS
	 */
	public double getUtilizationMad() {
		double mad = 0;
		if (!getUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getUtilizationHistory().size()) {
				n = getUtilizationHistory().size();
			}
			double median = MathUtil.median(getUtilizationHistory());
			double[] deviationSum = new double[n];
			for (int i = 0; i < n; i++) {
				deviationSum[i] = Math.abs(median - getUtilizationHistory().get(i));
			}
			mad = MathUtil.median(deviationSum);
		}
		return mad;
	}

	/**
	 * Gets the utilization mean in percents.
	 * 
	 * @return the utilization mean in MIPS
	 */
	public double getUtilizationMean() {
		double mean = 0;
		if (!getUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getUtilizationHistory().size()) {
				n = getUtilizationHistory().size();
			}
			for (int i = 0; i < n; i++) {
				mean += getUtilizationHistory().get(i);
			}
			mean /= n;
		}
		return mean * getMips();
	}

	/**
	 * Gets the utilization variance in MIPS.
	 * 
	 * @return the utilization variance in MIPS
	 */
	public double getUtilizationVariance() {
		double mean = getUtilizationMean();
		double variance = 0;
		if (!getUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getUtilizationHistory().size()) {
				n = getUtilizationHistory().size();
			}
			for (int i = 0; i < n; i++) {
				double tmp = getUtilizationHistory().get(i) * getMips() - mean;
				variance += tmp * tmp;
			}
			variance /= n;
		}
		return variance;
	}

	/**
	 * Adds a CPU utilization percentage history value.
	 * 
	 * @param utilization the CPU utilization percentage to add
	 */
	public void addUtilizationHistoryValue(final double utilization) {
		getUtilizationHistory().add(0, utilization);
		if (getUtilizationHistory().size() > HISTORY_LENGTH) {
			getUtilizationHistory().remove(HISTORY_LENGTH);
		}
	}

	/**
	 * Gets the CPU utilization percentage history.
	 * 
	 * @return the CPU utilization percentage history
	 */
	protected List<Double> getUtilizationHistory() {
		return utilizationHistory;
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
