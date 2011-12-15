package org.cloudbus.cloudsim.network.datacenter;

public class NetPacket {
     public NetPacket(int sender, int reciever, double data, double sendtime,
			double recievetime,int vsnd,int vrvd) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.data = data;
		this.sendtime = sendtime;
		this.recievetime = recievetime;
		this.virtualrecvid=vrvd;
		this.virtualsendid=vsnd;
	}
	int sender;
	int virtualrecvid;
	int virtualsendid;
     int reciever;
     double data;
     double sendtime;
     double recievetime;
}
