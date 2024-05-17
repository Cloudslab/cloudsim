/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
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
 * //TODO If an AllocPolicy is not being used, why it is being created. Perhaps
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
	public Map<Integer, Switch> Switchlist;

        /**
         * A map between VMs and Hosts, where each key
         * is a VM id and the corresponding value is the id of the host where the VM is placed.
         */
	public Map<Integer, Integer> VmtoHostlist;

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
		Switchlist = new HashMap<>();
	}

	protected void processVmCreate(SimEvent ev, boolean ack) {
		super.processVmCreate(ev, ack);
		GuestEntity vm = (GuestEntity) ev.getData();

		if (vm.getHost() != null) {
			VmToSwitchid.put(vm.getId(), ((NetworkHost) vm.getHost()).sw.getId());
			VmtoHostlist.put(vm.getId(), vm.getHost().getId());
		}
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
		for (Entry<Integer, Switch> es : Switchlist.entrySet()) {
			if (es.getValue().level == NetworkTags.EDGE_LEVEL) {
				edgeswitch.put(es.getKey(), es.getValue());
			}
		}
		return edgeswitch;

	}
}
