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
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * NetworkHost class extends Host to support simulation of networked datacenters. It executes
 * actions related to management of packets (send and receive)other than that of virtual machines
 * (e.g., creation and destruction). A host has a defined policy for provisioning memory and bw, as
 * well as an allocation policy for Pe's to virtual machines.
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
public class NetworkHost extends Host {

	public List<NetworkPacket> packetTosendLocal;

	public List<NetworkPacket> packetTosendGlobal;

	public List<NetworkPacket> packetrecieved;

	public double memory;

	public Switch sw; // Edge switch in general

	public double bandwidth;// latency

	/** time when last job will finish on CPU1 **/
	public List<Double> CPUfinTimeCPU = new ArrayList<Double>();

	public double fintime = 0;

	public NetworkHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);

		packetrecieved = new ArrayList<NetworkPacket>();
		packetTosendGlobal = new ArrayList<NetworkPacket>();
		packetTosendLocal = new ArrayList<NetworkPacket>();

	}

	/**
	 * Requests updating of processing of cloudlets in the VMs running in this host.
	 * 
	 * @param currentTime the current time
	 * 
	 * @return expected time of completion of the next cloudlet in all VMs in this host.
	 *         Double.MAX_VALUE if there is no future events expected in th is host
	 * 
	 * @pre currentTime >= 0.0
	 * @post $none
	 */
	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		// insert in each vm packet recieved
		recvpackets();
		for (Vm vm : super.getVmList()) {
			double time = ((NetworkVm) vm).updateVmProcessing(currentTime, getVmScheduler()
					.getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}
		// send the packets to other hosts/VMs
		sendpackets();

		return smallerTime;

	}

	/**
	 * Receives packet and forward it to the corresponding VM for processing host.
	 * 
	 * 
	 */
	private void recvpackets() {

		for (NetworkPacket hs : packetrecieved) {
			hs.pkt.recievetime = CloudSim.clock();

			// insertthe packet in recievedlist of VM
			Vm vm = VmList.getById(getVmList(), hs.pkt.reciever);
			List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv
					.get(hs.pkt.sender);

			if (pktlist == null) {
				pktlist = new ArrayList<HostPacket>();
				((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv.put(
						hs.pkt.sender,
						pktlist);

			}
			pktlist.add(hs.pkt);

		}
		packetrecieved.clear();
	}

	/**
	 * Send packet check whether a packet belongs to a local VM or to a VM hosted on other machine.
	 * 
	 * 
	 */
	private void sendpackets() {

		for (Vm vm : super.getVmList()) {
			for (Entry<Integer, List<HostPacket>> es : ((NetworkCloudletSpaceSharedScheduler) vm
					.getCloudletScheduler()).pkttosend.entrySet()) {
				List<HostPacket> pktlist = es.getValue();
				for (HostPacket pkt : pktlist) {
					NetworkPacket hpkt = new NetworkPacket(getId(), pkt, vm.getId(), pkt.sender);
					Vm vm2 = VmList.getById(this.getVmList(), hpkt.recievervmid);
					if (vm2 != null) {
						packetTosendLocal.add(hpkt);
					} else {
						packetTosendGlobal.add(hpkt);
					}
				}
				pktlist.clear();

			}

		}

		boolean flag = false;

		for (NetworkPacket hs : packetTosendLocal) {
			flag = true;
			hs.stime = hs.rtime;
			hs.pkt.recievetime = CloudSim.clock();
			// insertthe packet in recievedlist
			Vm vm = VmList.getById(getVmList(), hs.pkt.reciever);

			List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv
					.get(hs.pkt.sender);
			if (pktlist == null) {
				pktlist = new ArrayList<HostPacket>();
				((NetworkCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv.put(
						hs.pkt.sender,
						pktlist);
			}
			pktlist.add(hs.pkt);

		}
		if (flag) {
			for (Vm vm : super.getVmList()) {
				vm.updateVmProcessing(CloudSim.clock(), getVmScheduler().getAllocatedMipsForVm(vm));
			}
		}

		// Sending packet to other VMs therefore packet is forwarded to a Edge switch
		packetTosendLocal.clear();
		double avband = bandwidth / packetTosendGlobal.size();
		for (NetworkPacket hs : packetTosendGlobal) {
			double delay = (1000 * hs.pkt.data) / avband;
			NetworkConstants.totaldatatransfer += hs.pkt.data;

			CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudSimTags.Network_Event_UP, hs);
			// send to switch with delay
		}
		packetTosendGlobal.clear();
	}

	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
	}

}
