/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.googletrace.policies.vmallocation.PreemptableVmAllocationPolicy;

/**
 * TODO
 *  
 * @author Giovanni Farias
 * 
 */
public class GoogleDatacenter extends Datacenter {

	PreemptableVmAllocationPolicy vmAllocationPolicy;
	
	SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();
	
	private SortedSet<GoogleVm> vmsRunning = new TreeSet<GoogleVm>();
	private SortedSet<GoogleVm> vmsForScheduling = new TreeSet<GoogleVm>();	
	
	public GoogleDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			PreemptableVmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this Datacenter. This
	 * Datacenter will then send the status back to the User/Broker. It is important to note that
	 * the creation of VM does not have cost here. 
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		GoogleVm vm = (GoogleVm) ev.getData();

		allocateHostForVm(ack, vm);
	}

	protected void allocateHostForVm(boolean ack, GoogleVm vm) {
		GoogleHost host = (GoogleHost) getVmAllocationPolicy().selectHost(vm);	
		
		boolean result = tryingAllocateOnHost(vm, host);

		if (ack) {
			sendingAck(vm, result);
		}
		
		if (result) {
			getVmsRunning().add(vm);
			vm.setStartExec(simulationTimeUtil.clock());
			
			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}
			
			// We don't need to update the vm processing because there aren't cloudlets running in the vm
//			vm.updateVmProcessing(simulationTimeUtil.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
//					.getAllocatedMipsForVm(vm));
			
			getVmsForScheduling().remove(vm);
			
			double remainingTime = vm.getRuntime() - vm.getActualRuntime(simulationTimeUtil.clock());
			Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to destroy the VM #",
					vm.getId(), " in ", remainingTime, " microseconds.");
			send(getId(), remainingTime, CloudSimTags.VM_DESTROY_ACK, vm);			
			
		}
		

	}

	private boolean tryingAllocateOnHost(GoogleVm vm, GoogleHost host) {
		if (host == null) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There is not resource to allocate VM #" + vm.getId()
							+ " now, it will be tryed in the future.");
			if (!getVmsForScheduling().contains(vm)) {
				getVmsForScheduling().add(vm);
			}
			return false;
		}
		
		// trying to allocate
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);

		if (!result) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There is not resource to allocate VM #" + vm.getId()
							+ " right now.");
			
			GoogleVm vmToPreempt = (GoogleVm) host.nextVmForPreempting();
			if (vmToPreempt != null) {
				Log.printConcatLine(simulationTimeUtil.clock(),
						": Trying to preempt VM #" + vmToPreempt.getId()
								+ " (priority " + vmToPreempt.getPriority()
								+ ") to allocate VM #" + vm.getId()
								+ " (priority " + vm.getPriority() + ")");
				getVmAllocationPolicy().preempt(vmToPreempt);
				getVmsRunning().remove(vmToPreempt);
				getVmsForScheduling().add(vmToPreempt);
				return tryingAllocateOnHost(vm, host);
			} else if (!getVmsForScheduling().contains(vm)) {
				Log.printConcatLine(simulationTimeUtil.clock(),
						": There are not VMs to preempt. VM #" + vm.getId()
								+ " will be scheduled in the future.");
				getVmsForScheduling().add(vm);
			}
		}
		return result;
	}

	private void sendingAck(GoogleVm vm, boolean result) {
		int[] data = new int[3];
		data[0] = getId();
		data[1] = vm.getId();

		if (result) {
			data[2] = CloudSimTags.TRUE;
		} else {
			data[2] = CloudSimTags.FALSE;
		}
		send(vm.getUserId(), 0, CloudSimTags.VM_CREATE_ACK, data);
	}
	
	@Override
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		GoogleVm vm = (GoogleVm) ev.getData();
		
		if (vm.achievedRuntime(simulationTimeUtil.clock())) {
			if (getVmsRunning().remove(vm)) {		
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " will be terminated.");
				
				Host host = vm.getHost();
				getVmAllocationPolicy().deallocateHostForVm(vm);
				
				if (ack) {
					sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, vm);
				}
							
				if (!getVmsForScheduling().isEmpty()) {
					tryingToAllocateVms(host);
				}
			} else {
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " was terminated previously.");
			}
		} else {
			Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
					vm.getId(), " doesn't achieve the runtime yet.");
		}		
	}

	private void tryingToAllocateVms(Host host) {
		double availableMips = host.getAvailableMips();
		double mipsForRequestingNow = 0;
		List<GoogleVm> vmsToRequestNow = new ArrayList<GoogleVm>();
		
		// choosing the vms to request now
		for (GoogleVm currentVm : new ArrayList<GoogleVm>(getVmsForScheduling())) {
			if (mipsForRequestingNow + currentVm.getMips() <= availableMips) {
				mipsForRequestingNow += currentVm.getMips();
				vmsToRequestNow.add(currentVm);
			}
		}
		
		Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to Allocate "
				+ vmsToRequestNow.size() + " VMs now.");
		
		// trying to allocate Host
		for (GoogleVm requestedVm : vmsToRequestNow) {
			allocateHostForVm(false, requestedVm);				
		}
	}

	public SortedSet<GoogleVm> getVmsRunning() {
		return vmsRunning;
	}

	public SortedSet<GoogleVm> getVmsForScheduling() {
		return vmsForScheduling;
	}
	
	@Override
	public PreemptableVmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}
	
	@Override
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = (PreemptableVmAllocationPolicy) vmAllocationPolicy;
	}

	protected void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}
		
}
