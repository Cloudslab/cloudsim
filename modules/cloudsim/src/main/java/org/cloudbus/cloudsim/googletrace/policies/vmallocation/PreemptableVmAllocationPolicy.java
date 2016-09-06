package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;

public class PreemptableVmAllocationPolicy extends VmAllocationPolicy implements
		Preemptable {

	private HostSelectionPolicy hostSelector;
	private SortedSet<Host> sortedHosts;
	
	public PreemptableVmAllocationPolicy(List<? extends Host> hosts, HostSelectionPolicy hostSelector) {
		super(new ArrayList<Host>());
		sortedHosts = new TreeSet<Host>(hosts);
	}

	@Override
	public void preempt(Vm vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = hostSelector.select(getSortedHosts(), vm);
		if (host == null) {
			return false;
		}
		return host.vmCreate(vm);
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host == null) {
			return false;
		}
		return host.vmCreate(vm);
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public Host getHost(Vm vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Host getHost(int vmId, int userId) {
		// TODO Auto-generated method stub
		return null;
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
	
}
