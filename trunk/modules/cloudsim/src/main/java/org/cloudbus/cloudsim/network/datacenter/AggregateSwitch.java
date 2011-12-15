package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

public class AggregateSwitch extends Switch{

	public AggregateSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		// TODO Auto-generated constructor stub
		
		downlinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
		uplinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
		  uplinkbandwidth=Constants.BandWidthAggRoot;
			downlinkbandwidth=Constants.BandWidthEdgeAgg;
			latency=Constants.SwitchingDelayAgg;
			numport=Constants.AggSwitchPort;
			uplinkswitches=new ArrayList<Switch>();
			downlinkswitches=new ArrayList<Switch>();
	}

	
	private void processpacket_down(SimEvent ev) {
		// TODO Auto-generated method stub
		//packet coming from up level router.
		//has to send downward
		//check which switch to forward to 
		//add packet in the switch list
		//add packet in the host list
		//int src=ev.getSource();
		HostPacket hspkt=(HostPacket) ev.getData();
	    int recvVMid=hspkt.pkt.reciever;
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(),this.latency, CloudSimTags.Network_Event_send);
	    
	    if(this.level==Constants.Agg_LEVEL)
	    {
	    	//packet is coming from root so need to be sent to edgelevel swich
	    	//find the id for edgelevel switch
	    	int switchid=dc.VmToSwitchid.get(recvVMid);
	    	List<HostPacket> pktlist=this.downlinkswitchpktlist.get(switchid);
	    	if(pktlist==null){
	    		pktlist=new ArrayList<HostPacket>();
	    		this.downlinkswitchpktlist.put(switchid, pktlist);
	    	}
	    	pktlist.add(hspkt);
	    	return;
	    }
	  	
	}


	private void processpacket_up(SimEvent ev) {
		// TODO Auto-generated method stub
		//packet coming from down level router.
		//has to send up
		//check which switch to forward to 
		//add packet in the switch list
		//
		//int src=ev.getSource();
		HostPacket hspkt=(HostPacket) ev.getData();
	    int recvVMid=hspkt.pkt.reciever;
	    CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_send));
		schedule(getId(),this.switching_delay, CloudSimTags.Network_Event_send);
	    
	    if(this.level==Constants.Agg_LEVEL)
	    {
	    	//packet is coming from edge level router so need to be sent to either root or another edge level swich
	    	//find the id for edgelevel switch
	    	int switchid=dc.VmToSwitchid.get(recvVMid);
	    	boolean flagtoswtich=false;
	    	for(Switch sw:this.downlinkswitches)
	    	{
        	   if(switchid==sw.getId()) flagtoswtich=true; 
	    	}
	    	if(flagtoswtich)
	    	{
		    	List<HostPacket> pktlist=this.downlinkswitchpktlist.get(switchid);
		    	if(pktlist==null){
		    		pktlist=new ArrayList<HostPacket>();
		    		this.downlinkswitchpktlist.put(switchid, pktlist);
		    	}
		    	pktlist.add(hspkt);
	    	}
	    	else//send to up
	    	{
	    		Switch sw=this.uplinkswitches.get(0);
		    	List<HostPacket> pktlist=this.uplinkswitchpktlist.get(sw.getId());
		    	if(pktlist==null){
		    		pktlist=new ArrayList<HostPacket>();
		    		this.uplinkswitchpktlist.put(sw.getId(), pktlist);
		    	}
		    	pktlist.add(hspkt);
	    	}
	    }
	}


		

}
