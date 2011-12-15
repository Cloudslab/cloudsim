package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

public class EdgeSwitch extends Switch{

	public EdgeSwitch(String name, int dcid, NetworkDatacenter dc) {
		super(name, dcid, dc);
		hostlist=new HashMap<Integer,NetworkHost>();
		uplinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
		packetTohost=new HashMap<Integer,List<HostPacket>>();
        uplinkbandwidth=Constants.BandWidthEdgeAgg;
		downlinkbandwidth=Constants.BandWidthEdgeHost;
		this.switching_delay=Constants.SwitchingDelayEdge;
		numport=Constants.EdgeSwitchPort;
		uplinkswitches=new ArrayList<Switch>();
		// TODO Auto-generated constructor stub
	}
	private void registerHost(SimEvent ev) {
		// TODO Auto-generated method stub
		NetworkHost hs=(NetworkHost)ev.getData();
		hostlist.put(hs.getId(),(NetworkHost)ev.getData());
	}
	private void processpacket_up(SimEvent ev) {
		// TODO Auto-generated method stub
		//packet coming from down level router/host.
		//has to send up
		//check which switch to forward to 
		//add packet in the switch list
		//
		//int src=ev.getSource();
		HostPacket hspkt=(HostPacket) ev.getData();
	    int recvVMid=hspkt.pkt.reciever;
	    CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(),this.switching_delay, CloudSimTags.Network_Event_send);
	    
    	// packet is recieved from host
    	//packet is to be sent to aggregate level or to another host in the same level
    	
    	int hostid=dc.VmtoHostlist.get(recvVMid);
    	NetworkHost hs=this.hostlist.get(hostid);
    	hspkt.recieverhostid=hostid;
    	
    	//packet needs to go to a host which is connected directly to switch
    	if(hs!=null)
    	{
    		//packet to be sent to host connected to the switch
    		List<HostPacket> pktlist=this.packetTohost.get(hostid);
	    	if(pktlist==null){
	    		pktlist=new ArrayList<HostPacket>();
	    		this.packetTohost.put(hostid, pktlist);
	    	}
	    	pktlist.add(hspkt);
	    	return;
    		
    	}
    	//otherwise
    	//packet is to be sent to upper switch
    	//ASSUMPTION EACH EDGE is Connected to one aggregate level switch
    	//if there are more than one Aggregate level switch one need to modify following code
             	
    	Switch sw=this.uplinkswitches.get(0);
    	List<HostPacket> pktlist=this.uplinkswitchpktlist.get(sw.getId());
    	if(pktlist==null){
    		pktlist=new ArrayList<HostPacket>();
    		this.uplinkswitchpktlist.put(sw.getId(), pktlist);
    	}
    	pktlist.add(hspkt);
    	return;
	  	    
	   
	}
	private void processpacketforward(SimEvent ev) {
		// TODO Auto-generated method stub
		//search for the host and packets..send to them
		
		
		if(this.uplinkswitchpktlist!=null)
		{
			for(Entry<Integer, List<HostPacket>> es:uplinkswitchpktlist.entrySet())
			{
				int tosend=es.getKey();
				List<HostPacket> hspktlist=es.getValue();
				if(!hspktlist.isEmpty()){
					//sharing bandwidth between packets
					double avband=this.uplinkbandwidth/hspktlist.size();
					Iterator<HostPacket> it=hspktlist.iterator();
					while(it.hasNext())
					{
						HostPacket hspkt=it.next();
						double delay=1000*hspkt.pkt.data/avband;
						
						this.send(tosend,delay,CloudSimTags.Network_Event_UP, hspkt);
					}
					hspktlist.clear();
				}
			}
		}
		if(this.packetTohost!=null)
		{
			for(Entry<Integer, List<HostPacket>> es:packetTohost.entrySet())
			{
				int tosend=es.getKey();
				NetworkHost hs=this.hostlist.get(tosend);
				List<HostPacket> hspktlist=es.getValue();
				if(!hspktlist.isEmpty()){
					double avband=this.downlinkbandwidth/hspktlist.size();
					Iterator<HostPacket> it=hspktlist.iterator();
					while(it.hasNext()){
						HostPacket hspkt=it.next();
						//hspkt.recieverhostid=tosend;
						//hs.packetrecieved.add(hspkt);
						this.send(this.getId(),hspkt.pkt.data/avband,CloudSimTags.Network_Event_Host, hspkt);
					}
					hspktlist.clear();
				}
			}		
		}
		
		
		//or to switch at next level.
		//clear the list
		
	}



}
