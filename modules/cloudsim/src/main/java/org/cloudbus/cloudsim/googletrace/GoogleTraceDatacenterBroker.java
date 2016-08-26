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


	protected List<GoogleTask> createdTasks;
	
	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter characteristics map where each key
         * is a datacenter id and each value is its characteristics.. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	private GoogleCloudletDataStore cloudletDataStore;
	
	

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by {@link SimEntity} class)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public GoogleTraceDatacenterBroker(String name, String receivedCloudletsDatabaseURL) throws Exception {
		super(name);

		setCreatedTasks(new ArrayList<GoogleTask>());

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		cloudletDataStore = new GoogleCloudletDataStore(receivedCloudletsDatabaseURL);		
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
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			case CloudSimTags.VM_DESTROY_ACK:
				processVmDestroyAck(ev);
				break;			
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	private void processVmDestroyAck(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #",
				vmId, " is being destroyed now.");
		
		if (result == CloudSimTags.TRUE) {
			GoogleTask task = getTaskById(getCreatedTasks(), vmId);
			
			double now = CloudSim.clock();
			double startTime = task.getStartTime();
			//TODO check and change the hard values
			GoogleCloudletState cloudletState = new GoogleCloudletState(vmId, 2, task.getCpuReq(), task.getSubmitTime(), startTime, now,
					now - startTime, Cloudlet.SUCCESS);
			cloudletDataStore.addCloudlet(cloudletState);
			
			getCreatedTasks().remove(task); // removing cloudlet
			Vm vm = VmList.getById(getVmsCreatedList(), task.getId());
			getVmsCreatedList().remove(vm); // removing from created list
			
			if (getCreatedTasks().size() == 0) { 
				Log.printConcatLine(CloudSim.clock(), ": ", getName(),
						": All Cloudlets executed. Finishing...");
				finishExecution();
			}
		} else {
			System.out.println("Error while destroying VM #"+ vmId);
		}
	}

	private GoogleTask getTaskById(List<GoogleTask> tasks, int id) {
		for (GoogleTask task : tasks) {
			if (task.getId() == id) {
				return task;
			}
		}
		return null;
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
			submitTasks();
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

		// if VM was created successfully
		if (result == CloudSimTags.TRUE) {
			Vm createdVm = VmList.getById(getVmRequestedList(), vmId);
			getVmsCreatedList().add(createdVm); // adding VM to created list
			getVmRequestedList().remove(createdVm); // removing from requested list
			
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #",
					vmId, " has been created in Datacenter #", datacenterId,
					", Host #", createdVm.getHost().getId());

			GoogleTask task = getTaskById(getCreatedTasks(), vmId);		
			System.out.println("Task is null? " +( task == null));
			task.setStartTime(CloudSim.clock());
			
			Vm vm = VmList.getById(getVmsCreatedList(), task.getId());

			send(getDatacenterId(), task.getRuntime(), CloudSimTags.VM_DESTROY_ACK, vm);

		} else { //This situation should not happen
			Log.printConcatLine(CloudSim.clock(), ": ", getName(),
					": Creation of VM #", vmId, " failed in Datacenter #",
					datacenterId);

		}
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
	protected void submitTasks() {
		Log.printConcatLine("Scheduling the creation of VMs.");
		for (GoogleTask task : getCreatedTasks()) {
			scheduleRequestsForVm(task);
		}
	}

	private void scheduleRequestsForVm(GoogleTask task) {
		long size = 0; // image size (MB)
		double mips = task.getCpuReq();
		long bw = 0;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		Vm vmForRequest = new Vm(task.getId(), getId(), mips,
				pesNumber, (int) task.getMemReq(), bw, size, vmm,
				new CloudletSchedulerTimeShared());
		getVmRequestedList().add(vmForRequest);

		int datacenterId = getDatacenterId();
		/*
		 *  TODO check if the delay is passed correctly. 
		 *  (delay = submitTime - clock) in the case where tasks are submitted in timestamp different of 0. 
		 */
		scheduleVmCreationInDatacenter(vmForRequest, datacenterId, task.getSubmitTime() - CloudSim.clock());
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

	public List<GoogleTask> getCreatedTasks() {
		return createdTasks;
	}

	protected void setCreatedTasks(List<GoogleTask> createdTasks) {
		this.createdTasks = createdTasks;
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

	public List<GoogleCloudletState> getReceivedCloudlets() {
		return cloudletDataStore.getAllCloudlets();
	}

	public void submitTasks(List<GoogleTask> taskList) {
		getCreatedTasks().addAll(taskList);
	}

}
