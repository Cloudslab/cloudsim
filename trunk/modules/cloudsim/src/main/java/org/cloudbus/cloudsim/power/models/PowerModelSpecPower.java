/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

/**
 * The abstract class of power models created based on data from SPECpower benchmark:
 * http://www.spec.org/power_ssj2008/
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerModelSpecPower implements PowerModel {

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModel#getPower(double)
	 */
	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		if (utilization % 0.1 == 0) {
			return getPowerData((int) (utilization * 10));
		}
		int utilization1 = (int) Math.floor(utilization * 10);
		int utilization2 = (int) Math.ceil(utilization * 10);
		double power1 = getPowerData(utilization1);
		double power2 = getPowerData(utilization2);
		double delta = (power2 - power1) / 10;
		double power = power1 + delta * (utilization - (double) utilization1 / 10) * 100;
		return power;
	}

	/**
	 * Gets the power data.
	 * 
	 * @param index the index
	 * @return the power data
	 */
	protected abstract double getPowerData(int index);

}
