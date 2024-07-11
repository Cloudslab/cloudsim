/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * NetworkHost class extends {@link Host} to support simulation of networked datacenters. It executes
 * actions related to management of packets (sent and received) other than that of virtual machines
 * (e.g., creation and destruction). A host has a defined policy for provisioning memory and bw, as
 * well as an allocation policy for PE's to virtual machines.
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
public class NetworkHost extends Host {
	/**
	 * List of received packets.
	 */
	@Getter
	private List<NetworkPacket> receivedPkts;

	/**
	 * Edge switch in which the Host is connected.
	 */
	private Switch sw;

	public NetworkHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);

		receivedPkts = new ArrayList<>();
	}

	@Override
	public double updateCloudletsProcessing(double currentTime) {
		// insert received packets to destination guest
		receivePackets();

		double smallerTime = super.updateCloudletsProcessing(currentTime);

		// send the packets to other hosts/guests
		sendPackets();

		return smallerTime;
	}

	/**
	 * Receives packets and forward them to the corresponding guest.
	 */
	private void receivePackets() {
		for (NetworkPacket hs : getReceivedPkts()) {
			hs.pkt.recvTime = CloudSim.clock();

			// insert the packet in received list of destination guest
			GuestEntity guest = VmList.getById(getGuestList(), hs.pkt.receiverGuestId);
			List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) guest.getCloudletScheduler()).getReceivedPkts()
					.computeIfAbsent(hs.pkt.senderGuestId, k -> new ArrayList<>());

			pktlist.add(hs.pkt);

		}
		receivedPkts.clear();
	}

	/**
	 * Sends packets checks whether a packet belongs to a local VM or to a 
         * VM hosted on other machine.
	 */
	private void sendPackets() {
		boolean flag = false;

		// Retrieve packets to be sent
		for (GuestEntity sender : super.getGuestList()) {
			Map<Integer, List<HostPacket>> pkttosend = ((NetworkCloudletSpaceSharedScheduler) sender
															.getCloudletScheduler()).getPktToSend();
			int totalPkts = pkttosend.values().stream().mapToInt(List::size).sum();
			for (Entry<Integer, List<HostPacket>> es : pkttosend.entrySet()) {
				List<HostPacket> pktlist = es.getValue();
				for (HostPacket hpkt : pktlist) {
					NetworkPacket npkt = new NetworkPacket(getId(), hpkt);
					GuestEntity receiver = VmList.getById(this.getGuestList(), npkt.receiverGuestId);
					if (receiver != null) { // send locally to Vm, no network delay
						flag = true;

						npkt.sendTime = npkt.recvTime;
						npkt.pkt.recvTime = CloudSim.clock();

						// insert the packet in received list on destination guest
						((NetworkCloudletSpaceSharedScheduler) receiver.getCloudletScheduler()).getReceivedPkts()
								.computeIfAbsent(npkt.pkt.senderGuestId, k -> new ArrayList<>()).add(npkt.pkt);
					} else { // send to edge switch, since destination guest is hosted on another host
						// Assumption: no overprovisioning of guest's bandwidth
						double avband = (double) sender.getBw() / totalPkts;
						double delay = (1000 * npkt.pkt.data) / avband;

						NetworkGlobals.totaldatatransfer += npkt.pkt.data;

						// send to switch with delay
						CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudActionTags.NETWORK_PKT_UP, npkt);
					}
				}
				pktlist.clear();
			}
		}

		if (flag) {
			for (GuestEntity guest : super.getGuestList()) {
				guest.updateCloudletsProcessing(CloudSim.clock(), getGuestScheduler().getAllocatedMipsForGuest(guest));
			}
		}
	}

	public Switch getSwitch() {
		return sw;
	}

	public void setSwitch(Switch sw) {
		this.sw = sw;
	}
}
