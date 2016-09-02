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
import java.util.Properties;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.googletrace.datastore.GoogleInputTraceDataStore;
import org.cloudbus.cloudsim.googletrace.datastore.GoogleOutputTaskDataStore;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user.
 * 
 * @author Giovanni Farias
 * 
 */
public class GoogleTraceDatacenterBroker extends SimEntity {
	
	protected static final int STORE_FINISHED_TASKS = CloudSimTags.VM_BROKER_EVENT + 500;

	protected List<? extends Vm> vmRequestedList = new ArrayList<Vm>();

	/** The list of VMs created by the broker. */
	protected List<? extends Vm> vmsCreatedList = new ArrayList<Vm>();

	protected List<GoogleTask> createdTasks;
	
	protected List<GoogleTask> submittedTasks;
	
	protected List<GoogleTaskState> finishedTasks;
	
	/** The id's list of available datacenters. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter characteristics map where each key
         * is a datacenter id and each value is its characteristics.. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	private int intervalIndex;
	private int intervalSize; // in minutes
	
	private GoogleInputTraceDataStore inputTraceDataStore;
	
	private GoogleOutputTaskDataStore taskDataStore;
	
	public GoogleTraceDatacenterBroker(String name, Properties properties) throws Exception {
		super(name);

		setCreatedTasks(new ArrayList<GoogleTask>());
		setSubmittedTasks(new ArrayList<GoogleTask>());
		setFinishedTasks(new ArrayList<GoogleTaskState>());
		
		setIntervalIndex(0);
		setSubmitInterval(Integer.parseInt(properties.getProperty("interval_size")));
		
		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		
		inputTraceDataStore = new GoogleInputTraceDataStore(properties);
		taskDataStore = new GoogleOutputTaskDataStore(properties);		
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
			// load next google tasks from trace file
			case CloudSimTags.VM_BROKER_EVENT:
				loadNextGoogleTasks();
				break;
			// store already finished tasks
			case STORE_FINISHED_TASKS:
				storeFinishedTasks();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	private void storeFinishedTasks() {
		List<GoogleTaskState> toStore = new ArrayList<GoogleTaskState>(getFinishedTasks());
		if (toStore != null && !toStore.isEmpty() && taskDataStore.addTaskList(toStore)) {
			Log.printConcatLine(CloudSim.clock(), ": ", toStore.size(), " VMs stored now.");
			getFinishedTasks().removeAll(toStore);
		}
		
		System.out.println("tasks size: " + taskDataStore.getAllTasks().size());
		
		if (inputTraceDataStore.hasMoreEvents(getIntervalIndex(), getIntervalSizeInMicro())) {
			send(getId(), getIntervalSizeInMicro(), STORE_FINISHED_TASKS);
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
			GoogleTask task = getTaskById(getSubmittedTasks(), vmId);
			
			double now = CloudSim.clock();
			double startTime = task.getStartTime();
			
			getSubmittedTasks().remove(task); // removing task
			Vm vm = VmList.getById(getVmsCreatedList(), task.getId());
			getVmsCreatedList().remove(vm); // removing from created list

//			int hostId = vm.getHost().getId();

			GoogleTaskState taskState = new GoogleTaskState(vmId, 2,
					task.getCpuReq(), task.getSubmitTime(), startTime, now,
					task.getRuntime(), Cloudlet.SUCCESS);
//			taskDataStore.addTask(taskState);
			finishedTasks.add(taskState);
			
			if (getCreatedTasks().size() == 0 && getSubmittedTasks().size() == 0) { 
				Log.printConcatLine(CloudSim.clock(), ": ", getName(),
						": All Tasks executed. Finishing...");
				finishExecution();
			}
		} else {
			Log.printLine("Error while destroying VM #"+ vmId);
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
			loadNextGoogleTasks();
		
			// creating the first store event
			send(getId(), getIntervalSizeInMicro(), STORE_FINISHED_TASKS);
		}
		
	}

	private void loadNextGoogleTasks() {
		Log.printLine("Loading next google tasks. Interval index " + getIntervalIndex());

		List<GoogleTask> nextGoogleTasks = inputTraceDataStore
				.getGoogleTaskInterval(getIntervalIndex(), getIntervalSizeInMicro());

		// if nextGoogleTasks == null there are not more tasks 
		if (nextGoogleTasks != null ) {
			getCreatedTasks().addAll(nextGoogleTasks);
			submitTasks();
			setIntervalIndex(++intervalIndex);

			send(getId(), getIntervalSizeInMicro(), CloudSimTags.VM_BROKER_EVENT);
		}
	}

	private double getIntervalSizeInMicro() {
		return getIntervalSize() * 60 * 1000000;
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

			GoogleTask task = getTaskById(getSubmittedTasks(), vmId);		
			
			Log.printLine("Task is null? " +( task == null));
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
		for (GoogleTask task : new ArrayList<GoogleTask>(getCreatedTasks())) {
			scheduleRequestsForVm(task);
		}
	}

	private void scheduleRequestsForVm(GoogleTask task) {

		GoogleVm vmForRequest = new GoogleVm(task.getId(), getId(), task.getCpuReq(),
				task.getMemReq(), task.getSubmitTime(), task.getSchedulingClass());
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
		
		GoogleTask task = getTaskById(getCreatedTasks(), vm.getId());
		getCreatedTasks().remove(task);
		getSubmittedTasks().add(task);
		
		send(datacenterId, delay, CloudSimTags.VM_CREATE_ACK, vm);
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		storeFinishedTasks();
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
	
	public List<GoogleTask> getSubmittedTasks() {
		return submittedTasks;
	}

	protected void setSubmittedTasks(List<GoogleTask> submittedTasks) {
		this.submittedTasks = submittedTasks;
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

	public List<GoogleTaskState> getReceivedCloudlets() {
//		return finishedTasks;
		return taskDataStore.getAllTasks();
	}

	public void submitTasks(List<GoogleTask> taskList) {
		getCreatedTasks().addAll(taskList);
	}

	public int getIntervalIndex() {
		return intervalIndex;
	}

	private void setIntervalIndex(int intervalIndex) {
		this.intervalIndex = intervalIndex;
	}

	public int getIntervalSize() {
		return intervalSize;
	}

	private void setSubmitInterval(int intervalSize) {
		this.intervalSize = intervalSize;
	}

	public List<GoogleTaskState> getFinishedTasks() {
		return finishedTasks;
	}

	public void setFinishedTasks(List<GoogleTaskState> finishedTasks) {
		this.finishedTasks = finishedTasks;
	}
}
