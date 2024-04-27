/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * NetDatacentreBroker represents a broker acting on behalf of Datacenter provider. It hides VM
 * management, as vm creation, submission of cloudlets to these VMs and destruction of VMs. <br/>
 * <tt>NOTE</tt>: This class is an example only. It works on behalf of a provider not for users. 
 * One has to implement interaction with user broker to this broker.
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 3.0
 * //TODO The class is not a broker acting on behalf of users, but on behalf
 * of a provider. Maybe this distinction would be explicit by 
 * different class hierarchy, such as UserDatacenterBroker and ProviderDatacenterBroker.
 */
public class NetDatacenterBroker extends DatacenterBroker {
	/** The list of submitted {@link AppCloudlet AppCloudlets}. */
	private List<? extends AppCloudlet> cloudletList;

	/** The number of submitted cloudlets. */
	private int cloudletsSubmitted;

	public static NetworkDatacenter linkDC;

	public boolean createvmflag = true;

	public static int cachedcloudlet = 0;

	/**
	 * Creates a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity
	 * 
	 * @throws Exception the exception
	 * 
	 * @pre name != null
	 * @post $none
	 */
	public NetDatacenterBroker(String name) throws Exception {
		super(name);
	}

	public static void setLinkDC(NetworkDatacenter alinkDC) {
		linkDC = alinkDC;
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
			// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST -> processResourceCharacteristicsRequest(ev);

			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS -> processResourceCharacteristics(ev);
			// VM Creation answer TODO: Remo Andreoli: missing?

			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN -> processCloudletReturn(ev);
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION -> shutdownEntity();

			case CloudSimTags.NextCycle -> {
				if (NetworkConstants.BASE) {
					createVmsInDatacenter(linkDC.getId());
				}
			}

			// other unknown tags are processed by this method
			default -> processOtherEvent(ev);
		}
	}

	/**
	 * Processes the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != null
	 * @post $none
	 */

	/**
	 * Processes a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * 
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		cloudletsSubmitted--;
		// all cloudlets executed
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0 && NetworkConstants.iteration > 10) {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Creates virtual machines in a datacenter and submit/schedule cloudlets to them.
	 * 
	 * @param datacenterId Id of the Datacenter to create the VMs
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the
		// next one
		int requestedVms = 0;

		// All host will have two VMs (assumption) VM is the minimum unit
		if (createvmflag) {
			CreateVMs(datacenterId);
			createvmflag = false;
		}

		// generate Application execution Requests
		for (int i = 0; i < 100; i++) {
			this.getCloudletList().add(
					new TestWorkflowApp(AppCloudlet.APP_Workflow, NetworkConstants.currentAppId, 0, 0, getId()));
			NetworkConstants.currentAppId++;

		}
		int k = 0;

		// schedule the application on VMs
		for (AppCloudlet app : this.<AppCloudlet>getCloudletList()) {

			List<Integer> vmids = new ArrayList<>();
			int numVms = linkDC.getVmList().size();
			UniformDistr ufrnd = new UniformDistr(0, numVms, 5);
			for (int i = 0; i < app.numbervm; i++) {

				int vmid = (int) ufrnd.sample();
				vmids.add(vmid);

			}

			if (vmids != null) {
				if (!vmids.isEmpty()) {

					app.createCloudletList(vmids);
					for (int i = 0; i < app.numbervm; i++) {
						app.cloudletList.get(i).setUserId(getId());
						this.getCloudletSubmittedList().add(app.cloudletList.get(i));
						cloudletsSubmitted++;

						// Sending cloudlet
						sendNow(
								getVmsToDatacentersMap().get(this.getGuestList().get(0).getId()),
								CloudSimTags.CLOUDLET_SUBMIT,
								app.cloudletList.get(i));
					}
					System.out.println("app" + (k++));
				}
			}

		}
		setCloudletList(new ArrayList<>());
		if (NetworkConstants.iteration < 10) {

			NetworkConstants.iteration++;
			this.schedule(getId(), NetworkConstants.nexttime, CloudSimTags.NextCycle);
		}

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

        /**
         * Creates virtual machines in a datacenter
         * @param datacenterId The id of the datacenter where to create the VMs.
         */
	private void CreateVMs(int datacenterId) {
		// two VMs per host
		int numVM = linkDC.getHostList().size() * NetworkConstants.maxhostVM;
		for (int i = 0; i < numVM; i++) {
			int vmid = i;
			int mips = 1;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = NetworkConstants.HOST_PEs / NetworkConstants.maxhostVM;
			String vmm = "Xen"; // VMM name

			// create VM
			NetworkVm vm = new NetworkVm(
					vmid,
					getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					vmm,
					new NetworkCloudletSpaceSharedScheduler());
			linkDC.processVmCreateNetwork(vm);
			// add the VM to the vmList
			getGuestList().add(vm);
			getVmsToDatacentersMap().put(vmid, datacenterId);
			getGuestsCreatedList().add(VmList.getById(getGuestList(), vmid));
		}
	}
}
