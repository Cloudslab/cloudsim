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
 * @todo attributes should be private
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
         * @todo It doesn't appear to be used
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
         * @todo should be an enum
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

        /**
         * The latency of the network where the switch is connected to.
         * @todo Its value is being defined by a constant, but not every subclass
         * is setting the attribute accordingly.
         * The constants should be used as default values, but the class
         * should have a setter for this attribute.
         */
	public double latency;

	public double numport;

        /**
         * The datacenter where the switch is connected to.
         * @todo It doesn't appear to be used
         */
	public NetworkDatacenter dc;

	/** Something is running on these hosts. 
         * @todo The attribute is only used at the TestExample class. */
	public SortedMap<Double, List<NetworkHost>> fintimelistHost = new TreeMap<Double, List<NetworkHost>>();

	/** Something is running on these hosts. 
         * @todo The attribute doesn't appear to be used */
	public SortedMap<Double, List<NetworkVm>> fintimelistVM = new TreeMap<Double, List<NetworkVm>>();

        /**
         * List of  received packets.
         */
	public ArrayList<NetworkPacket> pktlist = new ArrayList<NetworkPacket>();

	/** 
         * @todo The attribute doesn't appear to be used */
	public List<Vm> BagofTaskVm = new ArrayList<Vm>();

        /**
         * The time the switch spends to process a received packet.
         * This time is considered constant no matter how many packets 
         * the switch have to process.
         * 
         * @todo The value of this attribute is being defined by
         * constants such as {@link NetworkConstants#SwitchingDelayRoot},
         * but not all sub classes are setting a value to it.
         * The constants should be used as default values, but the class
         * should have a setter for this attribute.
         */
	public double switching_delay;

        /**
         * A map of VMs connected to this switch.
         * @todo The list doesn't appear to be updated (VMs added to it) anywhere. 
         */
	public Map<Integer, NetworkVm> Vmlist = new HashMap<Integer, NetworkVm>();

	public Switch(String name, int level, NetworkDatacenter dc) {
		super(name);
		this.level = level;
		this.dc = dc;
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
		// Resource characteristics request
			case CloudSimTags.Network_Event_UP:
				// process the packet from down switch or host
				processpacket_up(ev);
				break;
			case CloudSimTags.Network_Event_DOWN:
				// process the packet from uplink
				processpacket_down(ev);
				break;
			case CloudSimTags.Network_Event_send:
				processpacketforward(ev);
				break;

			case CloudSimTags.Network_Event_Host:
				processhostpacket(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_Register:
				registerHost(ev);
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

        /**
         * Process a packet sent to a host.
         * @param ev The packet sent.
         */
	protected void processhostpacket(SimEvent ev) {
		// Send packet to host
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		NetworkHost hs = hostlist.get(hspkt.recieverhostid);
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
		int recvVMid = hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), latency, CloudSimTags.Network_Event_send);
		if (level == NetworkConstants.EDGE_LEVEL) {
			// packet is to be recieved by host
			int hostid = dc.VmtoHostlist.get(recvVMid);
			hspkt.recieverhostid = hostid;
			List<NetworkPacket> pktlist = packetTohost.get(hostid);
			if (pktlist == null) {
				pktlist = new ArrayList<NetworkPacket>();
				packetTohost.put(hostid, pktlist);
			}
			pktlist.add(hspkt);
			return;
		}
		if (level == NetworkConstants.Agg_LEVEL) {
			// packet is coming from root so need to be sent to edgelevel swich
			// find the id for edgelevel switch
			int switchid = dc.VmToSwitchid.get(recvVMid);
			List<NetworkPacket> pktlist = downlinkswitchpktlist.get(switchid);
			if (pktlist == null) {
				pktlist = new ArrayList<NetworkPacket>();
				downlinkswitchpktlist.put(switchid, pktlist);
			}
			pktlist.add(hspkt);
			return;
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
		int recvVMid = hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_send);
		if (level == NetworkConstants.EDGE_LEVEL) {
			// packet is recieved from host
			// packet is to be sent to aggregate level or to another host in the
			// same level

			int hostid = dc.VmtoHostlist.get(recvVMid);
			NetworkHost hs = hostlist.get(hostid);
			hspkt.recieverhostid = hostid;
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
			// packet is to be sent to upper switch
			// ASSUMPTION EACH EDGE is Connected to one aggregate level switch

			Switch sw = uplinkswitches.get(0);
			List<NetworkPacket> pktlist = uplinkswitchpktlist.get(sw.getId());
			if (pktlist == null) {
				pktlist = new ArrayList<NetworkPacket>();
				uplinkswitchpktlist.put(sw.getId(), pktlist);
			}
			pktlist.add(hspkt);
			return;
		}
		if (level == NetworkConstants.Agg_LEVEL) {
			// packet is coming from edge level router so need to be sent to
			// either root or another edge level swich
			// find the id for edgelevel switch
			int switchid = dc.VmToSwitchid.get(recvVMid);
			boolean flagtoswtich = false;
			for (Switch sw : downlinkswitches) {
				if (switchid == sw.getId()) {
					flagtoswtich = true;
				}
			}
			if (flagtoswtich) {
				List<NetworkPacket> pktlist = downlinkswitchpktlist.get(switchid);
				if (pktlist == null) {
					pktlist = new ArrayList<NetworkPacket>();
					downlinkswitchpktlist.put(switchid, pktlist);
				}
				pktlist.add(hspkt);
			} else// send to up
			{
				Switch sw = uplinkswitches.get(0);
				List<NetworkPacket> pktlist = uplinkswitchpktlist.get(sw.getId());
				if (pktlist == null) {
					pktlist = new ArrayList<NetworkPacket>();
					uplinkswitchpktlist.put(sw.getId(), pktlist);
				}
				pktlist.add(hspkt);
			}
		}
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
         * @todo the method should be protected to allow sub classes to override it,
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
					Iterator<NetworkPacket> it = hspktlist.iterator();
					while (it.hasNext()) {
						NetworkPacket hspkt = it.next();
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

        /**
         * Gets the host of a given VM.
         * @param vmid The id of the VM
         * @return the host of the VM
         */
	protected NetworkHost getHostwithVM(int vmid) {
		for (Entry<Integer, NetworkHost> es : hostlist.entrySet()) {
			Vm vm = VmList.getById(es.getValue().getVmList(), vmid);
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
         */
	protected List<NetworkVm> getfreeVmlist(int numVMReq) {
		List<NetworkVm> freehostls = new ArrayList<NetworkVm>();
		for (Entry<Integer, NetworkVm> et : Vmlist.entrySet()) {
			if (et.getValue().isFree()) {
				freehostls.add(et.getValue());
			}
			if (freehostls.size() == numVMReq) {
				break;
			}
		}

		return freehostls;
	}

        /**
         * Gets a list with a given number of free hosts.
         * 
         * @param numhost The number of free hosts to get.
         * @return the list of free hosts.
         */
	protected List<NetworkHost> getfreehostlist(int numhost) {
		List<NetworkHost> freehostls = new ArrayList<NetworkHost>();
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
