package org.cloudbus.cloudsim.network.datacenter;

public class HostPacket {
	public HostPacket(int id, NetPacket pkt2, int vmid, int cloudletid) {
		// TODO Auto-generated constructor stub
		this.pkt=pkt2;
		this.sendervmid=vmid;
		this.cloudletid=cloudletid;
		this.senderhostid=id;
		this.stime=pkt.sendtime;
		this.recievervmid=pkt2.reciever;
		
	}
	NetPacket pkt;
    int senderhostid;
    int recieverhostid;
    int sendervmid;
    int recievervmid;
    int cloudletid;
    double stime;//time when sent
    double rtime;//time when received
}
