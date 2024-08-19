/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.Arrays;
import java.util.List;

import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.PowerHostEntity;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * PowerHost class enables simulation of power-aware hosts.
 * It stores its CPU utilization percentage history. The history is used by VM allocation
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
 * @author Remo Andreoli
 * @since CloudSim Toolkit 2.0
 */
public class PowerHost extends HostDynamicWorkload implements PowerHostEntity {

	/** The power model used by the host. */
	private PowerModel powerModel;

	/**
	 * Instantiates a new PowerHost.
	 * 
	 * @param id the id of the host
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage capacity
	 * @param peList the host's PEs list
	 * @param vmScheduler the vm scheduler
	 * @param powerModel the power consumption model
	 */
	public PowerHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setPowerModel(powerModel);
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return getPower(getUtilizationOfCpu());
	}

	/**
	 * Gets the current power consumption of the host. For this moment only consumed by all PEs.
	 * 
	 * @param utilization the utilization percentage (between [0 and 1]) of a resource that
         * is critical for power consumption
	 * @return the power consumption
	 */
	public double getPower(double utilization) {
		double power = 0;
		try {
			power = getPowerModel().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Sets the power model.
	 * 
	 * @param powerModel the new power model
	 */
	public void setPowerModel(PowerModel powerModel) {
		this.powerModel = powerModel;
	}

	/**
	 * Gets the power model.
	 * 
	 * @return the power model
	 */
	public PowerModel getPowerModel() {
		return powerModel;
	}

	/**
	 * Gets the host CPU utilization percentage history.
	 *
	 * @return the host CPU utilization percentage history
	 */
	public double[] getUtilizationHistory() {
		double[] utilizationHistory = new double[PowerHostEntity.HISTORY_LENGTH];
		double hostMips = getTotalMips();
		int maxlen = 0;
		for (PowerVm vm : this.<PowerVm>getGuestList()) {
			double guestMips = vm.getMips();
			int i = 0;
			for (double u : vm.getUtilizationHistory()) {
				utilizationHistory[i++] += u * guestMips / hostMips;
			}
			if (i > maxlen)
				maxlen = i;
		}
		return Arrays.copyOf(utilizationHistory, maxlen);
	}
}
