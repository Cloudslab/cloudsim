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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * This class represents a Network Switch in a Datacenter network.
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
 * @author Remo Andreoli
 * @since CloudSim Toolkit 3.0
 */
public class Switch extends SimEntity {
	/** Switch level in datacenter topology.
	 * -) Root switch connects the Datacenter to external network.
	 * -) Aggregate switches reside in-between the root switch and the edge switches.
	 * -) Edge switches have their downlink ports connected to hosts.
	 *
	 * Note that the order of the values within the enum is significant
	 */
	public enum SwitchLevel {
		EDGE_LEVEL,
		AGGR_LEVEL,
		ROOT_LEVEL;
	}

	/** The switch id */
	public int id;

        /**
         * The level (layer) of the switch in the network topology.
         */
	public SwitchLevel level;

        /**
         * Map of packets sent to switches on the uplink,
         * where each key is a switch id and the corresponding
         * value is the packets sent to that switch.
         */
	public Map<Integer, List<NetworkPacket>> pktsToUplinkSwitches;

        /**
         * Map of packets sent to switches on the downlink,
         * where each key is a switch id and the corresponding
         * value is the packets sent to that switch.
         */
	public Map<Integer, List<NetworkPacket>> pktsToDownlinkSwitches;

	/**
	 * Map of packets sent to hosts connected in the switch,
	 * where each key is a host id and the corresponding
	 * value is the packets sent to that host.
	 */
	public Map<Integer, List<NetworkPacket>> pktsToHosts;

        /**
         * List of uplink switches.
         */
	public List<Switch> uplinkSwitches;

        /**
         * List of downlink switches.
         */
	public List<Switch> downlinkSwitches;

	/**
	 * Map of hosts connected to the switch, where each key is the host ID
	 * and the corresponding value is the host itself.
	 */
	public Map<Integer, NetworkHost> hostList;

	/**
         * Bandwitdh of uplink (Bytes/sec).
         */
	public double uplinkBw;

        /**
         * Bandwitdh of downlink (Bytes/sec).
         */
	public double downlinkBw;

	public double numPort;

	/**
	 * The datacenter where the switch is connected to.
	 */
	public NetworkDatacenter dc;

	/** Something is running on these hosts. 
         * //TODO The attribute is only used at the TestExample class. */
	public SortedMap<Double, List<NetworkHost>> fintimelistHost = new TreeMap<>();

	/** Something is running on these hosts. 
         * //TODO The attribute doesn't appear to be used */
	//public SortedMap<Double, List<Vm>> fintimelistVM = new TreeMap<>();

        /**
         * List of  received packets.
         */
	//public ArrayList<NetworkPacket> pktlist = new ArrayList<>();

        /**
         * The time the switch spends to process a received packet.
         * This time is considered constant no matter how many packets 
         * the switch have to process.
         *
         */
	public double switchingDelay;

