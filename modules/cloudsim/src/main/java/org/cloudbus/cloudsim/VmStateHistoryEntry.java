/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2011, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

/**
 * Stores historic data about a VM.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.1.2
 */
public class VmStateHistoryEntry {

	/** The time. */
	private double time;

	/** The allocated mips. */
	private double allocatedMips;

	/** The requested mips. */
	private double requestedMips;

	/** The is in migration. */
	private boolean isInMigration;

	/**
	 * Instantiates a new VmStateHistoryEntry
	 * 
	 * @param time the time
	 * @param allocatedMips the allocated mips
	 * @param requestedMips the requested mips
	 * @param isInMigration the is in migration
	 */
	public VmStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isInMigration) {
		setTime(time);
		setAllocatedMips(allocatedMips);
		setRequestedMips(requestedMips);
		setInMigration(isInMigration);
	}

	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	protected void setTime(double time) {
		this.time = time;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Sets the allocated mips.
	 * 
	 * @param allocatedMips the new allocated mips
	 */
	protected void setAllocatedMips(double allocatedMips) {
		this.allocatedMips = allocatedMips;
	}

	/**
	 * Gets the allocated mips.
	 * 
	 * @return the allocated mips
	 */
	public double getAllocatedMips() {
		return allocatedMips;
	}

	/**
	 * Sets the requested mips.
	 * 
	 * @param requestedMips the new requested mips
	 */
	protected void setRequestedMips(double requestedMips) {
		this.requestedMips = requestedMips;
	}

	/**
	 * Gets the requested mips.
	 * 
	 * @return the requested mips
	 */
	public double getRequestedMips() {
		return requestedMips;
	}

	/**
	 * Sets the in migration.
	 * 
	 * @param isInMigration the new in migration
	 */
	protected void setInMigration(boolean isInMigration) {
		this.isInMigration = isInMigration;
	}

	/**
	 * Checks if is in migration.
	 * 
	 * @return true, if is in migration
	 */
	public boolean isInMigration() {
		return isInMigration;
	}

}
