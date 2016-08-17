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
 * DatacentreBroker represents a broker acting on behalf of a user.
 * 
 * @author Giovanni Farias
 * 
 */
public class GoogleTraceDatacenterBroker extends SimEntity {

	protected List<? extends Vm> vmRequestedList = new ArrayList<Vm>();

	/** The list of VMs created by the broker. */
	protected List<? extends Vm> vmsCreatedList = new ArrayList<Vm>();

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

	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

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

		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
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
			Vm createdVm = VmList.getById(getVmRequestedList(), vmId);			
			getVmsCreatedList().add(createdVm); // adding VM to created list
			getVmRequestedList().remove(createdVm); // removing from requested list
			
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #",
					vmId, " has been created in Datacenter #", datacenterId,
					", Host #", createdVm.getHost().getId());

			Cloudlet cloudlet = CloudletList.getById(getCloudletList(), vmId);

			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": Sending cloudlet ", cloudlet.getCloudletId(),
					" to VM #", vmId);

			sendNow(getDatacenterId(),
					CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;

			getCloudletSubmittedList().add(cloudlet); // adding to submitted cloudlet list
			getCloudletList().remove(cloudlet); // removing from cloudlet list

		} else {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": Creation of VM #", vmId, " failed in Datacenter #",
					datacenterId);
			
			Cloudlet cloudlet = CloudletList.getById(getCloudletList(), vmId);
			try {
				// Setting the cloudlet status to failed due to resource unavailability 
				cloudlet.setCloudletStatus(Cloudlet.FAILED_RESOURCE_UNAVAILABLE);
				getCloudletReceivedList().add(cloudlet); // adding to received cloudlet list
				getCloudletList().remove(cloudlet); // removing from cloudlet list
			} catch (Exception e) {	
				Log.print(e);
				Log.printLine("There was an error while setting cloudlet status to FAILED_RESOURCE_UNAVAILABLE.");
			}
			
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

		}
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
		getCloudletReceivedList().add(cloudlet); // adding to received list
		getCloudletSubmittedList().remove(cloudlet); // removing from submitted list
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloudlet ",
				cloudlet.getCloudletId(), " received");
		cloudletsSubmitted--;

		// destroying VM binded to returned cloudlet
		destroyBindedVm(cloudlet);

		/*
		 * TODO We can think about to put checkpoint here (when a cloudlet
		 * returns we could serialize it). Other possibility is to create a new
		 * event to serialize the current results.
		 */
		
		// all cloudlets executed
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { 
			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": All Cloudlets executed. Finishing...");
			finishExecution();
		} else {
			// There are cloudlets running yet
		}
	}

	private void destroyBindedVm(Cloudlet cloudlet) {
		Vm vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
		getVmsCreatedList().remove(vm); // removing from created list
		Log.printConcatLine(CloudSim.clock(), ": " + getName(),
				": Destroying VM #", vm.getId());
		sendNow(getDatacenterId(), CloudSimTags.VM_DESTROY, vm);
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
		long bw = 0;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		Vm vmForRequest = new Vm(gCloudlet.getCloudletId(), gCloudlet.getUserId(), mips,
				pesNumber, (int) gCloudlet.getMemReq(), bw, size, vmm,
				new CloudletSchedulerTimeShared());
		getVmRequestedList().add(vmForRequest);
		cloudlet.setVmId(vmForRequest.getId());

		int datacenterId = getDatacenterId();
		scheduleVmCreationInDatacenter(vmForRequest, datacenterId, gCloudlet.getDelay());
	}

	/*
	 * TODO This method is returning always the first element because our
	 * simulation has only one datacenter. In the future, if we would like to
	 * simulate more than one datacenter, we can implement a policy to choose
	 * the best datacenter
	 */
	private int getDatacenterId() {
		return getDatacenterIdsList().get(0);
	}

	private void scheduleVmCreationInDatacenter(Vm vm, int datacenterId, double delay) {
		String datacenterName = CloudSim.getEntityName(datacenterId);
		Log.printLine(CloudSim.clock() + ": " + getName()
				+ ": Trying to Create VM #" + vm.getId() + " in "
				+ datacenterName);
		send(datacenterId, delay, CloudSimTags.VM_CREATE_ACK, vm);
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
	public <T extends Vm> List<T> getVmRequestedList() {
		return (List<T>) vmRequestedList;
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
}
