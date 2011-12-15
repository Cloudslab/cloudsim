package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

public class RootSwitch extends Switch{

	public RootSwitch(String name, int level, NetworkDatacenter dc) {
		super(name, level, dc);
		downlinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
		downlinkswitches=new ArrayList<Switch>();
		
		downlinkbandwidth=Constants.BandWidthAggRoot;
		latency=Constants.SwitchingDelayRoot;
		numport=Constants.RootSwitchPort;
		
		// TODO Auto-generated constructor stub
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
	    
	    if(this.level==Constants.ROOT_LEVEL)
	    {
	    	//get id of edge router
	    	int edgeswitchid=dc.VmToSwitchid.get(recvVMid);
	    	//search which aggregate switch has it
	    	int aggSwtichid=-1;;
	    	for(Switch sw:this.downlinkswitches)
	    	{
        	    for(Switch edge:sw.downlinkswitches)
        	    {
        	    	if(edge.getId()==edgeswitchid){
        	    		aggSwtichid=sw.getId();
        	    		break;
        	    	}
        	    }
	    	}
	    	if(aggSwtichid<0) System.out.println(" No destination for this packet");
	    	else{
		    	List<HostPacket> pktlist=this.downlinkswitchpktlist.get(aggSwtichid);
		    	if(pktlist==null){
		    		pktlist=new ArrayList<HostPacket>();
		    		this.downlinkswitchpktlist.put(aggSwtichid, pktlist);
		    	}
		    	pktlist.add(hspkt);
	    	}
	    }
	}

}
