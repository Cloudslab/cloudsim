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

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.*;
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
public class NetworkHost extends Host implements NetworkedEntity {
	/** Edge switch to which the Host is connected. */
	private Switch sw;

	/** cloudlet -> nic
	 * @TODO: Ideally the nic shouldn't skip the guest entity; to be fixed
	 */
	private Map<Integer, NetworkInterfaceCard> nics;
	private Map<Integer, List<NetworkPacket>> sendPktExternally;

	public NetworkHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		nics = new HashMap<>();
		sendPktExternally = new HashMap<>();
	}

	@Override
	public double updateCloudletsProcessing(double currentTime) {
		double smallerTime = super.updateCloudletsProcessing(currentTime);

		// send the packets to other hosts/guests
		sendPackets();

		return smallerTime;
	}

	/**
	 * Sends packets checks whether a packet belongs to a local VM or to a 
         * VM hosted on other machine.
	 */
    public void sendPackets() {
		boolean flag = false;

		for (NetworkInterfaceCard nic : nics.values()) {
			for (HostPacket hpkt : nic.getPktsToSend()) {
				GuestEntity receiver = VmList.getById(this.getGuestList(), hpkt.receiverGuestId);
				if (receiver != null) { // send locally to Vm, no network delay
					flag = true;
					hpkt.recvTime = CloudSim.clock();

					// insert the packet in received list on destination guest
					nics.get(hpkt.receiverCloudletId).getReceivedPkts().add(hpkt);
				} else {
					sendPktExternally.computeIfAbsent(hpkt.senderGuestId, k -> new ArrayList<>())
									 .add(new NetworkPacket(getId(), hpkt));
				}
			}
			nic.getPktsToSend().clear();
		}

		// send to edge switch, since destination guest is hosted on another host
		for (Integer guestId : sendPktExternally.keySet()) {
			GuestEntity sender = VmList.getById(this.getGuestList(), guestId);
			if (sender == null) {
				throw new RuntimeException("senderVm not found! is it nested?");
			}

			for (NetworkPacket npkt : sendPktExternally.get(guestId)) {
				// Assumption: no overprovisioning of guest's bandwidth
				double avband = (double) sender.getBw() / sendPktExternally.get(guestId).size();
				double delay = (8 * npkt.pkt.data / avband) + npkt.pkt.accumulatedVirtualizationOverhead;

				((NetworkDatacenter) getDatacenter()).totalDataTransfer += npkt.pkt.data;

				// send to switch with delay
				CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudActionTags.NETWORK_PKT_UP, npkt);
			}
		}
		sendPktExternally.clear();

		if (flag) {
			for (GuestEntity guest : super.getGuestList()) {
				guest.updateCloudletsProcessing(CloudSim.clock(), getGuestScheduler().getAllocatedMipsForGuest(guest));
			}
		}
	}

	public Map<Integer, NetworkInterfaceCard> getNics() {
		return nics;
	}

	public Switch getSwitch() {
		return sw;
	}

	public void setSwitch(Switch sw) {
		this.sw = sw;
	}
}
