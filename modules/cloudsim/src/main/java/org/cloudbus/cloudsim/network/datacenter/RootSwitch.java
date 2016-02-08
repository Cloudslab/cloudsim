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

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

/**
 * This class allows to simulate Root switch which connects Datacenters to external network. 
 * It interacts with other switches in order to exchange packets.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class RootSwitch extends Switch {

	/**
	 * Instantiates a Root Switch specifying what other switches are connected to its downlink
	 * ports, and corresponding bandwidths.
	 * 
	 * @param name Name of the root switch
	 * @param level At which level the switch is with respect to hosts.
	 * @param dc The Datacenter where the switch is connected to
	 */
	public RootSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		downlinkswitchpktlist = new HashMap<Integer, List<NetworkPacket>>();
		downlinkswitches = new ArrayList<Switch>();

		downlinkbandwidth = NetworkConstants.BandWidthAggRoot;
		latency = NetworkConstants.SwitchingDelayRoot;
		numport = NetworkConstants.RootSwitchPort;
	}

	@Override
	protected void processpacket_up(SimEvent ev) {

		// packet coming from down level router.
		// has to send up
		// check which switch to forward to
		// add packet in the switch list

		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);

		if (level == NetworkConstants.ROOT_LEVEL) {
			// get id of edge router
			int edgeswitchid = dc.VmToSwitchid.get(recvVMid);
			// search which aggregate switch has it
			int aggSwtichid = -1;
			;
			for (Switch sw : downlinkswitches) {
				for (Switch edge : sw.downlinkswitches) {
					if (edge.getId() == edgeswitchid) {
						aggSwtichid = sw.getId();
						break;
					}
				}
			}
			if (aggSwtichid < 0) {
				System.out.println(" No destination for this packet");
			} else {
				List<NetworkPacket> pktlist = downlinkswitchpktlist.get(aggSwtichid);
				if (pktlist == null) {
					pktlist = new ArrayList<NetworkPacket>();
					downlinkswitchpktlist.put(aggSwtichid, pktlist);
				}
				pktlist.add(hspkt);
			}
		}
	}
}
