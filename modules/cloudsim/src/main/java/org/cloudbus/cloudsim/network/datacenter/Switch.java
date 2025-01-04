/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

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
	public long uplinkBw;

        /**
         * Bandwitdh of downlink (Bytes/sec).
         */
	public long downlinkBw;

	public double numPort;

	/**
	 * The datacenter where the switch is connected to.
	 */
	public NetworkDatacenter dc;

        /**
         * The time the switch spends to process a received packet.
         * This time is considered constant no matter how many packets 
         * the switch have to process.
         *
         */
	public double switchingDelay;

	public Switch(String name, double numPort, SwitchLevel level, double switchingDelay, long downlinkBw, long uplinkBw, NetworkDatacenter dc) {
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
		super.startEntity();
		//schedule(getId(), 0, CloudActionTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	@Override
	public void processEvent(SimEvent ev) {
		CloudSimTags tag = ev.getTag();

        if (tag == CloudActionTags.NETWORK_PKT_UP) {
            processPacketUp(ev);
        } else if (tag == CloudActionTags.NETWORK_PKT_DOWN) {
            processPacketDown(ev);
        } else if (tag == CloudActionTags.NETWORK_PKT_FORWARD) {
            forwardProcessedPackets();
        } else if (tag == CloudActionTags.NETWORK_PKT_REACHED_HOST) {
            storePacketInHost(ev);
        } else if (tag == CloudActionTags.NETWORK_ATTACH_HOST) {
            registerHost(ev);
        } else { // other unknown tags are processed by this method
            processOtherEvent(ev);
        }
	}

        /**
         * Store a processed packet in the receiver host.
         * @param ev The packet sent.
         */
	protected void storePacketInHost(SimEvent ev) {
		NetworkPacket npkt = (NetworkPacket) ev.getData();

		NetworkHost hs = hostList.get(npkt.receiverHostId);
		NetworkInterfaceCard nic = hs.getNics().get(npkt.pkt.receiverCloudletId);

		nic.getReceivedPkts().add(npkt.pkt);
	}

	/**
	 * Process packet a packet coming from uplink port, and to be sent to
	 * switches connected through a downlink port.
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processPacketDown(SimEvent ev) {
		NetworkPacket npkt = (NetworkPacket) ev.getData();
		int recvVMid = npkt.pkt.receiverGuestId;
		CloudSim.cancelAll(getId(), new PredicateType(CloudActionTags.NETWORK_PKT_FORWARD));
		schedule(getId(), switchingDelay, CloudActionTags.NETWORK_PKT_FORWARD);

		// packet is to be received by the host
		if (level == SwitchLevel.EDGE_LEVEL) {
			int hostid = dc.VmtoHostlist.get(recvVMid);
			npkt.receiverHostId = hostid;
			pktsToHosts.computeIfAbsent(hostid, k -> new ArrayList<>()).add(npkt);
		} else if (level == SwitchLevel.AGGR_LEVEL) { // From root level to edge level
			// find the id for edgelevel switch
			int switchId = dc.VmToSwitchid.get(recvVMid);
			pktsToDownlinkSwitches.computeIfAbsent(switchId, k -> new ArrayList<>()).add(npkt);
		}

	}

	/**
	 * Process a packet coming from a downlink port, and to be sent to
	 * switches connected through a uplink port.
	 * 
	 * @param ev Event/packet to process
	 */
	protected void processPacketUp(SimEvent ev) {
		NetworkPacket npkt = (NetworkPacket) ev.getData();
		int recvVMid = npkt.pkt.receiverGuestId;

		CloudSim.cancelAll(getId(), new PredicateType(CloudActionTags.NETWORK_PKT_FORWARD));
		schedule(getId(), switchingDelay, CloudActionTags.NETWORK_PKT_FORWARD);

		// Packet is to be sent from an host
		if (level == SwitchLevel.EDGE_LEVEL) {
			int hostId = dc.VmtoHostlist.get(recvVMid);
			NetworkHost hs = hostList.get(hostId);
			npkt.receiverHostId = hostId;

			// Receiver host directly connected to the switch -- found!
			if (hs != null) {
				pktsToHosts.computeIfAbsent(hostId, k -> new ArrayList<>()).add(npkt);
				return;
			}

			// Send to aggregate level
			// ASSUMPTION: EACH EDGE is Connected to one aggregate level switch only
			Switch sw = uplinkSwitches.getFirst();
			pktsToUplinkSwitches.computeIfAbsent(sw.getId(), k -> new ArrayList<>()).add(npkt);
		}
		else if (level == SwitchLevel.AGGR_LEVEL) { // packet received from edge router
			// find the id for edgelevel switch
			int switchId = dc.VmToSwitchid.get(recvVMid);

			// send to edge (it's not going up, but same level)
            if (downlinkSwitches.stream().anyMatch(sw -> sw.getId() == switchId)) {
				pktsToDownlinkSwitches.computeIfAbsent(switchId, k -> new ArrayList<>()).add(npkt);
			} else {// send to up to root level (ASSUMPTION: EACH EDGE is Connected to one aggregate level switch only)
				Switch sw = uplinkSwitches.getFirst();
				pktsToUplinkSwitches.computeIfAbsent(sw.getId(), k -> new ArrayList<>()).add(npkt);
			}
		}
		// @TODO: confusing, this packet is going down, not up!!!
		else if (level == SwitchLevel.ROOT_LEVEL) { // packet received from aggregate router
			// get id of edge switch
			int edgeSwitchId = dc.VmToSwitchid.get(recvVMid);
			// search which aggregate switch is connected to the edge switch
			int aggrSwitchId = -1;
            for (Switch sw : downlinkSwitches) {
				for (Switch edge : sw.downlinkSwitches) {
					if (edge.getId() == edgeSwitchId) {
						aggrSwitchId = sw.getId();
						break;
					}
				}
			}
			if (aggrSwitchId < 0) {
				Log.println(" No destination for this packet");
			} else {
				pktsToDownlinkSwitches.computeIfAbsent(aggrSwitchId, k -> new ArrayList<>()).add(npkt);
			}
		} else {
			throw new IllegalStateException("Unknown switch level " + level);
		}
	}
        
	/**
	 * Register a host that is connected to the switch.
	 * Resource characteristics answer (@TODO: not in use)
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
		CloudSim.cancelAll(getId(), new PredicateType(CloudActionTags.NETWORK_PKT_UP));
		schedule(getId(), switchingDelay, CloudActionTags.NETWORK_PKT_UP);
		pktlist.add((NetworkPacket) ev.getTaskLength());
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
			Log.printlnConcat(getName(), ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**
	 * Forwards the processed packets to their respective destinations:
	 * an host, a downlink switch, or a uplink switch.
	 *
	 */
	protected void forwardProcessedPackets() {
		// Iterate over the packets in the downlink switch
		for (Entry<Integer, List<NetworkPacket>> es : pktsToDownlinkSwitches.entrySet()) {
			int receiverSwitchId = es.getKey();
			List<NetworkPacket> hspktlist = es.getValue();
			if (!hspktlist.isEmpty()) {
				double avband = (double) downlinkBw / hspktlist.size();
				for (NetworkPacket hspkt : hspktlist) {
					double delay = 8 * hspkt.pkt.data / avband;

					this.send(receiverSwitchId, delay, CloudActionTags.NETWORK_PKT_DOWN, hspkt);
				}
				hspktlist.clear();
			}
		}

		for (Entry<Integer, List<NetworkPacket>> es : pktsToUplinkSwitches.entrySet()) {
			int receiverSwitchId = es.getKey();
			List<NetworkPacket> hspktlist = es.getValue();
			if (!hspktlist.isEmpty()) {
				// sharing bandwidth between packets
				double avband = (double) uplinkBw / hspktlist.size();
				for (NetworkPacket hspkt : hspktlist) {
					double delay = 8 * hspkt.pkt.data / avband;

					this.send(receiverSwitchId, delay, CloudActionTags.NETWORK_PKT_UP, hspkt);
				}
				hspktlist.clear();
			}
		}

		for (Entry<Integer, List<NetworkPacket>> es : pktsToHosts.entrySet()) {
			List<NetworkPacket> hspktlist = es.getValue();
			if (!hspktlist.isEmpty()) {
				double avband = (double) downlinkBw / hspktlist.size();
				for (NetworkPacket npkt : hspktlist) {
					NetworkHost hs = hostList.get(npkt.receiverHostId);

					if (hs == null) { // nested virtualization edge-case
						for (NetworkHost candidateHs: hostList.values()) {
							if (candidateHs.getNics().get(npkt.pkt.receiverCloudletId) != null) {
								hs = candidateHs;

								// Replace packet host
								npkt.receiverHostId = hs.getId();
								break;
							}
						}
					}

					// simulate traversal overhead of the virtualization layers (host -> (nested) receiver guest)
                    assert hs != null;
                    int virtOverhead = hs.getTotalVirtualizationOverhead(npkt.getReceiverGuestId(), hs.getGuestList().iterator(), 0);
					double delay = (8 * npkt.pkt.data / avband) + virtOverhead;
					this.send(getId(), delay, CloudActionTags.NETWORK_PKT_REACHED_HOST, npkt);
				}
				hspktlist.clear();
			}
		}
	}

	// @TODO: These are never used, remove?
        /**
         * Gets the host of a given VM.
         * @param vmid The id of the VM
         * @return the host of the VM
         */
	/*protected NetworkHost getHostwithVM(int vmid) {
		for (Entry<Integer, NetworkHost> es : hostList.entrySet()) {
			Vm vm = VmList.getById(es.getValue().getGuestList(), vmid);
			if (vm != null) {
				return es.getValue();
			}
		}
		return null;
	}*/

        /**
         * Gets a list with a given number of free VMs.
         * 
         * @param numVMReq The number of free VMs to get.
         * @return the list of free VMs.
		 *
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
	/*protected List<NetworkHost> getfreehostlist(int numhost) {
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
	}*/
}
