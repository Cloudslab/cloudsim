/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * The class of a host that stores its CPU utilization history. The history is used by VM allocation
 * and selection policies.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerHostUtilizationHistory extends PowerHost {

	/**
	 * Instantiates a new power host utilization history.
	 * 
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param peList the pe list
	 * @param vmScheduler the vm scheduler
	 * @param powerModel the power model
	 */
	public PowerHostUtilizationHistory(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
	}

	/**
	 * Gets the host utilization history.
	 * 
	 * @return the host utilization history
	 */
	protected double[] getUtilizationHistory() {
		double[] utilizationHistory = new double[PowerVm.HISTORY_LENGTH];
		double hostMips = getTotalMips();
		for (PowerVm vm : this.<PowerVm> getVmList()) {
			for (int i = 0; i < vm.getUtilizationHistory().size(); i++) {
				utilizationHistory[i] += vm.getUtilizationHistory().get(i) * vm.getMips() / hostMips;
			}
		}
		return MathUtil.trimZeroTail(utilizationHistory);
	}

}
