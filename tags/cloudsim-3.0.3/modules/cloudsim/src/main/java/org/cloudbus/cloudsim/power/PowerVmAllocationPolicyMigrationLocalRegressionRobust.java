/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * The Local Regression Robust (LRR) VM allocation policy.
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
public class PowerVmAllocationPolicyMigrationLocalRegressionRobust extends
		PowerVmAllocationPolicyMigrationLocalRegression {

	/**
	 * Instantiates a new power vm allocation policy migration local regression.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param schedulingInterval the scheduling interval
	 * @param fallbackVmAllocationPolicy the fallback vm allocation policy
	 * @param utilizationThreshold the utilization threshold
	 */
	public PowerVmAllocationPolicyMigrationLocalRegressionRobust(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter,
			double schedulingInterval,
			PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy,
			double utilizationThreshold) {
		super(
				hostList,
				vmSelectionPolicy,
				safetyParameter,
				schedulingInterval,
				fallbackVmAllocationPolicy,
				utilizationThreshold);
	}

	/**
	 * Instantiates a new power vm allocation policy migration local regression.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param schedulingInterval the scheduling interval
	 * @param fallbackVmAllocationPolicy the fallback vm allocation policy
	 */
	public PowerVmAllocationPolicyMigrationLocalRegressionRobust(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter,
			double schedulingInterval,
			PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
		super(hostList, vmSelectionPolicy, safetyParameter, schedulingInterval, fallbackVmAllocationPolicy);
	}

	/**
	 * Gets the parameter estimates.
	 * 
	 * @param utilizationHistoryReversed the utilization history reversed
	 * @return the parameter estimates
	 */
	@Override
	protected double[] getParameterEstimates(double[] utilizationHistoryReversed) {
		return MathUtil.getRobustLoessParameterEstimates(utilizationHistoryReversed);
	}

}
