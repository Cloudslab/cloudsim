/**
 * 
 */
package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sareh
 *
 */
public class ContainerAllocationPolicySimple extends ContainerAllocationPolicy {
	/** The vm table. */
	private Map<String, ContainerVm> containerVmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	/**
	 * Creates the new VmAllocationPolicySimple object.
	 *
	 * @pre $none
	 * @post $none
	 */
	public ContainerAllocationPolicySimple() {
		super();
		setFreePes(new ArrayList<Integer>());
		setContainerVmTable(new HashMap<String, ContainerVm>());
		setUsedPes(new HashMap<String, Integer>());
	}


	@Override
	public boolean allocateVmForContainer(Container container, List<ContainerVm> containerVmList) {
//		the available container list is updated. It gets is from the data center.
		setContainerVmList(containerVmList);
		for (ContainerVm containerVm : getContainerVmList()) {
			getFreePes().add(containerVm.getNumberOfPes());

		}
		int requiredPes = container.getNumberOfPes();
		boolean result = false;
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		if (!getContainerVmTable().containsKey(container.getUid())) { // if this vm was not created
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

				ContainerVm containerVm = getContainerVmList().get(idx);
				result = containerVm.containerCreate(container);

				if (result) { // if vm were succesfully created in the host
					getContainerVmTable().put(container.getUid(), containerVm);
					getUsedPes().put(container.getUid(), requiredPes);
					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					result = true;
					break;
				} else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());

		}

		freePesTmp.clear();

		return result;
	}

	@Override
	public boolean allocateVmForContainer(Container container, ContainerVm containerVm) {
		if (containerVm.containerCreate(container)) { // if vm has been succesfully created in the host
			getContainerVmTable().put(container.getUid(), containerVm);

			int requiredPes = container.getNumberOfPes();
			int idx = getContainerVmList().indexOf(container);
			getUsedPes().put(container.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

			Log.formatLine(
					"%.2f: Container #" + container.getId() + " has been allocated to the Vm #" + containerVm.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}


	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Container> containerList) {
		return null;
	}

	@Override
	public void deallocateVmForContainer(Container container) {

		ContainerVm containerVm = getContainerVmTable().remove(container.getUid());
		int idx = getContainerVmList().indexOf(containerVm);
		int pes = getUsedPes().remove(container.getUid());
		if (containerVm != null) {
			containerVm.containerDestroy(container);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}

	}

	@Override
	public ContainerVm getContainerVm(Container container) {
		return getContainerVmTable().get(container.getUid());
	}

	@Override
	public ContainerVm getContainerVm(int containerId, int userId) {
		return getContainerVmTable().get(Container.getUid(userId, containerId));
	}

	protected Map<String, ContainerVm> getContainerVmTable() {
		return containerVmTable;
	}

	protected void setContainerVmTable(Map<String, ContainerVm> containerVmTable) {
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
