/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * CloudletSchedulerDynamicWorkload implements a policy of scheduling performed by a virtual machine
 * to run its {@link Cloudlet Cloudlets}, 
 * assuming there is just one cloudlet which is working as an online service.
 * It extends a TimeShared policy, but in fact, considering that there is just
 * one cloudlet for the VM using this scheduler, the cloudlet will not
 * compete for CPU with other ones.
 * Each VM has to have its own instance of a CloudletScheduler.
 * 
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 2.0
 * CloudletSchedulerSingleService as its Test Suite
 */
//@TODO The name of the class doesn't represent its goal. A clearer name would be
public class CloudletSchedulerDynamicWorkload extends CloudletSchedulerTimeShared {

	/** The individual MIPS capacity of each PE allocated to the VM using the scheduler,
         * considering that all PEs have the same capacity. 
         * //@TODO Despite of the class considers that all PEs have the same capacity,
         * it accepts a list of PEs with different MIPS at the method 
         * {@link CloudletScheduler#updateCloudletsProcessing(double, List) }
         */
	private double mips;

	/** The number of PEs allocated to the VM using the scheduler. */
	private int numberOfPes;

	/** The total MIPS considering all PEs. */
	private double totalMips;

	/** The under allocated MIPS. */
	private Map<String, Double> underAllocatedMips;

	/** The cache of the previous time when the {@link #getCurrentRequestedMips()} was called. */
	private double cachePreviousTime;

	/** The cache of the last current requested MIPS. 
         * @see  #getCurrentRequestedMips() 
         */
	private List<Double> cacheCurrentRequestedMips;

	/**
	 * Instantiates a new VM scheduler
	 * 
	 * @param mips The individual MIPS capacity of each PE allocated to the VM using the scheduler,
         * considering that all PEs have the same capacity.
	 * @param numberOfPes The number of PEs allocated to the VM using the scheduler.
	 */
	public CloudletSchedulerDynamicWorkload(double mips, int numberOfPes) {
		super();
		setMips(mips);
		setNumberOfPes(numberOfPes);
                /*//@TODO There shouldn't be a setter to total mips, considering
                that it is computed from number of PEs and mips.
                If the number of pes of mips is set any time after here,
                the total mips will be wrong. Just the getTotalMips is enough,
                and it have to compute there the total, instead of storing into an attribute.*/
		setTotalMips(getNumberOfPes() * getMips());
		setUnderAllocatedMips(new HashMap<>());
		setCachePreviousTime(-1);
	}

