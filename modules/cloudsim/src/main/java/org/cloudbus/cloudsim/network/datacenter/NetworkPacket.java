package org.cloudbus.cloudsim.network.datacenter;

/**
 * NewtorkPacket represents the packet which travel from one server to another. 
 * Each packet contains ids of the sender VM and receiver VM, 
 * time at which it is send and received, type and virtual ids of tasks, which are communicating.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel
 * Applications in Cloud Simulations, Proceedings of the 4th IEEE/ACM
 * International Conference on Utility and Cloud Computing (UCC 2011, IEEE CS
 * Press, USA), Melbourne, Australia, December 5-7, 2011.
 *
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */

public class NetworkPacket {
	public NetworkPacket(int id, HostPacket pkt2, int vmid, int cloudletid) {
		// TODO Auto-generated constructor stub
		this.pkt = pkt2;
		this.sendervmid = vmid;
		this.cloudletid = cloudletid;
		this.senderhostid = id;
		this.stime = pkt.sendtime;
		this.recievervmid = pkt2.reciever;

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
