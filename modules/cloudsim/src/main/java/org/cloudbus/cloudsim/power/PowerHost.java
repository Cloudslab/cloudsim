package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.lists.PowerPeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * The Class PowerHost.
 */
public class PowerHost extends HostDynamicWorkload {

	/**
	 * Instantiates a new host.
	 *
	 * @param id the id
	 * @param cpuProvisioner the cpu provisioner
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param vmScheduler the VM scheduler
	 */
	public PowerHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 *
	 * @return the power
	 */
	@SuppressWarnings("unchecked")
	public double getPower() {
		return PowerPeList.getPower((List<PowerPe>) getPeList());
	}

	/**
	 * Gets the maximum power. For this moment only consumed by all PEs.
	 *
	 * @return the power
	 */
	@SuppressWarnings("unchecked")
	public double getMaxPower() {
    	return PowerPeList.getMaxPower((List<PowerPe>) getPeList());
	}

}