	@Override
	public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);

		double timeSpan = currentTime - getPreviousTime();
		double nextEvent = Double.MAX_VALUE;
		List<Cloudlet> cloudletsToFinish = new ArrayList<>();

		for (Cloudlet cl : getCloudletExecList()) {
			cl.updateCloudletFinishedSoFar((long) (timeSpan *
					getTotalCurrentAllocatedMipsForCloudlet(cl, getPreviousTime()) * Consts.MILLION));
			cl.updateCloudlet(null);

			if (cl.getRemainingCloudletLength() == 0) { // finished: remove from the list
				cloudletsToFinish.add(cl);
			} else { // not finish: estimate the finish time
				double estimatedFinishTime = getEstimatedFinishTime(cl, currentTime);
				if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
					estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
				}
				if (estimatedFinishTime < nextEvent) {
					nextEvent = estimatedFinishTime;
				}
			}
		}

		for (Cloudlet cl : cloudletsToFinish) {
			getCloudletExecList().remove(cl);
			cloudletFinish(cl);
		}

		setPreviousTime(currentTime);

		if (getCloudletExecList().isEmpty()) {
			return 0;
		}

		return nextEvent;
	}

	@Override
	public double cloudletSubmit(Cloudlet cl, double fileTransferTime) {
		cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);

		getCloudletExecList().add(cl);
		return getEstimatedFinishTime(cl, getPreviousTime());
	}



	@Override
	public List<Double> getCurrentRequestedMips() {
		if (getCachePreviousTime() == getPreviousTime()) {
			return getCacheCurrentRequestedMips();
		}
		List<Double> currentMips = new ArrayList<>();
		double totalMips = getTotalUtilizationOfCpu(getPreviousTime()) * getTotalMips();
		double mipsForPe = totalMips / getNumberOfPes();

		for (int i = 0; i < getNumberOfPes(); i++) {
			currentMips.add(mipsForPe);
		}

		setCachePreviousTime(getPreviousTime());
		setCacheCurrentRequestedMips(currentMips);

		return currentMips;
	}

	@Override
	public double getCurrentRequestedTotalMips() {
		List<Double> currentMips = getCurrentRequestedMips();
		double mips = 0.0;
		for (double v : currentMips)
			mips += v;
		return mips;
	}

	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(Cloudlet cl, double time) {
		return cl.getUtilizationOfCpu(time) * getTotalMips();
	}

	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(Cloudlet cl, List<Double> mipsShare) {
		double totalCurrentMips = 0.0;
		if (mipsShare != null) {
			int neededPEs = cl.getNumberOfPes();
			for (double mips : mipsShare) {
				totalCurrentMips += mips;
				neededPEs--;
				if (neededPEs <= 0) {
					break;
				}
			}
		}
		return totalCurrentMips;
	}

	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(Cloudlet cl, double time) {
		double totalCurrentRequestedMips = getTotalCurrentRequestedMipsForCloudlet(cl, time);
		double totalCurrentAvailableMips = getTotalCurrentAvailableMipsForCloudlet(cl, getCurrentMipsShare());
		return Math.min(totalCurrentRequestedMips, totalCurrentAvailableMips);
	}

	/**
	 * Update under allocated mips for cloudlet.
	 * 
	 * @param cl the cloudlet
	 * @param mips the mips
         * //@TODO It is not clear the goal of this method. The related test case
         * doesn't make it clear too. The method doesn't appear to be used anywhere.
	 */
	public void updateUnderAllocatedMipsForCloudlet(Cloudlet cl, double mips) {
		if (getUnderAllocatedMips().containsKey(cl.getUid())) {
			mips += getUnderAllocatedMips().get(cl.getUid());
		}
		getUnderAllocatedMips().put(cl.getUid(), mips);
	}

	/**
	 * Get the estimated completion time of a given cloudlet.
	 * 
	 * @param cl the cloudlet
	 * @param time the time
	 * @return the estimated finish time
	 */
	@Override
	public double getEstimatedFinishTime(Cloudlet cl, double time) {
		return time
				+ ((cl.getRemainingCloudletLength()) / getTotalCurrentAllocatedMipsForCloudlet(cl, time));
	}

	/**
	 * Gets the total current mips available for the VM using the scheduler.
         * The total is computed from the {@link #getCurrentMipsShare()}
	 * 
	 * @return the total current mips
	 */
	public double getTotalCurrentMips() {
		double totalCurrentMips = 0;
		for (double mips : getCurrentMipsShare()) {
			totalCurrentMips += mips;
		}
		return totalCurrentMips;
	}

	/**
	 * Sets the total mips.
	 * 
	 * @param mips the new total mips
	 */
	public void setTotalMips(double mips) {
		totalMips = mips;
	}

	/**
	 * Gets the total mips.
	 * 
	 * @return the total mips
	 */
	public double getTotalMips() {
		return totalMips;
	}

	/**
	 * Sets the pes number.
	 * 
	 * @param pesNumber the new pes number
	 */
	public void setNumberOfPes(int pesNumber) {
		numberOfPes = pesNumber;
	}

	/**
	 * Gets the pes number.
	 * 
	 * @return the pes number
	 */
	public int getNumberOfPes() {
		return numberOfPes;
	}

	/**
	 * Sets the mips.
	 * 
	 * @param mips the new mips
	 */
	public void setMips(double mips) {
		this.mips = mips;
	}

	/**
	 * Gets the mips.
	 * 
	 * @return the mips
	 */
	public double getMips() {
		return mips;
	}

	/**
	 * Sets the under allocated mips.
	 * 
	 * @param underAllocatedMips the under allocated mips
	 */
	public void setUnderAllocatedMips(Map<String, Double> underAllocatedMips) {
		this.underAllocatedMips = underAllocatedMips;
	}

	/**
	 * Gets the under allocated mips.
	 * 
	 * @return the under allocated mips
	 */
	public Map<String, Double> getUnderAllocatedMips() {
		return underAllocatedMips;
	}

	/**
	 * Gets the cache of previous time.
	 * 
	 * @return the cache previous time
	 */
	protected double getCachePreviousTime() {
		return cachePreviousTime;
	}

	/**
	 * Sets the cache of previous time.
	 * 
	 * @param cachePreviousTime the new cache previous time
	 */
	protected void setCachePreviousTime(double cachePreviousTime) {
		this.cachePreviousTime = cachePreviousTime;
	}

	/**
	 * Gets the cache of current requested mips.
	 * 
	 * @return the cache current requested mips
	 */
	protected List<Double> getCacheCurrentRequestedMips() {
		return cacheCurrentRequestedMips;
	}

	/**
	 * Sets the cache of current requested mips.
	 * 
	 * @param cacheCurrentRequestedMips the new cache current requested mips
	 */
	protected void setCacheCurrentRequestedMips(List<Double> cacheCurrentRequestedMips) {
		this.cacheCurrentRequestedMips = cacheCurrentRequestedMips;
	}

}
