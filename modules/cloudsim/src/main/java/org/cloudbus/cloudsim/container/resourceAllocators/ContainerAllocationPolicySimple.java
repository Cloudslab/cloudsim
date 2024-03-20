/**
 * 
 */
package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sareh
 *  * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 *  * with less PEs in use. It is therefore a Worst Fit policy, allocating VMs into the
 *  * host with most available PE.
 */
public class ContainerAllocationPolicySimple extends ContainerAllocationPolicy {
	/** The vm table. */
	private Map<String, HostEntity> containerVmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;

	/**
	 * Creates a new ContainerAllocationPolicySimple object.
	 *
	 * @param list the list of hosts
	 * @pre $none
	 * @post $none
	 */
	public ContainerAllocationPolicySimple(List<? extends HostEntity> list) {
		super(list);
		setFreePes(new ArrayList<>());
		for (HostEntity host : getHostList()) {
			getFreePes().add(host.getNumberOfPes());
		}
		setContainerVmTable(new HashMap<>());
		setUsedPes(new HashMap<>());
	}


	@Override
	public boolean allocateHostForGuest(GuestEntity guest) {
		boolean result = false;
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<>(getFreePes());

		if (!getContainerVmTable().containsKey(guest.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) > moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}

				ContainerVm containerVm = getHostList().get(idx);
				if (allocateHostForGuest(guest, containerVm)) { // if vm were succesfully created in the host
					freePesTmp.clear();
					return true;
				} else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (tries < getFreePes().size());

		}

		freePesTmp.clear();
		return false;
	}

	@Override
	public boolean allocateHostForGuest(GuestEntity guest, HostEntity containerVm) {
		if (containerVm.guestCreate(guest)) { // if vm has been succesfully created in the host
			getContainerVmTable().put(guest.getUid(), containerVm);

			int requiredPes = guest.getNumberOfPes();
			int idx = getHostList().indexOf(containerVm);
			getUsedPes().put(guest.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

			Log.formatLine(
					"%.2f: Container #" + guest.getId() + " has been allocated to the Vm #" + containerVm.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> containerList) {
		return null;
	}

	@Override
	public void deallocateHostForGuest(GuestEntity guest) {

		HostEntity containerVm = getContainerVmTable().remove(guest.getUid());
		int idx = getHostList().indexOf(containerVm);
		int pes = getUsedPes().remove(guest.getUid());
		if (containerVm != null) {
			containerVm.guestDestroy(guest);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}
	}

	@Override
	public HostEntity getHost(GuestEntity guest) {
		return getContainerVmTable().get(guest.getUid());
	}

	@Override
	public HostEntity getHost(int containerId, int userId) {
		return getContainerVmTable().get(Container.getUid(userId, containerId));
	}

	protected Map<String, HostEntity> getContainerVmTable() {
		return containerVmTable;
	}

	protected void setContainerVmTable(Map<String, HostEntity> containerVmTable) {
		this.containerVmTable = containerVmTable;
	}

	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	protected List<Integer> getFreePes() {
		return freePes;
	}

	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}
}
