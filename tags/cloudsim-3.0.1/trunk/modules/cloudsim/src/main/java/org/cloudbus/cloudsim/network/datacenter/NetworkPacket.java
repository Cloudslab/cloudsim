/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * NewtorkPacket represents the packet which travel from one server to another. Each packet contains
 * ids of the sender VM and receiver VM, time at which it is send and received, type and virtual ids
 * of tasks, which are communicating.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */
public class NetworkPacket {

	public NetworkPacket(int id, HostPacket pkt2, int vmid, int cloudletid) {
		pkt = pkt2;
		sendervmid = vmid;
		this.cloudletid = cloudletid;
		senderhostid = id;
		stime = pkt.sendtime;
		recievervmid = pkt2.reciever;

	}

	HostPacket pkt;

	int senderhostid;

	int recieverhostid;

	int sendervmid;

	int recievervmid;

	int cloudletid;

	double stime;// time when sent

	double rtime;// time when received
}