	public Switch(String name, double numPort, SwitchLevel level, double switchingDelay, double downlinkBw, double uplinkBw, NetworkDatacenter dc) {
		super(name);
		this.level = level;
		this.numPort = numPort;

		this.switchingDelay = switchingDelay;
		this.downlinkBw = downlinkBw;
		this.uplinkBw = uplinkBw;

		this.dc = dc;

		hostList = new HashMap<>();
		pktsToHosts = new HashMap<>();

		pktsToDownlinkSwitches = new HashMap<>();
		pktsToUplinkSwitches = new HashMap<>();

		downlinkSwitches = new ArrayList<>();
		uplinkSwitches = new ArrayList<>();
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		//schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	@Override
	public void processEvent(SimEvent ev) {
		// Log.printLine(CloudSim.clock()+"[Broker]: event received:"+ev.getTag());
		switch (ev.getTag()) {
			// process the packet from down switch or host
			case CloudSimTags.NETWORK_PKT_UP -> processPacketUp(ev);

			// process the packet from uplink
			case CloudSimTags.NETWORK_PKT_DOWN -> processPacketDown(ev);

			// forward packet to an host
			case CloudSimTags.NETWORK_PKT_FORWARD -> processPacketForward(ev);

			// Store packet in host
			case CloudSimTags.NETWORK_PKT_REACHED -> processHostPacket(ev);

			// Resource characteristics answer (@TODO: Remo Andreoli: not in use)
			case CloudSimTags.NETWORK_ATTACH_HOST -> registerHost(ev);

			// other unknown tags are processed by this method
			default -> processOtherEvent(ev);
		}
	}

        /**
         * Process a packet sent to a host.
         * @param ev The packet sent.
         */
	protected void processHostPacket(SimEvent ev) {
		// Send packet to host
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		NetworkHost hs = hostList.get(hspkt.receiverHostId);
		hs.pktReceived.add(hspkt);
	}

	/**
	 * Sends a packet to switches connected through a downlink port.
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processPacketDown(SimEvent ev) {
		// packet coming from up level router
		// has to send downward.
		// check which switch to forward to
		// add packet in the switch list
		// add packet in the host list
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.receiverVmId;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.NETWORK_PKT_FORWARD));
		schedule(getId(), switchingDelay, CloudSimTags.NETWORK_PKT_FORWARD);

		if (level == SwitchLevel.EDGE_LEVEL) {
			// packet is to be recieved by host
			int hostid = dc.VmtoHostlist.get(recvVMid);
			hspkt.receiverHostId = hostid;
			pktsToHosts.computeIfAbsent(hostid, k -> new ArrayList<>()).add(hspkt);;
		} else if (level == SwitchLevel.AGGR_LEVEL) {
			// packet is coming from root so need to be sent to edgelevel swich
			// find the id for edgelevel switch
			int switchId = dc.VmToSwitchid.get(recvVMid);
			pktsToDownlinkSwitches.computeIfAbsent(switchId, k -> new ArrayList<>()).add(hspkt);
		}

	}

	/**
	 * Sends a packet to switches connected through a uplink port.
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processPacketUp(SimEvent ev) {
		// packet coming from down level router.
		// has to be sent up.
		// check which switch to forward to
		// add packet in the switch list
		//
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.receiverVmId;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.NETWORK_PKT_FORWARD));
		schedule(getId(), switchingDelay, CloudSimTags.NETWORK_PKT_FORWARD);

		if (level == SwitchLevel.EDGE_LEVEL) {
			// packet is received from host
			// packet is to be sent to aggregate level or to another host in the
			// same level

			int hostid = dc.VmtoHostlist.get(recvVMid);
			NetworkHost hs = hostList.get(hostid);
			hspkt.receiverHostId = hostid;
			if (hs != null) {
				// packet to be sent to host directly connected to the switch
				pktsToHosts.computeIfAbsent(hostid, k -> new ArrayList<>()).add(hspkt);
				return;
			}
			// packet is to be sent to upper switch
			// ASSUMPTION: EACH EDGE is Connected to one aggregate level switch

			Switch sw = uplinkSwitches.get(0);
			pktsToUplinkSwitches.computeIfAbsent(sw.getId(), k -> new ArrayList<>()).add(hspkt);
		}
		else if (level == SwitchLevel.AGGR_LEVEL) {
			// packet is coming from edge level router so need to be sent to
			// either root or another edge level swich
			// find the id for edgelevel switch
			int switchId = dc.VmToSwitchid.get(recvVMid);

            if (downlinkSwitches.stream().anyMatch(sw -> sw.getId() == switchId)) {
				pktsToDownlinkSwitches.computeIfAbsent(switchId, k -> new ArrayList<>()).add(hspkt);
			} else {// send to up (ASSUMPTION: EACH EDGE is Connected to one aggregate level switch)
				Switch sw = uplinkSwitches.get(0);
				pktsToUplinkSwitches.computeIfAbsent(sw.getId(), k -> new ArrayList<>()).add(hspkt);
			}
		}
		else if (level == SwitchLevel.ROOT_LEVEL) {
			// get id of edge router
			int edgeswitchid = dc.VmToSwitchid.get(recvVMid);
			// search which aggregate switch has it
			int aggSwtichid = -1;
            for (Switch sw : downlinkSwitches) {
				for (Switch edge : sw.downlinkSwitches) {
					if (edge.getId() == edgeswitchid) {
						aggSwtichid = sw.getId();
						break;
					}
				}
			}
			if (aggSwtichid < 0) {
				System.out.println(" No destination for this packet");
			} else {
				pktsToDownlinkSwitches.computeIfAbsent(aggSwtichid, k -> new ArrayList<>()).add(hspkt);
			}
		} else {
			throw new IllegalStateException("Unknown switch level " + level);
		}
	}
        
        /**
         * Register a host that is connected to the switch.
         * @param ev
		 *
         */
	private void registerHost(SimEvent ev) {
		NetworkHost hs = (NetworkHost) ev.getData();
		hostList.put(hs.getId(), hs);
	}

        /**
         * Process a received packet.
         * @param ev The packet received.
         */
	/*protected void processpacket(SimEvent ev) {
		// send packet to itself with switching delay (discarding other)
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.NETWORK_PKT_UP));
		schedule(getId(), switchingDelay, CloudSimTags.NETWORK_PKT_UP);
		pktlist.add((NetworkPacket) ev.getData());
	}*/

        /**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
         *
         */
	private void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**
	 * Sends a packet to hosts connected to the switch
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processPacketForward(SimEvent ev) {
		// search for the host and packets..send to them
		for (Entry<Integer, List<NetworkPacket>> es : pktsToDownlinkSwitches.entrySet()) {
			int tosend = es.getKey();
			List<NetworkPacket> hspktlist = es.getValue();
			if (!hspktlist.isEmpty()) {
				double avband = downlinkBw / hspktlist.size();
				for (NetworkPacket hspkt : hspktlist) {
					double delay = 1000 * hspkt.pkt.data / avband;

					this.send(tosend, delay, CloudSimTags.NETWORK_PKT_DOWN, hspkt);
				}
				hspktlist.clear();
			}
		}

		for (Entry<Integer, List<NetworkPacket>> es : pktsToUplinkSwitches.entrySet()) {
			int tosend = es.getKey();
			List<NetworkPacket> hspktlist = es.getValue();
			if (!hspktlist.isEmpty()) {
				// sharing bandwidth between packets
				double avband = uplinkBw / hspktlist.size();
				for (NetworkPacket hspkt : hspktlist) {
					double delay = 1000 * hspkt.pkt.data / avband;

					this.send(tosend, delay, CloudSimTags.NETWORK_PKT_UP, hspkt);
				}
				hspktlist.clear();
			}
		}

		for (Entry<Integer, List<NetworkPacket>> es : pktsToHosts.entrySet()) {
			List<NetworkPacket> hspktlist = es.getValue();
			if (!hspktlist.isEmpty()) {
				double avband = downlinkBw / hspktlist.size();
				for (NetworkPacket hspkt : hspktlist) {
					double delay = 1000 * hspkt.pkt.data / avband;
					this.send(getId(), delay, CloudSimTags.NETWORK_PKT_REACHED, hspkt);
				}
				hspktlist.clear();
			}
		}

		// or to switch at next level.
		// clear the list

	}

        /**
         * Gets the host of a given VM.
         * @param vmid The id of the VM
         * @return the host of the VM
         */
	protected NetworkHost getHostwithVM(int vmid) {
		for (Entry<Integer, NetworkHost> es : hostList.entrySet()) {
			Vm vm = VmList.getById(es.getValue().getGuestList(), vmid);
			if (vm != null) {
				return es.getValue();
			}
		}
		return null;
	}

        /**
         * Gets a list with a given number of free VMs.
         * 
         * @param numVMReq The number of free VMs to get.
         * @return the list of free VMs.
		 *
		 * TODO: Remo Andreoli: This is never used, remove?
         */
	/*protected List<Vm> getfreeVmlist(int numVMReq) {
		List<Vm> freehostls = new ArrayList<>();
		for (Entry<Integer, Vm> et : Vmlist.entrySet()) {
			if (et.getValue().isFree()) {
				freehostls.add(et.getValue());
			}
			if (freehostls.size() == numVMReq) {
				break;
			}
		}

		return freehostls;
	}*/

        /**
         * Gets a list with a given number of free hosts.
         * 
         * @param numhost The number of free hosts to get.
         * @return the list of free hosts.
         */
	protected List<NetworkHost> getfreehostlist(int numhost) {
		List<NetworkHost> freehostls = new ArrayList<>();
		for (Entry<Integer, NetworkHost> et : hostList.entrySet()) {
			if (et.getValue().getNumberOfFreePes() == et.getValue().getNumberOfPes()) {
				freehostls.add(et.getValue());
			}
			if (freehostls.size() == numhost) {
				break;
			}
		}

		return freehostls;
	}

	@Override
	public void shutdownEntity() {
		Log.printConcatLine(CloudSim.clock(), ": ", getName(), " is shutting down...");
	}

}
