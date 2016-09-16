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
import org.cloudbus.cloudsim.googletrace.PriorityHostSkin;
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
	private Map<Integer, SortedSet<PriorityHostSkin>> priorityToSortedHostSkins;

	public PreemptableVmAllocationPolicy(List<GoogleHost> hosts, HostSelectionPolicy hostSelector) {
		super(new ArrayList<Host>(0));
		setHostSelector(hostSelector);
		
		priorityToSortedHostSkins = new HashMap<Integer, SortedSet<PriorityHostSkin>>();
		int numberOfPriorities = hosts.get(0).getNumberOfPriorities();
		for (int priority = 0; priority < numberOfPriorities; priority++) {
			getPriorityToSortedHostSkins().put(priority, new TreeSet<PriorityHostSkin>());
		}
		
		// creating priority host skins
		for (GoogleHost host : hosts) {
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				getPriorityToSortedHostSkins().get(priority).add(
						new PriorityHostSkin(host, priority));
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
		removePrioritySkins(host);
		host.vmDestroy(vm);
		addPrioritySkins(host);
		return true;
	}

	private void addPrioritySkins(Host host) {
		GoogleHost gHost = (GoogleHost) host;
		for (int prioriry = 0; prioriry < gHost.getNumberOfPriorities(); prioriry++) {
			PriorityHostSkin skin = new PriorityHostSkin(gHost, prioriry);			
			getPriorityToSortedHostSkins().get(prioriry).add(skin);
		}
	}

	private void removePrioritySkins(Host host) {
		GoogleHost gHost = (GoogleHost) host;
		for (int prioriry = 0; prioriry < gHost.getNumberOfPriorities(); prioriry++) {
			PriorityHostSkin skin = new PriorityHostSkin(gHost, prioriry);			
			getPriorityToSortedHostSkins().get(prioriry).remove(skin);
		}
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = selectHost(vm);
		if (host == null) {
			return false;
		}
		
		// just to update the sorted set
		removePrioritySkins(host);
		boolean result = host.vmCreate(vm);
		addPrioritySkins(host);
		
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
		removePrioritySkins(host);
		boolean result = host.vmCreate(vm);
		addPrioritySkins(host);
		
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
			removePrioritySkins(host);
			host.vmDestroy(vm);
			addPrioritySkins(host);
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
		for (PriorityHostSkin prioritySkin : getPriorityToSortedHostSkins().get(0)) {
			hostList.add(prioritySkin.getHost());
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

	public Map<Integer, SortedSet<PriorityHostSkin>> getPriorityToSortedHostSkins() {
		return priorityToSortedHostSkins;
	}
	
	public void setPriorityToSortedHostSkins(
			Map<Integer, SortedSet<PriorityHostSkin>> priorityToSortedHostSkins) {
		this.priorityToSortedHostSkins = priorityToSortedHostSkins;
	}

	public Host selectHost(Vm vm) {
		GoogleVm gVm = (GoogleVm) vm;
		if (getPriorityToSortedHostSkins().containsKey(gVm.getPriority())) {
			PriorityHostSkin hostSkin = getHostSelector().select(getPriorityToSortedHostSkins().get(gVm.getPriority()), vm);
			if (hostSkin != null) {
				return hostSkin.getHost();
			}
		}
		return null;
	}
}

