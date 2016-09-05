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
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * TODO
 *  
 * @author Giovanni Farias
 * 
 */
public class GoogleDatacenter extends Datacenter {

	private SortedSet<Vm> vmsRunning = new TreeSet<Vm>();
	private SortedSet<Vm> vmsForScheduling = new TreeSet<Vm>();	
	
	public GoogleDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
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
		Vm vm = (Vm) ev.getData();

		allocateHostForVm(ack, vm);
	}

	private void allocateHostForVm(boolean ack, Vm vm) {
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

		if (!result && !getVmsForScheduling().contains(vm)) {
			Log.printConcatLine(CloudSim.clock(),
					": There is not resource to allocate VM #" + vm.getId()
							+ " now, it will be tryed in the future.");
			getVmsForScheduling().add(vm);
			return;
		}
		
		if (ack) {
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

		if (result) {
			getVmsRunning().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			// We don't need to update the vm processing because there aren't cloudlets running in the vm
//			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
//					.getAllocatedMipsForVm(vm));
			
			getVmsForScheduling().remove(vm);
		}
	}
	
	@Override
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		Host host = vm.getHost();
		getVmAllocationPolicy().deallocateHostForVm(vm);		
		
		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
		}

		getVmsRunning().remove(vm);
		
		if (!getVmsForScheduling().isEmpty()) {
			double availableMips = host.getAvailableMips();
			double mipsForRequestingNow = 0;
			List<Vm> vmsToRequestNow = new ArrayList<Vm>();
			
			// choosing the vms to request now
			for (Vm currentVm : new ArrayList<Vm>(getVmsForScheduling())) {
				if (mipsForRequestingNow + currentVm.getMips() <= availableMips) {
					mipsForRequestingNow += currentVm.getMips();
					vmsToRequestNow.add(currentVm);
				}
			}
			
			Log.printConcatLine(CloudSim.clock(), "Trying to Allocate "
					+ vmsToRequestNow.size() + " VMs now.");
			
			// trying to allocate Host
			for (Vm requestedVm : vmsToRequestNow) {
				allocateHostForVm(true, requestedVm);				
			}
		}
	}

	public SortedSet<Vm> getVmsRunning() {
		return vmsRunning;
	}

	public SortedSet<Vm> getVmsForScheduling() {
		return vmsForScheduling;
	}
	
	
}
