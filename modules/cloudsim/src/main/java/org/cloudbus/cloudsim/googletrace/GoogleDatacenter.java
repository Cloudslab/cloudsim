/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
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

	List<Vm> vmsNotFulfilled = new ArrayList<Vm>();
	
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

		if (!result && !vmsNotFulfilled.contains(vm)) {
			vmsNotFulfilled.add(vm);
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
			getVmList().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
					.getAllocatedMipsForVm(vm));
			
			vmsNotFulfilled.remove(vm);
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

		getVmList().remove(vm);
		
		if (!vmsNotFulfilled.isEmpty()) {
			double availableMips = host.getAvailableMips();
			double requestedMipsNow = 0;
			List<Vm> vmsToRequestNow = new ArrayList<Vm>();
			
			// choosing the vms to request now
			for (Vm currentVm : new ArrayList<Vm>(vmsNotFulfilled)) {
				if (requestedMipsNow + currentVm.getMips() <= availableMips) {
					requestedMipsNow += currentVm.getMips();
					vmsToRequestNow.add(currentVm);
				}
			}
			
			// trying to allocate Host
			for (Vm requestedVm : vmsToRequestNow) {
				allocateHostForVm(true, requestedVm);				
			}
		}
	}
}
