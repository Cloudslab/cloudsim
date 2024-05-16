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
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * Represents a Network Switch.
 * //TODO attributes should be private
 */
public class Switch extends SimEntity {

	/** The switch id */
	public int id;

        /**
         * The level (layer) of the switch in the network topology.
         */
	public int level;

        /**
         * The id of the datacenter where the switch is connected to.
         * //TODO It doesn't appear to be used
         */
	public int datacenterid;

        /**
         * Map of packets sent to switches on the uplink,
         * where each key is a switch id and the corresponding
         * value is the packets sent to that switch.
         */
	public Map<Integer, List<NetworkPacket>> uplinkswitchpktlist;

        /**
         * Map of packets sent to switches on the downlink,
         * where each key is a switch id and the corresponding
         * value is the packets sent to that switch.
         */
	public Map<Integer, List<NetworkPacket>> downlinkswitchpktlist;

        /**
         * Map of hosts connected to the switch, where each key is the host ID
         * and the corresponding value is the host itself.
         */
	public Map<Integer, NetworkHost> hostlist;

        /**
         * List of uplink switches.
         */
	public List<Switch> uplinkswitches;

        /**
         * List of downlink switches.
         */
	public List<Switch> downlinkswitches;

        /**
         * Map of packets sent to hosts connected in the switch,
         * where each key is a host id and the corresponding
         * value is the packets sent to that host.
         */
	public Map<Integer, List<NetworkPacket>> packetTohost;

        /**
         * The switch type: edge switch or aggregation switch.
         * //TODO should be an enum
         */
	int type;

        /**
         * Bandwitdh of uplink.
         */
	public double uplinkbandwidth;

        /**
         * Bandwitdh of downlink.
         */
	public double downlinkbandwidth;

	public double numport;

        /**
         * The datacenter where the switch is connected to.
         * //TODO It doesn't appear to be used
         */
	public NetworkDatacenter dc;

	/** Something is running on these hosts. 
         * //TODO The attribute is only used at the TestExample class. */
	public SortedMap<Double, List<NetworkHost>> fintimelistHost = new TreeMap<>();

	/** Something is running on these hosts. 
         * //TODO The attribute doesn't appear to be used */
	public SortedMap<Double, List<Vm>> fintimelistVM = new TreeMap<>();

        /**
         * List of  received packets.
         */
	public ArrayList<NetworkPacket> pktlist = new ArrayList<>();

	/** 
         * //TODO The attribute doesn't appear to be used */
	public List<Vm> BagofTaskVm = new ArrayList<>();

        /**
         * The time the switch spends to process a received packet.
         * This time is considered constant no matter how many packets 
         * the switch have to process.
         *
         */
	public double switching_delay;

        /**
         * A map of VMs connected to this switch.
         * //TODO The list doesn't appear to be updated (VMs added to it) anywhere.
         */
	public Map<Integer, Vm> Vmlist = new HashMap<>();

	public Switch(String name, double numport, int level, double switching_delay, double downlinkbandwidth, double uplinkbandwidth, NetworkDatacenter dc) {
		super(name);
		this.level = level;
		this.numport = numport;

		this.switching_delay = switching_delay;
		this.downlinkbandwidth = downlinkbandwidth;
		this.uplinkbandwidth = uplinkbandwidth;

		this.dc = dc;

		hostlist = new HashMap<>();
		packetTohost = new HashMap<>();

		downlinkswitchpktlist = new HashMap<>();
		uplinkswitchpktlist = new HashMap<>();

		downlinkswitches = new ArrayList<>();
		uplinkswitches = new ArrayList<>();
	}

