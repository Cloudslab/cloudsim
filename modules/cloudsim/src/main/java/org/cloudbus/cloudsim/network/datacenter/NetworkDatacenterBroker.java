/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.SimEvent;
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
public class NetworkDatacenterBroker extends DatacenterBroker {
	/** The number of submitted cloudlets. */
	private int cloudletsSubmitted;

	public static NetworkDatacenter linkDC;

	public static int cachedcloudlet = 0;

	public static int iteration;

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
	public NetworkDatacenterBroker(String name, int iteration) throws Exception {
		super(name);
		this.iteration = iteration;
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
			case CloudSimTags.NextCycle -> createVmsInDatacenter(linkDC.getId());

			// other (potentially unknown tags) are processed by the base class
			default -> super.processEvent(ev);
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
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0 && iteration > NetworkTags.MAX_ITERATION) {
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

	@Override
	protected void submitCloudlets() {
		int vmIndex = 0;
		List<Cloudlet> successfullySubmitted = new ArrayList<>();

		if (getCloudletList().isEmpty()) {
			System.out.println("WARNING: no AppCloudlet defined");
			return;
		}

		for (AppCloudlet appCloudlet : this.<AppCloudlet>getCloudletList()) {
			if (appCloudlet.cList.isEmpty()) {
				System.out.println("WARNING: no tasks within AppCloudlet #"+appCloudlet.getCloudletId());
				return;
			}

			for (Cloudlet cloudlet : appCloudlet.cList) {
				GuestEntity vm;
				// if user didn't bind this cloudlet and it has not been executed yet
				if (cloudlet.getGuestId() == -1) {
					vm = getGuestsCreatedList().get(vmIndex);
				} else { // submit to the specific vm
					vm = VmList.getById(getGuestsCreatedList(), cloudlet.getGuestId());
					if (vm == null) { // vm was not created
						vm = VmList.getById(getGuestList(), cloudlet.getGuestId()); // check if exists in the submitted list

						if (!Log.isDisabled()) {
							if (vm != null) {
								Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
										cloudlet.getCloudletId(), ": bount " + vm.getClassName() + " #" + vm.getId() + " not available");
							} else {
								Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
										cloudlet.getCloudletId(), ": bount guest entity doesn't exist");
							}
						}
						continue;
					}
				}

				if (!Log.isDisabled()) {
					Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending ", cloudlet.getClass().getSimpleName(),
							" #", cloudlet.getCloudletId(), " to " + vm.getClassName() + " #", vm.getId());
				}

				cloudlet.setGuestId(vm.getId());
				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
				cloudletsSubmitted++;
				vmIndex = (vmIndex + 1) % getGuestsCreatedList().size();
				getCloudletSubmittedList().add(cloudlet);
				successfullySubmitted.add(cloudlet);
			}
		}

		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);
	}
}
