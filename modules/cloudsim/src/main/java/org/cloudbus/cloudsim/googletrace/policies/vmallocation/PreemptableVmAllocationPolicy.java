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
import org.cloudbus.cloudsim.googletrace.GoogleHost;
import org.cloudbus.cloudsim.googletrace.GoogleVm;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.googletrace.util.PriorityHostComparator;

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
	private Map<Integer, SortedSet<GoogleHost>> priorityToSortedHost;

	public PreemptableVmAllocationPolicy(List<GoogleHost> hosts, HostSelectionPolicy hostSelector) {
		super(new ArrayList<Host>(0));
		setHostSelector(hostSelector);
		
		priorityToSortedHost = new HashMap<Integer, SortedSet<GoogleHost>>();
		int numberOfPriorities = hosts.get(0).getNumberOfPriorities();

		for (int priority = 0; priority < numberOfPriorities; priority++) {

			PriorityHostComparator comparator = new PriorityHostComparator(priority);
			getPriorityToSortedHost().put(priority, new TreeSet<GoogleHost>(comparator));

		}
		
		// creating priority host skins
		for (GoogleHost host : hosts) {
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				getPriorityToSortedHost().get(priority).add(host);
			}
		}
		
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
		removePriorityHost(host);
		host.vmDestroy(vm);
		addPriorityHost(host);
		return true;
	}

	private void addPriorityHost(Host host) {
		GoogleHost gHost = (GoogleHost) host;
		for (int prioriry = 0; prioriry < gHost.getNumberOfPriorities(); prioriry++) {
			getPriorityToSortedHost().get(prioriry).add(gHost);
		}
	}

	private void removePriorityHost(Host host) {
		GoogleHost gHost = (GoogleHost) host;
		for (int prioriry = 0; prioriry < gHost.getNumberOfPriorities(); prioriry++) {
			getPriorityToSortedHost().get(prioriry).remove(gHost);
		}
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = selectHost(vm);
		if (host == null) {
			return false;
		}
		
		// just to update the sorted set
		removePriorityHost(host);
		boolean result = host.vmCreate(vm);
		addPriorityHost(host);
		
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
		removePriorityHost(host);
		boolean result = host.vmCreate(vm);
		addPriorityHost(host);
		
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
			// just to update the sorted set
			removePriorityHost(host);
			host.vmDestroy(vm);
			addPriorityHost(host);
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
		List<Host> hostList = new ArrayList<Host>();
		for (GoogleHost host : getPriorityToSortedHost().get(0)) {
			hostList.add(host);
		}
		return hostList;
	}
	
	public HostSelectionPolicy getHostSelector() {
		return hostSelector;
	}

	protected void setHostSelector(HostSelectionPolicy hostSelector) {
		this.hostSelector = hostSelector;
	}

	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	public Map<Integer, SortedSet<GoogleHost>> getPriorityToSortedHost() {
		return priorityToSortedHost;
	}
	
	public void setPriorityToSortedHost(
			Map<Integer, SortedSet<GoogleHost>> priorityToSortedHost) {
		this.priorityToSortedHost = priorityToSortedHost;
	}

	public Host selectHost(Vm vm) {
		GoogleVm gVm = (GoogleVm) vm;
		if (getPriorityToSortedHost().containsKey(gVm.getPriority())) {
			GoogleHost host = getHostSelector().select(getPriorityToSortedHost().get(gVm.getPriority()), vm);
			if (host != null) {
				return host;
			}
		}
		return null;
	}
}