	@Override
	public void startEntity() {
		Log.printConcatLine(getName(), " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	@Override
	public void processEvent(SimEvent ev) {
		// Log.printLine(CloudSim.clock()+"[Broker]: event received:"+ev.getTag());
		switch (ev.getTag()) {
			// process the packet from down switch or host
			case CloudSimTags.Network_Event_UP -> processpacket_up(ev);

			// process the packet from uplink
			case CloudSimTags.Network_Event_DOWN -> processpacket_down(ev);

			case CloudSimTags.Network_Event_send -> processpacketforward(ev);
			case CloudSimTags.Network_Event_Host -> processhostpacket(ev);

			// Resource characteristics answer
			case CloudSimTags.RESOURCE_Register -> registerHost(ev);

			// other unknown tags are processed by this method
			default -> processOtherEvent(ev);
		}
	}

        /**
         * Process a packet sent to a host.
         * @param ev The packet sent.
         */
	protected void processhostpacket(SimEvent ev) {
		// Send packet to host
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		NetworkHost hs = hostlist.get(hspkt.receiverHostId);
		hs.packetrecieved.add(hspkt);
	}

	/**
	 * Sends a packet to switches connected through a downlink port.
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processpacket_down(SimEvent ev) {
		// packet coming from up level router
		// has to send downward.
		// check which switch to forward to
		// add packet in the switch list
		// add packet in the host list
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.receiverVmId;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);
		if (level == NetworkTags.EDGE_LEVEL) {
			// packet is to be recieved by host
			int hostid = dc.VmtoHostlist.get(recvVMid);
			hspkt.receiverHostId = hostid;
			List<NetworkPacket> pktlist = packetTohost.computeIfAbsent(hostid, k -> new ArrayList<>());
			pktlist.add(hspkt);
			return;
		}
		if (level == NetworkTags.AGGR_LEVEL) {
			// packet is coming from root so need to be sent to edgelevel swich
			// find the id for edgelevel switch
			int switchid = dc.VmToSwitchid.get(recvVMid);
			List<NetworkPacket> pktlist = downlinkswitchpktlist.computeIfAbsent(switchid, k -> new ArrayList<>());
			pktlist.add(hspkt);
		}

	}

	/**
	 * Sends a packet to switches connected through a uplink port.
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processpacket_up(SimEvent ev) {
		// packet coming from down level router.
		// has to be sent up.
		// check which switch to forward to
		// add packet in the switch list
		//
		// int src=ev.getSource();
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		int recvVMid = hspkt.pkt.receiverVmId;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);

		if (level == NetworkTags.EDGE_LEVEL) {
			// packet is received from host
			// packet is to be sent to aggregate level or to another host in the
			// same level

			int hostid = dc.VmtoHostlist.get(recvVMid);
			NetworkHost hs = hostlist.get(hostid);
			hspkt.receiverHostId = hostid;
			if (hs != null) {
				// packet to be sent to host connected to the switch
				List<NetworkPacket> pktlist = packetTohost.computeIfAbsent(hostid, k -> new ArrayList<>());
				pktlist.add(hspkt);
				return;

			}
			// packet is to be sent to upper switch
			// ASSUMPTION EACH EDGE is Connected to one aggregate level switch

			Switch sw = uplinkswitches.get(0);
			List<NetworkPacket> pktlist = uplinkswitchpktlist.computeIfAbsent(sw.getId(), k -> new ArrayList<>());
			pktlist.add(hspkt);
			return;
		}
		else if (level == NetworkTags.AGGR_LEVEL) {
			// packet is coming from edge level router so need to be sent to
			// either root or another edge level swich
			// find the id for edgelevel switch
			int switchid = dc.VmToSwitchid.get(recvVMid);
			boolean flagtoswtich = false;
			for (Switch sw : downlinkswitches) {
				if (switchid == sw.getId()) {
					flagtoswtich = true;
					break;
				}
			}
			if (flagtoswtich) {
				List<NetworkPacket> pktlist = downlinkswitchpktlist.computeIfAbsent(switchid, k -> new ArrayList<>());
				pktlist.add(hspkt);
			} else// send to up
			{
				Switch sw = uplinkswitches.get(0);
				List<NetworkPacket> pktlist = uplinkswitchpktlist.computeIfAbsent(sw.getId(), k -> new ArrayList<>());
				pktlist.add(hspkt);
			}
		}
		else if (level == NetworkTags.ROOT_LEVEL) {
			// get id of edge router
			int edgeswitchid = dc.VmToSwitchid.get(recvVMid);
			// search which aggregate switch has it
			int aggSwtichid = -1;
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
				List<NetworkPacket> pktlist = downlinkswitchpktlist.computeIfAbsent(aggSwtichid, k -> new ArrayList<>());
				pktlist.add(hspkt);
			}
		}

		// TODO: Remo Andreoli: Error?
	}
        
        /**
         * Register a host that is connected to the switch.
         * @param ev 
         */
	private void registerHost(SimEvent ev) {
		NetworkHost hs = (NetworkHost) ev.getData();
		hostlist.put(hs.getId(), hs);
	}

        /**
         * Process a received packet.
         * @param ev The packet received.
         */
	protected void processpacket(SimEvent ev) {
		// send packet to itself with switching delay (discarding other)
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_UP));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_UP);
		pktlist.add((NetworkPacket) ev.getData());

		// add the packet in the list

	}

        /**
	 * Process non-default received events that aren't processed by
         * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
         * This method should be overridden by subclasses in other to process
         * new defined events.
         *  
         * //TODO the method should be protected to allow sub classes to override it,
         * once it does nothing here.
         */
	private void processOtherEvent(SimEvent ev) {

	}

	/**
	 * Sends a packet to hosts connected to the switch
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processpacketforward(SimEvent ev) {
		// search for the host and packets..send to them
		if (downlinkswitchpktlist != null) {
			for (Entry<Integer, List<NetworkPacket>> es : downlinkswitchpktlist.entrySet()) {
				int tosend = es.getKey();
				List<NetworkPacket> hspktlist = es.getValue();
				if (!hspktlist.isEmpty()) {
					double avband = downlinkbandwidth / hspktlist.size();
					for (NetworkPacket hspkt : hspktlist) {
						double delay = 1000 * hspkt.pkt.data / avband;

						this.send(tosend, delay, CloudSimTags.Network_Event_DOWN, hspkt);
					}
					hspktlist.clear();
				}
			}
		}
		if (uplinkswitchpktlist != null) {
			for (Entry<Integer, List<NetworkPacket>> es : uplinkswitchpktlist.entrySet()) {
				int tosend = es.getKey();
				List<NetworkPacket> hspktlist = es.getValue();
				if (!hspktlist.isEmpty()) {
					double avband = uplinkbandwidth / hspktlist.size();
					for (NetworkPacket hspkt : hspktlist) {
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
					for (NetworkPacket hspkt : hspktlist) {
						// hspkt.receiverHostId=tosend;
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

        /**
         * Gets the host of a given VM.
         * @param vmid The id of the VM
         * @return the host of the VM
         */
	protected NetworkHost getHostwithVM(int vmid) {
		for (Entry<Integer, NetworkHost> es : hostlist.entrySet()) {
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
		for (Entry<Integer, NetworkHost> et : hostlist.entrySet()) {
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
		Log.printConcatLine(getName(), " is shutting down...");
	}

}
