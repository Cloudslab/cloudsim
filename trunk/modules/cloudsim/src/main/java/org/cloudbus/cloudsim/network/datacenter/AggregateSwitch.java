package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

/**
 * This class allows to simulate aggregate switch for Datacenter network.
 * It interacts with other switches in order to exchange
 * packets. Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel 
 * Applications in Cloud Simulations, Proceedings of the 4th IEEE/ACM International 
 * Conference on Utility and Cloud Computing (UCC 2011, IEEE CS Press, USA), 
 * Melbourne, Australia, December 5-7, 2011. 
 *
 * @author		Saurabh Kumar Garg
 * @since		CloudSim Toolkit 3.0
 * 
 */


public class AggregateSwitch extends Switch {

	
	/**
	 * Constructor for Aggregate  Switch
	 * We have to specify switches that are connected to its downlink and uplink ports,
	 * and corresponding bandwidths
	 *
	 * @param name Name of the switch 
	 * @param level At which level switch is with respect to hosts.
	 * @param dc  Pointer to Datacenter
	 */
	public AggregateSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		// TODO Auto-generated constructor stub

		downlinkswitchpktlist = new HashMap<Integer, List<NetworkPacket>>();
		uplinkswitchpktlist = new HashMap<Integer, List<NetworkPacket>>();
		uplinkbandwidth = NetworkConstants.BandWidthAggRoot;
		downlinkbandwidth = NetworkConstants.BandWidthEdgeAgg;
		latency = NetworkConstants.SwitchingDelayAgg;
		numport = NetworkConstants.AggSwitchPort;
		uplinkswitches = new ArrayList<Switch>();
		downlinkswitches = new ArrayList<Switch>();
	}
	
	/**
	 * Send Packet to switch connected through a downlink port
	 *
	 * @param ev Event/packet to process
	 */
	
	@Override
	protected void processpacket_down(SimEvent ev) {
		// packet coming from up level router.
		// has to send downward
		// check which switch to forward to
		// add packet in the switch list
		// add packet in the host list
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(
				CloudSimTags.Network_Event_send));
		schedule(getId(), this.latency, CloudSimTags.Network_Event_send);

		if (this.level == NetworkConstants.Agg_LEVEL) {
			// packet is coming from root so need to be sent to edgelevel swich
			// find the id for edgelevel switch
			int switchid = dc.VmToSwitchid.get(recvVMid);
			List<NetworkPacket> pktlist = this.downlinkswitchpktlist.get(switchid);
			if (pktlist == null) {
				pktlist = new ArrayList<NetworkPacket>();
				this.downlinkswitchpktlist.put(switchid, pktlist);
			}
			pktlist.add(hspkt);
			return;
		}

	}
	/**
	 * Send Packet to switch connected through a uplink port
	 *
	 * @param ev Event/packet to process
	 */
	@Override
	protected void processpacket_up(SimEvent ev) {
	
		// packet coming from down level router.
		// has to send up
		// check which switch to forward to
		// add packet in the switch list
		//
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(
				CloudSimTags.Network_Event_send));
		schedule(getId(), this.switching_delay, CloudSimTags.Network_Event_send);

		if (this.level == NetworkConstants.Agg_LEVEL) {
			// packet is coming from edge level router so need to be sent to
			// either root or another edge level swich
			// find the id for edgelevel switch
			int switchid = dc.VmToSwitchid.get(recvVMid);
			boolean flagtoswtich = false;
			for (Switch sw : this.downlinkswitches) {
				if (switchid == sw.getId())
					flagtoswtich = true;
			}
			if (flagtoswtich) {
				List<NetworkPacket> pktlist = this.downlinkswitchpktlist
						.get(switchid);
				if (pktlist == null) {
					pktlist = new ArrayList<NetworkPacket>();
					this.downlinkswitchpktlist.put(switchid, pktlist);
				}
				pktlist.add(hspkt);
			} else// send to up
			{
				Switch sw = this.uplinkswitches.get(0);
				List<NetworkPacket> pktlist = this.uplinkswitchpktlist.get(sw
						.getId());
				if (pktlist == null) {
					pktlist = new ArrayList<NetworkPacket>();
					this.uplinkswitchpktlist.put(sw.getId(), pktlist);
				}
				pktlist.add(hspkt);
			}
		}
	}

}
