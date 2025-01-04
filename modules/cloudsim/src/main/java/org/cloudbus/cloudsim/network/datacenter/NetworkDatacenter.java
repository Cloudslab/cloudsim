/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.*;

/**
 * NetworkDatacenter class is a {@link Datacenter} whose hostList are virtualized and networked. It contains
 * all the information about internal network. For example, which VM is connected to what switch etc. It
 * deals with processing of VM queries (i.e., handling of VMs) instead of processing
 * Cloudlet-related queries. So, even though an AllocPolicy will be instantiated (in the init()
 * method of the superclass, it will not be used, as processing of cloudlets are handled by the
 * CloudletScheduler and processing of VirtualMachines are handled by the VmAllocationPolicy.
 * 
 * //@TODO If an AllocPolicy is not being used, why it is being created. Perhaps
 * a better class hierarchy should be created, introducing some abstract class
 * or interface.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 3.0
 */
public class NetworkDatacenter extends Datacenter {
	/**
	 * A map between VMs and Switches, where each key
	 * is a VM id and the corresponding value is the id of the switch where the VM is connected to.
	 */
	public Map<Integer, Integer> VmToSwitchid;

	/**
	 * A map between hosts and Switches, where each key
	 * is a host id and the corresponding value is the id of the switch where the host is connected to.
	 */
	public Map<Integer, Integer> HostToSwitchid;

	/**
	 * A map of datacenter switches where each key is a switch id
	 * and the corresponding value is the switch itself.
	 */
	private final Map<Integer, Switch> SwitchList;

	/**
	 * A map between VMs and Hosts, where each key
	 * is a VM id and the corresponding value is the id of the host where the VM is placed.
	 */
	public Map<Integer, Integer> VmtoHostlist;

	/** Total data transmitted through the network of this datacenter (in bytes) */
	public double totalDataTransfer = 0;

	/**
	 * Instantiates a new NetworkDatacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by {@link org.cloudbus.cloudsim.core.SimEntity})
	 * @param characteristics the datacenter characteristics
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 * @param storageList a List of storage elements, for data simulation
         * @param schedulingInterval the scheduling delay to process each datacenter received event
	 * 
	 * @throws Exception  when one of the following scenarios occur:
	 *         <ul>
	 *         <li>creating this entity before initializing CloudSim package
	 *         <li>this entity name is <tt>null</tt> or empty
	 *         <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
	 *         No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
	 *         more Machines. A Machine must contain one or more PEs.
	 *         </ul>
	 * 
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public NetworkDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		VmToSwitchid = new HashMap<>();
		HostToSwitchid = new HashMap<>();
		VmtoHostlist = new HashMap<>();
		SwitchList = new HashMap<>();
	}

	public Map<Integer, Switch> getSwitchList() { return SwitchList; }

	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		super.processVmCreate(ev, ack);
		GuestEntity guest = (GuestEntity) ev.getData();
		HostEntity host = guest.getHost();

		if (host != null) {
			// very ugly, but no other way to support nested virtualization with the current network routing logic
			while (host instanceof VirtualEntity vm) {
				host = vm.getHost();
			}

			VmToSwitchid.put(guest.getId(), ((NetworkedEntity) host).getSwitch().getId());
			VmtoHostlist.put(guest.getId(), host.getId());
		}
	}

	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);

		NetworkCloudlet ncl = (NetworkCloudlet) ev.getData();

		int userId = ncl.getUserId();
		int vmId = ncl.getGuestId();
		NetworkedEntity host = (NetworkedEntity) getVmAllocationPolicy().getHost(vmId, userId);

		host.getNics().put(ncl.getCloudletId(), ncl.getNic());
	}

	/**
	 * Gets a map of all EdgeSwitches in the Datacenter network. 
         * One can design similar functions for other type of switches.
	 * 
         * @return a EdgeSwitches map, where each key is the switch id
         * and each value it the switch itself.
	 */
	public Map<Integer, Switch> getEdgeSwitch() {
		Map<Integer, Switch> edgeswitch = new HashMap<>();
		for (Entry<Integer, Switch> es : SwitchList.entrySet()) {
			if (es.getValue().level == Switch.SwitchLevel.EDGE_LEVEL) {
				edgeswitch.put(es.getKey(), es.getValue());
			}
		}
		return edgeswitch;

	}

	public void registerSwitch(Switch sw) {
		if (!getSwitchList().containsKey(sw.getId())) {
			getSwitchList().put(sw.getId(), sw);
		}
	}

	public void attachSwitchToHost(Switch sw, NetworkHost netHost) {
		if (!getSwitchList().containsKey(sw.getId()) || !getHostList().contains(netHost)) {
			throw new IllegalArgumentException("Switch or Host are not part of this Datacenter");
		}

		if (sw.level != Switch.SwitchLevel.EDGE_LEVEL) {
			throw new IllegalArgumentException("Switch is not at the edge level");
		}

		sw.hostList.put(netHost.getId(), netHost);
		sendNow(sw.getId(), CloudActionTags.NETWORK_ATTACH_HOST, netHost);
		HostToSwitchid.put(netHost.getId(), sw.getId());
		netHost.setSwitch(sw);
	}

	public void attachSwitchToSwitch(Switch sw1, Switch sw2) {
		if (!getSwitchList().containsKey(sw1.getId()) || !getSwitchList().containsKey(sw2.getId())) {
			throw new IllegalArgumentException("One or both switches are not part of this Datacenter");
		}

		// Switches are already connected
		if (sw1.downlinkSwitches.contains(sw2) || sw1.uplinkSwitches.contains(sw2)) {
			return;
		}

		if (sw1.level == Switch.SwitchLevel.EDGE_LEVEL) {
			if (sw2.level != Switch.SwitchLevel.AGGR_LEVEL) {
				throw new IllegalArgumentException("Edge switch can only be attached to Aggregate switch");
			} else {
				sw1.uplinkSwitches.add(sw2);
				sw2.downlinkSwitches.add(sw1);
			}
		} else if (sw1.level == Switch.SwitchLevel.AGGR_LEVEL) {
			if (sw2.level == Switch.SwitchLevel.ROOT_LEVEL) {
				sw1.uplinkSwitches.add(sw2);
				sw2.downlinkSwitches.add(sw1);
			} else if (sw2.level == Switch.SwitchLevel.EDGE_LEVEL) {
				sw1.downlinkSwitches.add(sw2);
				sw2.uplinkSwitches.add(sw1);
			} else {
				throw new IllegalArgumentException("Cannot attach to switch of same level");
			}
		} else { // root-level sw1
			if (sw2.level != Switch.SwitchLevel.AGGR_LEVEL) {
				throw new IllegalArgumentException("Root switch can only be attached to Aggregate switch");
			} else {
				sw1.downlinkSwitches.add(sw2);
				sw2.uplinkSwitches.add(sw1);
			}
		}
	}
}
