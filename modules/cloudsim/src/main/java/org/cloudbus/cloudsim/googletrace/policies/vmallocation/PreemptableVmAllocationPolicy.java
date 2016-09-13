package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.googletrace.GoogleVm;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;

/**
 * 
 * @author Giovanni Farias
 *
 */
public class PreemptableVmAllocationPolicy extends VmAllocationPolicy implements
		Preemptable {

	/**
	 * The map between each VM and its allocated host. The map key is a VM UID
	 * and the value is the allocated host for that VM.
	 */
	private Map<String, Host> vmTable;

	private HostSelectionPolicy hostSelector;
	private SortedSet<Host> sortedHosts;

	public PreemptableVmAllocationPolicy(List<? extends Host> hosts, HostSelectionPolicy hostSelector) {
		super(new ArrayList<Host>(0));
		setHostSelector(hostSelector);
		setSortedHosts(new TreeSet<Host>(hosts));
		setVmTable(new HashMap<String, Host>());
	}

	@Override
	public boolean preempt(GoogleVm vm) {
		Host host = getVmTable().remove(vm.getUid());
		if (host == null) {
			return false;
		}
		vm.preempt(CloudSim.clock());
		// just to update the sorted set
		getSortedHosts().remove(host);
		host.vmDestroy(vm);
		getSortedHosts().add(host);
		return true;
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = selectHost(vm);
		if (host == null) {
			return false;
		}
		
		// just to update the sorted set
		getSortedHosts().remove(host);
		boolean result = host.vmCreate(vm);
		getSortedHosts().add(host);
		
		if (result) {
			getVmTable().put(vm.getUid(), host);
		}
		return result;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host == null) {
			return false;
		}
		// just to update the sorted set
		getSortedHosts().remove(host);
		boolean result = host.vmCreate(vm);
		getSortedHosts().add(host);
		
		if (result) {
			getVmTable().put(vm.getUid(), host);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			// just to update the sorted set
			getSortedHosts().remove(host);
			getSortedHosts().add(host);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	@Override
	public List<Host> getHostList() {
		return new ArrayList<Host>(sortedHosts);
	}
	
	public HostSelectionPolicy getHostSelector() {
		return hostSelector;
	}

	protected void setHostSelector(HostSelectionPolicy hostSelector) {
		this.hostSelector = hostSelector;
	}

	public SortedSet<Host> getSortedHosts() {
		return sortedHosts;
	}

	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	protected void setSortedHosts(SortedSet<Host> sortedHosts) {
		this.sortedHosts = sortedHosts;
	}

	public Host selectHost(Vm vm) {
		return getHostSelector().select(getSortedHosts(), vm);
	}
}
