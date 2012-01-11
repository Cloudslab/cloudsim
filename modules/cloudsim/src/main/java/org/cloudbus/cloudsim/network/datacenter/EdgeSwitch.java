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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

/**
 * This class allows to simulate Edge switch for Datacenter network. It interacts with other
 * switches in order to exchange packets.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class EdgeSwitch extends Switch {

	/**
	 * Constructor for Edge Switch We have to specify switches that are connected to its downlink
	 * and uplink ports, and corresponding bandwidths. In this switch downlink ports are connected
	 * to hosts not to a switch.
	 * 
	 * @param name Name of the switch
	 * @param level At which level switch is with respect to hosts.
	 * @param dc Pointer to Datacenter
	 */
	public EdgeSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		hostlist = new HashMap<Integer, NetworkHost>();
		uplinkswitchpktlist = new HashMap<Integer, List<NetworkPacket>>();
		packetTohost = new HashMap<Integer, List<NetworkPacket>>();
		uplinkbandwidth = NetworkConstants.BandWidthEdgeAgg;
		downlinkbandwidth = NetworkConstants.BandWidthEdgeHost;
		switching_delay = NetworkConstants.SwitchingDelayEdge;
		numport = NetworkConstants.EdgeSwitchPort;
		uplinkswitches = new ArrayList<Switch>();
	}

	/**
	 * Send Packet to switch connected through a uplink port
	 * 
	 * @param ev Event/packet to process
	 */
	@Override
	protected void processpacket_up(SimEvent ev) {
		// packet coming from down level router/host.
		// has to send up
		// check which switch to forward to
		// add packet in the switch list
		//
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);

		// packet is recieved from host
		// packet is to be sent to aggregate level or to another host in the same level

		int hostid = dc.VmtoHostlist.get(recvVMid);
		NetworkHost hs = hostlist.get(hostid);
		hspkt.recieverhostid = hostid;

		// packet needs to go to a host which is connected directly to switch
		if (hs != null) {
			// packet to be sent to host connected to the switch
			List<NetworkPacket> pktlist = packetTohost.get(hostid);
			if (pktlist == null) {
				pktlist = new ArrayList<NetworkPacket>();
				packetTohost.put(hostid, pktlist);
			}
			pktlist.add(hspkt);
			return;

		}
		// otherwise
		// packet is to be sent to upper switch
		// ASSUMPTION EACH EDGE is Connected to one aggregate level switch
		// if there are more than one Aggregate level switch one need to modify following code

		Switch sw = uplinkswitches.get(0);
		List<NetworkPacket> pktlist = uplinkswitchpktlist.get(sw.getId());
		if (pktlist == null) {
			pktlist = new ArrayList<NetworkPacket>();
			uplinkswitchpktlist.put(sw.getId(), pktlist);
		}
		pktlist.add(hspkt);
		return;

	}

	/**
	 * Send Packet to hosts connected to the switch
	 * 
	 * @param ev Event/packet to process
	 */
	@Override
	protected void processpacketforward(SimEvent ev) {
		// search for the host and packets..send to them

		if (uplinkswitchpktlist != null) {
			for (Entry<Integer, List<NetworkPacket>> es : uplinkswitchpktlist.entrySet()) {
				int tosend = es.getKey();
				List<NetworkPacket> hspktlist = es.getValue();
				if (!hspktlist.isEmpty()) {
					// sharing bandwidth between packets
					double avband = uplinkbandwidth / hspktlist.size();
					Iterator<NetworkPacket> it = hspktlist.iterator();
					while (it.hasNext()) {
						NetworkPacket hspkt = it.next();
						double delay = 1000 * hspkt.pkt.data / avband;

						this.send(tosend, delay, CloudSimTags.Network_Event_UP, hspkt);
					}
					hspktlist.clear();
				}
			}
		}
		if (packetTohost != null) {
			for (Entry<Integer, List<NetworkPacket>> es : packetTohost.entrySet()) {
				List<NetworkPacket> hspktlist = es.getValue();
				if (!hspktlist.isEmpty()) {
					double avband = downlinkbandwidth / hspktlist.size();
					Iterator<NetworkPacket> it = hspktlist.iterator();
					while (it.hasNext()) {
						NetworkPacket hspkt = it.next();
						// hspkt.recieverhostid=tosend;
						// hs.packetrecieved.add(hspkt);
						this.send(getId(), hspkt.pkt.data / avband, CloudSimTags.Network_Event_Host, hspkt);
					}
					hspktlist.clear();
				}
			}
		}

		// or to switch at next level.
		// clear the list

	}

}
