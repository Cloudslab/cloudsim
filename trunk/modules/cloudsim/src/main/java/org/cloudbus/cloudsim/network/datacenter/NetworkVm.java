/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

/**
 * NetworkVm class extends Vm to support simulation of networked datacenters. It executes actions
 * related to management of packets (send and receive).
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
public class NetworkVm extends Vm implements Comparable<Object> {

	public NetworkVm(
			int id,
			int userId,
			double mips,
			int pesNumber,
			int ram,
			long bw,
			long size,
			String vmm,
			CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, pesNumber, ram, bw, size, vmm, cloudletScheduler);

		cloudletlist = new ArrayList<NetworkCloudlet>();
	}

	public ArrayList<NetworkCloudlet> cloudletlist;

	int type;

	public ArrayList<HostPacket> recvPktlist;

	public double memory;

	public boolean flagfree;// if true it is free

	public double finishtime;

	public boolean isFree() {
		return flagfree;
	}

	@Override
	public int compareTo(Object arg0) {
		NetworkVm hs = (NetworkVm) arg0;
		if (hs.finishtime > finishtime) {
			return -1;
		}
		if (hs.finishtime < finishtime) {
			return 1;
		}
		return 0;
	}
}
