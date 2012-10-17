/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
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

public class Switch extends SimEntity {

	// switch level
	public int id;

	public int level;// three levels

	public int datacenterid;

	public Map<Integer, List<NetworkPacket>> uplinkswitchpktlist;

	public Map<Integer, List<NetworkPacket>> downlinkswitchpktlist;

	public Map<Integer, NetworkHost> hostlist;

	public List<Switch> uplinkswitches;

	public List<Switch> downlinkswitches;

	public Map<Integer, List<NetworkPacket>> packetTohost;

	int type;// edge switch or aggregation switch

	public double uplinkbandwidth;

	public double downlinkbandwidth;

	public double latency;

	public double numport;

	public NetworkDatacenter dc;

	// something is running on these hosts
	public SortedMap<Double, List<NetworkHost>> fintimelistHost = new TreeMap<Double, List<NetworkHost>>();

	// something is running on these hosts
	public SortedMap<Double, List<NetworkVm>> fintimelistVM = new TreeMap<Double, List<NetworkVm>>();

	public ArrayList<NetworkPacket> pktlist;

	public List<Vm> BagofTaskVm = new ArrayList<Vm>();

	public double switching_delay;

	public Map<Integer, NetworkVm> Vmlist;

	public Switch(String name, int level, NetworkDatacenter dc) {
		super(name);
		this.level = level;
		this.dc = dc;
	}

	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
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

	protected void processhostpacket(SimEvent ev) {
		// Send packet to host
		NetworkPacket hspkt = (NetworkPacket) ev.getData();
		NetworkHost hs = hostlist.get(hspkt.recieverhostid);
		hs.packetrecieved.add(hspkt);
	}

	protected void processpacket_down(SimEvent ev) {
		// packet coming from up level router.
		// has to send downward
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

	protected void processpacket_up(SimEvent ev) {
		// packet coming from down level router.
		// has to send up
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

	private void registerHost(SimEvent ev) {
		NetworkHost hs = (NetworkHost) ev.getData();
		hostlist.put(hs.getId(), (NetworkHost) ev.getData());
	}

	protected void processpacket(SimEvent ev) {
		// send packet to itself with switching delay (discarding other)
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_UP));
		schedule(getId(), switching_delay, CloudSimTags.Network_Event_UP);
		pktlist.add((NetworkPacket) ev.getData());

		// add the packet in the list

	}

	private void processOtherEvent(SimEvent ev) {

	}

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

	//
	// R: We changed visibility of the below methods from private to protected.
	//

	protected NetworkHost getHostwithVM(int vmid) {
		for (Entry<Integer, NetworkHost> es : hostlist.entrySet()) {
			Vm vm = VmList.getById(es.getValue().getVmList(), vmid);
			if (vm != null) {
				return es.getValue();
			}
		}
		return null;
	}

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
		Log.printLine(getName() + " is shutting down...");
	}

}
