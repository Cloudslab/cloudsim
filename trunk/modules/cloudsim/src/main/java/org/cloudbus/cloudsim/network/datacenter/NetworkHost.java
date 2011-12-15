/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
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
 * Host class extends a Machine to include other hostList beside PEs
 * to support simulation of virtualized grids. It executes actions related
 * to management of virtual machines (e.g., creation and destruction). A host has
 * a defined policy for provisioning memory and bw, as well as an allocation policy
 * for Pe's to virtual machines.
 *
 * A host is associated to a datacenter. It can host virtual machines.
 *
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class NetworkHost extends Host {

	public List <HostPacket> packetTosendLocal;
	public List <HostPacket> packetTosendGlobal;
	public List <HostPacket> packetrecieved;
	public double memory;
	public Switch sw;
	public double bandwidth;//latency
	public List<Double> CPUfinTimeCPU=new ArrayList<Double>();//time when last job will finish on CPU1 
	
	public double fintime=0;
	
	public NetworkHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		// TODO Auto-generated constructor stub
		   this.packetrecieved=new ArrayList<HostPacket>();
		   this.packetTosendGlobal=new ArrayList<HostPacket>();
		   this.packetTosendLocal=new ArrayList<HostPacket>();
	   
	}
	
	
    /**
	 * Requests updating of processing of cloudlets in the VMs running in this host.
	 *
	 * @param currentTime the current time
	 *
	 * @return 		expected time of completion of the next cloudlet in all VMs in this host. Double.MAX_VALUE
	 * if there is no future events expected in this host
	 *
	 * @pre 		currentTime >= 0.0
	 * @post 		$none
	 */
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
        //insert in each vm packet recieved
		recvpackets();
		for (Vm vm : super.getVmList()) {
//			if (vm.isInMigration()) {
//				continue;
//			}
			
			double time = ((NetworkVm)vm).updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}
        sendpackets();
	
		return smallerTime;
		
	}


	private void recvpackets() {
		// TODO Auto-generated method stub
		for(HostPacket hs:packetrecieved)
		{
			//hs.stime=hs.rtime;
			hs.pkt.recievetime=CloudSim.clock();
			
			//insertthe packet in recievedlist
			Vm vm=VmList.getById(getVmList(), hs.pkt.reciever);
		 	List<NetPacket> pktlist=((CloudletHPCSpaceShared)vm.getCloudletScheduler()).pktrecv.get(hs.pkt.sender);
		    //List<NetPacket> pktlist=((CloudletHPCSpaceSharedO)vm.getCloudletScheduler()).pktrecv.get(hs.pkt.virtualsendid);
			if(pktlist==null){
				pktlist=new ArrayList<NetPacket>();
				((CloudletHPCSpaceShared)vm.getCloudletScheduler()).pktrecv.put(hs.pkt.sender, pktlist);
			// ((CloudletHPCSpaceSharedO)vm.getCloudletScheduler()).pktrecv.put(hs.pkt.virtualsendid, pktlist);
			}
			pktlist.add(hs.pkt);
			
			
		}
		packetrecieved.clear();
	}


	private void sendpackets() {
		// TODO Auto-generated method stub
		
		for(Vm vm:super.getVmList())
		{
			for(Entry<Integer, List<NetPacket>> es:((CloudletHPCSpaceShared)vm.getCloudletScheduler()).pkttosend.entrySet())
			//for(Entry<Integer, List<NetPacket>> es:((CloudletHPCSpaceSharedO)vm.getCloudletScheduler()).pkttosend.entrySet())
			{
				List<NetPacket> pktlist=es.getValue();
			    for(NetPacket pkt:pktlist)
			    {
			    	HostPacket hpkt=new HostPacket(this.getId(),pkt,vm.getId(),pkt.sender);
			    	Vm vm2=VmList.getById(this.getVmList(), hpkt.recievervmid);
			    	if(vm2!=null) this.packetTosendLocal.add(hpkt);
			    	else this.packetTosendGlobal.add(hpkt);
			    }
			    pktlist.clear();
				
			}
						
		}
		
		boolean flag=false;
		
		for(HostPacket hs:packetTosendLocal)
		{
			flag=true;
			hs.stime=hs.rtime;
			hs.pkt.recievetime=CloudSim.clock();
			//insertthe packet in recievedlist
			Vm vm=VmList.getById(getVmList(), hs.pkt.reciever);
			//Vm vm=getVmList().get(hs.pkt.reciever);
			
			List<NetPacket> pktlist=((CloudletHPCSpaceShared)vm.getCloudletScheduler()).pktrecv.get(hs.pkt.sender);
			//List<NetPacket> pktlist=((CloudletHPCSpaceSharedO)vm.getCloudletScheduler()).pktrecv.get(hs.pkt.virtualsendid);
			if(pktlist==null){
				pktlist=new ArrayList<NetPacket>();
					((CloudletHPCSpaceShared)vm.getCloudletScheduler()).pktrecv.put(hs.pkt.sender, pktlist);
			//	((CloudletHPCSpaceSharedO)vm.getCloudletScheduler()).pktrecv.put(hs.pkt.virtualsendid, pktlist);
			}
			pktlist.add(hs.pkt);
			
		}
		if(flag){
		for (Vm vm : super.getVmList()) {
//			if (vm.isInMigration()) {
//				continue;
//			}
			
			double time = ((NetworkVm)vm).updateVmProcessing(CloudSim.clock(), getVmScheduler().getAllocatedMipsForVm(vm));
			
		}
		}
		this.packetTosendLocal.clear();
		double avband=this.bandwidth/packetTosendGlobal.size();
		for(HostPacket hs:packetTosendGlobal)
		{
			double delay=(1000*hs.pkt.data)/avband;
			Constants.totaldatatransfer+=hs.pkt.data;
			//System.out.println(hs.pkt.virtualsendid+"  "+hs.pkt.virtualrecvid+" "+hs.pkt.data);
			
			CloudSim.send(this.getDatacenter().getId(), this.sw.getId(), delay, CloudSimTags.Network_Event_UP, hs);
			//send to switch with delay
		}
		this.packetTosendGlobal.clear();
	}
   
  
	@SuppressWarnings("unchecked")
	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes((List<Pe>) getPeList(), vm);
	}
	
}
