/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

/**
 * Implements a power model where the power consumption is the square root of the resource usage.
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
public class PowerModelSqrt implements PowerModel {
	/** The max power that can be consumed. */
	private double maxPower;

	/** The constant that represents the power consumption
         * for each fraction of resource used. */
	private double constant;

	/** The static power consumption that is not dependent of resource usage. 
         * It is the amount of energy consumed even when the host is idle.
         */
	private double staticPower;

	/**
	 * Instantiates a new power model sqrt.
	 * 
	 * @param maxPower the max power
	 * @param staticPowerPercent the static power percent
	 */
	public PowerModelSqrt(double maxPower, double staticPowerPercent) {
		setMaxPower(maxPower);
		setStaticPower(staticPowerPercent * maxPower);
		setConstant((maxPower - getStaticPower()) / Math.sqrt(100));
	}

	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		if (utilization == 0) {
			return 0;
		}
		return getStaticPower() + getConstant() * Math.sqrt(utilization * 100);
	}

	/**
	 * Gets the max power.
	 * 
	 * @return the max power
	 */
	protected double getMaxPower() {
		return maxPower;
	}

	/**
	 * Sets the max power.
	 * 
	 * @param maxPower the new max power
	 */
	protected void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	/**
	 * Gets the constant.
	 * 
	 * @return the constant
	 */
	protected double getConstant() {
		return constant;
	}

	/**
	 * Sets the constant.
	 * 
	 * @param constant the new constant
	 */
	protected void setConstant(double constant) {
		this.constant = constant;
	}

	/**
	 * Gets the static power.
	 * 
	 * @return the static power
	 */
	protected double getStaticPower() {
		return staticPower;
	}

	/**
	 * Sets the static power.
	 * 
	 * @param staticPower the new static power
	 */
	protected void setStaticPower(double staticPower) {
		this.staticPower = staticPower;
	}

}
