/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, submission of cloudlets to VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class GoogleTraceDatacenterBroker extends SimEntity {

	/** The list of VMs submitted to be managed by the broker. */
	protected List<? extends Vm> vmList;

	/** The list of VMs created by the broker. */
	protected List<? extends Vm> vmsCreatedList;

	/** The list of cloudlet submitted to the broker. 
         * @see #submitCloudletList(java.util.List) 
         */
	protected List<? extends Cloudlet> cloudletList;

	/** The list of submitted cloudlets. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The list of received cloudlet. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The number of submitted cloudlets. */
	protected int cloudletsSubmitted;

	/** The number of requests to create VM. */
	protected int vmsRequested;

	/** The number of acknowledges (ACKs) sent in response to
         * VM creation requests. */
	protected int vmsAcks;

	/** The number of destroyed VMs. */
	protected int vmsDestroyed;

	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

	/** The list of datacenters where was requested to place VMs. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map, where each key is a VM id
         * and each value is the datacenter id whwere the VM is placed. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics map where each key
         * is a datacenter id and each value is its characteristics.. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by {@link SimEntity} class)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public GoogleTraceDatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
         * 
         * @todo The name of the method is confused with the {@link #submitCloudlets()},
         * that in fact submit cloudlets to VMs. The term "submit" is being used
         * ambiguously. The method {@link #submitCloudlets()} would be named "sendCloudletsToVMs"
         * 
         * The method {@link #submitVmList(java.util.List)} may have
         * be checked too.
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a Datacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			submitCloudlets();
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloud Resource List received with ",
				getDatacenterIdsList().size(), " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		// if VM was created
		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);

			Vm createdVm = VmList.getById(getVmList(), vmId);
			getVmsCreatedList().add(createdVm);
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #",
					vmId, " has been created in Datacenter #", datacenterId,
					", Host #", createdVm.getHost().getId());

			Cloudlet cloudlet = CloudletList.getById(getCloudletList(), vmId);

			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": Sending cloudlet ", cloudlet.getCloudletId(),
					" to VM #", vmId);

			sendNow(getVmsToDatacentersMap().get(vmId),
					CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;

			getCloudletSubmittedList().add(cloudlet);
			getCloudletList().remove(cloudlet);

		} else {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": Creation of VM #", vmId, " failed in Datacenter #",
					datacenterId);

			/*
			 * TODO Do we really need to do it? Once a VM was not created,
			 * should we try to create it again? Firstly there isn't problem
			 * because we are considering only one datacenter for our cloud (so
			 * the code won't try to create again), but if exist other
			 * datacenter, should the code tires?
			 * 
			 * If think so, but we can think better about how to chosse the
			 * datacenter.
			 */

			getDatacenterRequestedIdsList().add(datacenterId);

			for (int nextDatacenterId : getDatacenterIdsList()) {
				if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
					createVmInDatacenter(VmList.getById(getVmList(), vmId),
							nextDatacenterId, 0);
					break;
				}
			}
		}

		incrementVmsAcks();
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ",
				cloudlet.getCloudletId(), " received");
		cloudletsSubmitted--;

		// destroying VM binded to returned cloudlet
		destroyBindedVm(cloudlet);

		// all cloudlets executed
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { 
			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else {
			// There are cloudlets running yet
		}
	}

	private void destroyBindedVm(Cloudlet cloudlet) {
		Vm vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
		Log.printConcatLine(CloudSim.clock(), ": " + getName(),
				": Destroying VM #", vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
	}

	/**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
         * @todo to ensure the method will be overridden, it should be defined 
         * as abstract in a super class from where new brokers have to be extended.
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): ", "Error - an event is null.");
			return;
		}

		Log.printConcatLine(getName(), ".processOtherEvent(): Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
         * @see #submitCloudletList(java.util.List) 
	 */
	protected void submitCloudlets() {
		Log.printConcatLine("Scheduling the creation of VMs.");
		for (Cloudlet cloudlet : getCloudletList()) {
			scheduleRequestsForVm(cloudlet);
		}
	}

	private void scheduleRequestsForVm(Cloudlet cloudlet) {
		GoogleCloudlet gCloudlet = (GoogleCloudlet) cloudlet;
		long size = 0; // image size (MB)
		double mips = gCloudlet.getCpuReq();
		System.out.println("VM Mips: " + mips);
		long bw = 0;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name
		
		Vm vm = new Vm(gCloudlet.getCloudletId(), gCloudlet.getUserId(), mips,
				pesNumber, (int) gCloudlet.getMemReq(), bw, size, vmm,
				new CloudletSchedulerTimeShared());
		getVmList().add(vm);
		
		cloudlet.setVmId(vm.getId());
				
		for (int nextDatacenterId : getDatacenterIdsList()) {
			if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
				createVmInDatacenter(vm, nextDatacenterId, gCloudlet.getDelay());	
				break;
			}
		}		
	}

	private void createVmInDatacenter(Vm vm, int datacenterId, double delay) {
		//TODO Think better and check requestedVms local and global variables
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": Trying to Create VM #" + vm.getId() + " in "
					+ datacenterName);
			send(datacenterId, delay, CloudSimTags.VM_CREATE_ACK, vm);
			requestedVms++;
		}

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Destroy all virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		//TODO check it
//		for (Vm vm : getVmsCreatedList()) {
//			Log.printConcatLine(CloudSim.clock(), ": " + getName(), ": Destroying VM #", vm.getId());
//			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
//		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	@Override
	public void shutdownEntity() {
		Log.printConcatLine(getName(), " is shutting down...");
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment the number of acknowledges (ACKs) sent in response
         * to requests of VM creation.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

}
