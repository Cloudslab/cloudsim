package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;


public class NetworkVm extends Vm implements Comparable{

	public NetworkVm(int id, int userId, double mips, int pesNumber, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, pesNumber, ram, bw, size, vmm, cloudletScheduler);
		// TODO Auto-generated constructor stub
		cloudletlist=new ArrayList<NetworkCloudlet>();
	}
	public ArrayList<NetworkCloudlet> cloudletlist;
    int type;
	public ArrayList<NetPacket> recvPktlist;
	public double memory;
	public boolean flagfree;//if true it is free
	public double finishtime;
	public boolean isFree()
	{
		return flagfree;
	}
	public int compareTo(Object arg0) {
		NetworkVm hs=(NetworkVm)arg0;
		if(hs.finishtime>this.finishtime) return -1;
		if(hs.finishtime<this.finishtime) return 1;
		return 0;
	}
}
