/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.*;

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

/**
 * DatacentreBroker represents a broker acting on behalf of a user.
 *
 * @author Giovanni Farias
 */
public class GoogleTraceDatacenterBroker extends SimEntity {

    private static final int BROKER_BASE = 500;
    protected static final int LOAD_NEXT_TASKS_EVENT = BROKER_BASE + 1;
    protected static final int STORE_FINISHED_TASKS_EVENT = BROKER_BASE + 2;

    private static final int DEFAULT_LOADING_INTERVAL_SIZE = 5;
    private static final int DEFAULT_STORING_INTERVAL_SIZE = 5;

    protected TreeSet<GoogleTask> createdTasks;

    protected Map<Integer, GoogleTask> submittedTasks;

    protected List<GoogleTaskState> finishedTasks;

    /**
     * The id's list of available datacenters.
     */
    protected List<Integer> datacenterIdsList;

    /**
     * The datacenter characteristics map where each key
     * is a datacenter id and each value is its characteristics..
     */
    protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

    private int intervalIndex;
    private int loadingIntervalSize; // in minutes
    private int storingIntervalSize; // in minutes

    private GoogleInputTraceDataStore inputTraceDataStore;

    private GoogleOutputTaskDataStore taskDataStore;

    public GoogleTraceDatacenterBroker(String name, Properties properties) throws Exception {
        super(name);

        setCreatedTasks(new TreeSet<GoogleTask>());
        setSubmittedTasks(new HashMap<Integer, GoogleTask>());
        setFinishedTasks(new ArrayList<GoogleTaskState>());

        int loadingIntervalSize = properties
                .getProperty("loading_interval_size") == null ? DEFAULT_LOADING_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("loading_interval_size"));

        int storingIntervalSize = properties
                .getProperty("storing_interval_size") == null ? DEFAULT_STORING_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("storing_interval_size"));

        setIntervalIndex(0);
        setLoadingIntervalSize(loadingIntervalSize);
        setStoringIntervalSize(storingIntervalSize);

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
            // if the simulation finishes
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
            case CloudSimTags.VM_DESTROY_ACK:
                processVmDestroyAck(ev);
                break;
            // load next google tasks from trace file
            case LOAD_NEXT_TASKS_EVENT:
                loadNextGoogleTasks();
                break;
            // store already finished tasks
            case STORE_FINISHED_TASKS_EVENT:
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
        if (toStore != null && !toStore.isEmpty()
                && taskDataStore.addTaskList(toStore)) {
            Log.printConcatLine(CloudSim.clock(), ": ", toStore.size(),
                    " VMs stored now.");
            getFinishedTasks().removeAll(toStore);
        }

        Log.printConcatLine(CloudSim.clock(), ": Currently there are "
                + taskDataStore.getAllTasks().size()
                + " tasks stored in the database.");

        // creating next event if the are more events to be treated
        if (inputTraceDataStore.hasMoreEvents(getIntervalIndex(),
                getIntervalSizeInMicro(getStoringIntervalSize()))) {
            send(getId(), getIntervalSizeInMicro(getStoringIntervalSize()), STORE_FINISHED_TASKS_EVENT);
        }
    }

    private void processVmDestroyAck(SimEvent ev) {
        GoogleVm vm = (GoogleVm) ev.getData();

        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #",
                vm.getId(), " is being destroyed now.");

        GoogleTask task = getSubmittedTasks().get(vm.getId());

        double now = CloudSim.clock();

        getSubmittedTasks().remove(task); // removing task

        GoogleTaskState taskState = new GoogleTaskState(vm.getId(), 2,
                task.getCpuReq(), task.getSubmitTime(), vm.getStartExec(), now,
                task.getRuntime(), Cloudlet.SUCCESS, task.getPriority());
        finishedTasks.add(taskState);

        if (getCreatedTasks().size() == 0 && getSubmittedTasks().size() == 0) {
            Log.printConcatLine(CloudSim.clock(), ": ", getName(),
                    ": All Tasks executed. Finishing...");
            finishExecution();
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
            send(getId(), getIntervalSizeInMicro(getStoringIntervalSize()), STORE_FINISHED_TASKS_EVENT);
        }

    }

    private void loadNextGoogleTasks() {
        Log.printLine("Loading next google tasks. Interval index " + getIntervalIndex());

        List<GoogleTask> nextGoogleTasks = inputTraceDataStore
                .getGoogleTaskInterval(getIntervalIndex(), getIntervalSizeInMicro(getLoadingIntervalSize()));

        // if nextGoogleTasks == null there are not more tasks
        if (nextGoogleTasks != null) {
            getCreatedTasks().addAll(nextGoogleTasks);
            submitTasks();
            setIntervalIndex(++intervalIndex);

            send(getId(), getIntervalSizeInMicro(getLoadingIntervalSize()), LOAD_NEXT_TASKS_EVENT);
        }
    }

    private double getIntervalSizeInMicro(double intervalSize) {
        return intervalSize * 60 * 1000000;
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

        while (!getCreatedTasks().isEmpty()) {
            scheduleRequestsForVm(getCreatedTasks().first());
        }
    }

    private void scheduleRequestsForVm(GoogleTask task) {

        GoogleVm vm = new GoogleVm(task.getId(), getId(), task.getCpuReq(),
                task.getMemReq(), task.getSubmitTime(), task.getPriority(), task.getRuntime());

        int datacenterId = getDatacenterId();

        Log.printLine(CloudSim.clock() + ": " + getName()
                + ": Trying to Create VM #" + vm.getId() + " in "
                + datacenterId);

        getCreatedTasks().remove(task);
        getSubmittedTasks().put(task.getId(), task);

        double delay = task.getSubmitTime() - CloudSim.clock();
        send(datacenterId, delay, CloudSimTags.VM_CREATE, vm);
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

    public SortedSet<GoogleTask> getCreatedTasks() {
        return createdTasks;
    }

    public Map<Integer, GoogleTask> getSubmittedTasks() {
        return submittedTasks;
    }

    protected void setSubmittedTasks(HashMap<Integer, GoogleTask> submittedTasks) {
        this.submittedTasks = submittedTasks;
    }

    protected void setCreatedTasks(TreeSet<GoogleTask> createdTasks) {
        this.createdTasks = createdTasks;
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

    public List<GoogleTaskState> getStoredTasks() {
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

    public int getLoadingIntervalSize() {
        return loadingIntervalSize;
    }

    private void setLoadingIntervalSize(int intervalSize) {
        this.loadingIntervalSize = intervalSize;
    }

    public int getStoringIntervalSize() {
        return storingIntervalSize;
    }

    private void setStoringIntervalSize(int storingIntervalSize) {
        this.storingIntervalSize = storingIntervalSize;
    }

    public List<GoogleTaskState> getFinishedTasks() {
        return finishedTasks;
    }

    public void setFinishedTasks(List<GoogleTaskState> finishedTasks) {
        this.finishedTasks = finishedTasks;
    }
}
