package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.VmList;

public class Switch extends SimEntity{

	
	//switch level
	public int id;
	public int level;//three levels
	public int datacenterid;
	public Map<Integer,List<HostPacket>> uplinkswitchpktlist;
	public Map<Integer,List<HostPacket>> downlinkswitchpktlist;
	public Map<Integer,NetworkHost> hostlist;
    public List<Switch>uplinkswitches;
    public List <Switch>downlinkswitches;
	public Map<Integer,List<HostPacket>> packetTohost;
	int type;//edge switch or aggregation switch
	public double uplinkbandwidth;
	public double downlinkbandwidth;
	public double latency;
	public double numport;
	public NetworkDatacenter dc;
	
	public SortedMap<Double,List<NetworkHost>> fintimelistHost=new TreeMap<Double,List<NetworkHost>>();//something is running on these hosts
	public SortedMap<Double,List<NetworkVm>> fintimelistVM=new TreeMap<Double,List<NetworkVm>>();//something is running on these hosts
	public ArrayList<HostPacket> pktlist;
	public List<Vm> BagofTaskVm=new ArrayList<Vm>();
	
	public double switching_delay;
	public Map<Integer,NetworkVm> Vmlist;
	public Switch(String name,int level,NetworkDatacenter dc) {
		super(name);
		this.level=level;
		
	/*	if(level==Constants.EDGE_LEVEL)
		{
			hostlist=new HashMap<Integer,HPCHost>();
			uplinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
			packetTohost=new HashMap<Integer,List<HostPacket>>();
	        uplinkbandwidth=Constants.BandWidthEdgeAgg;
			downlinkbandwidth=Constants.BandWidthEdgeHost;
			latency=Constants.SwitchingDelayEdge;
			numport=Constants.EdgeSwitchPort;
			uplinkswitches=new ArrayList<Switch>();
			
		}
		if(level==Constants.Agg_LEVEL)
		{
			downlinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
			uplinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
			  uplinkbandwidth=Constants.BandWidthAggRoot;
				downlinkbandwidth=Constants.BandWidthEdgeAgg;
				latency=Constants.SwitchingDelayAgg;
				numport=Constants.AggSwitchPort;
				uplinkswitches=new ArrayList<Switch>();
				downlinkswitches=new ArrayList<Switch>();
		}
		if(level==Constants.ROOT_LEVEL)
		{
			downlinkswitchpktlist=new HashMap<Integer,List<HostPacket>>();
			downlinkswitches=new ArrayList<Switch>();
			
			downlinkbandwidth=Constants.BandWidthAggRoot;
			latency=Constants.SwitchingDelayRoot;
			numport=Constants.RootSwitchPort;
			
		}*/
		this.dc=dc;
		// TODO Auto-generated constructor stub
	}


	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}


	@Override
	public void processEvent(SimEvent ev) {
		//Log.printLine(CloudSim.clock()+"[Broker]: event received:"+ev.getTag());
		switch (ev.getTag()){
			// Resource characteristics request
			case CloudSimTags.Network_Event_UP:
				//process the packet from down switch or host
				processpacket_up(ev);
				break;
			case CloudSimTags.Network_Event_DOWN:
				//process the packet from uplink
				processpacket_down(ev);
				break;
			case CloudSimTags.Network_Event_send:
				processpacketforward(ev);
				break;
			
			case CloudSimTags.Network_Event_Host:
				processhostpacket(ev);
				break;
			// Resource characteristics answer
	        case CloudSimTags.RESOURCE_Register:
	        	registerHost(ev);
	            break;
            // other unknown tags are processed by this method
	        default:
	            processOtherEvent(ev);
	            break;
		}
	}
 
	private void processhostpacket(SimEvent ev) {
		// TODO Auto-generated method stub
		//Send packet to host
		HostPacket hspkt=(HostPacket) ev.getData();
		NetworkHost hs=this.hostlist.get(hspkt.recieverhostid);
		hs.packetrecieved.add(hspkt);
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
	    if(this.level==Constants.EDGE_LEVEL)
	    {
	    	// packet is to be recieved by host
	    	int hostid=dc.VmtoHostlist.get(recvVMid);
	    	hspkt.recieverhostid=hostid;
	    	List<HostPacket> pktlist=this.packetTohost.get(hostid);
	    	if(pktlist==null){
	    		pktlist=new ArrayList<HostPacket>();
	    		this.packetTohost.put(hostid, pktlist);
	    	}
	    	pktlist.add(hspkt);
	    	return;
	    }
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
	    if(this.level==Constants.EDGE_LEVEL)
	    {
	    	// packet is recieved from host
	    	//packet is to be sent to aggregate level or to another host in the same level
	    	
	    	int hostid=dc.VmtoHostlist.get(recvVMid);
	    	NetworkHost hs=this.hostlist.get(hostid);
	    	hspkt.recieverhostid=hostid;
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
	    	//packet is to be sent to upper switch
	    	//ASSUMPTION EACH EDGE is Connected to one aggregate level switch
	             	
	    	Switch sw=this.uplinkswitches.get(0);
	    	List<HostPacket> pktlist=this.uplinkswitchpktlist.get(sw.getId());
	    	if(pktlist==null){
	    		pktlist=new ArrayList<HostPacket>();
	    		this.uplinkswitchpktlist.put(sw.getId(), pktlist);
	    	}
	    	pktlist.add(hspkt);
	    	return;
	    }
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


	private void registerHost(SimEvent ev) {
		// TODO Auto-generated method stub
		NetworkHost hs=(NetworkHost)ev.getData();
		hostlist.put(hs.getId(),(NetworkHost)ev.getData());
	}


	private void processpacket(SimEvent ev) {
		// TODO Auto-generated method stub
		//send packet to itself with switching delay (discarding other)
		CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.Network_Event_UP));
		schedule(getId(),this.switching_delay, CloudSimTags.Network_Event_UP);
		pktlist.add((HostPacket)ev.getData());
		
		//add the packet in the list
		
	}


	private void processOtherEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		
	}

	private void processpacketforward(SimEvent ev) {
		// TODO Auto-generated method stub
		//search for the host and packets..send to them
		
		if(this.downlinkswitchpktlist!=null)
		{
			for(Entry<Integer, List<HostPacket>> es:downlinkswitchpktlist.entrySet())
			{
				int tosend=es.getKey();
				List<HostPacket> hspktlist=es.getValue();
				if(!hspktlist.isEmpty()){
					double avband=this.downlinkbandwidth/hspktlist.size();
					Iterator<HostPacket> it=hspktlist.iterator();
					while(it.hasNext()){
						HostPacket hspkt=it.next();
						double delay=1000*hspkt.pkt.data/avband;
						
						this.send(tosend,delay,CloudSimTags.Network_Event_DOWN, hspkt);
					}
					hspktlist.clear();
				}
			}
		}
		if(this.uplinkswitchpktlist!=null)
		{
			for(Entry<Integer, List<HostPacket>> es:uplinkswitchpktlist.entrySet())
			{
				int tosend=es.getKey();
				List<HostPacket> hspktlist=es.getValue();
				if(!hspktlist.isEmpty()){
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


	private NetworkHost getHostwithVM(int vmid) {
		// TODO Auto-generated method stub
		for(Entry<Integer, NetworkHost> es:this.hostlist.entrySet())
		{
			Vm vm=VmList.getById(es.getValue().getVmList(),vmid);
		    if(vm!=null) return es.getValue();
		}
		return null;
	}

	public List<NetworkHost> gethostsWithAvailability(int numHostReq,
			double deadline, double exeTime) {
		// TODO Auto-generated method stub
		List<NetworkHost> ls=null;
	
		for(Entry<Double,List<NetworkHost>> es:this.fintimelistHost.entrySet()){
			if((es.getKey()+exeTime)>=deadline){
				ls= null;
			    break;}
			else{
					if(es.getValue().size()>=numHostReq)
					{
						ls= es.getValue();
						break;
					}
			}
		}
//		if(ls==null)
//		{
//			List<HPCHost> freehostlist=getfreehostlist(numHostReq);
//			if(freehostlist.size()>=numHostReq) ls=freehostlist;
//		}
		return ls;
	}
	public List<NetworkVm> getVmsWithAvailability(int numVMReq,
			double deadline, double exeTime) {
		// TODO Auto-generated method stub
		List<NetworkVm> ls=new ArrayList<NetworkVm>();
		
	
//		SortedMap<Double,HPCVm> vmlst=getfintimelistVM();
		int i =0;
     	for(Entry<Integer,NetworkHost> es:this.hostlist.entrySet()){
     		
     		for(Vm vm:es.getValue().getVmList())
     		{
     			NetworkVm v=(NetworkVm)vm;
     			if((v.finishtime+exeTime)<deadline)
     			{
     				ls.add(v);
     				i++;
     			}
     			if(i>=numVMReq) break;
     		}
     		if(i>=numVMReq) break;
     	}
     		
     		//			if((es.getKey()+exeTime)>=deadline){
//				ls= null;
//			    break;}
//			else{
//					if(es.getValue().size()>=numVMReq)
//					{
//						ls= es.getValue();
//						break;
//					}
//			}
//		}
//		if(ls==null)
//		{
//			List<HPCVm> freeVMlist=getfreeVmlist(numVMReq);
//			if(freeVMlist.size()>=numVMReq) ls=freeVMlist;
//		}
		return ls;
	}
//	private SortedMap<Double, HPCVm> getfintimelistVM() {
//		// TODO Auto-generated method stub
//		SortedMap<Double,HPCVm> getfintimelis
//		return null;
//	}


	private List<NetworkVm> getfreeVmlist(int numVMReq) {
		// TODO Auto-generated method stub
		List<NetworkVm> freehostls=new ArrayList<NetworkVm>();
		for(Entry<Integer,NetworkVm> et:this.Vmlist.entrySet())
		{
			if(et.getValue().isFree())
			{
				freehostls.add(et.getValue());
			}
			if(freehostls.size()==numVMReq)
				break;
		}
		
		return freehostls;
	}


	private List<NetworkHost> getfreehostlist(int numhost) {
		// TODO Auto-generated method stub
		List<NetworkHost> freehostls=new ArrayList<NetworkHost>();
		for(Entry<Integer,NetworkHost> et:this.hostlist.entrySet())
		{
			if(et.getValue().getFreePesNumber()==et.getValue().getPesNumber())
			{
				freehostls.add(et.getValue());
			}
			if(freehostls.size()==numhost)
				break;
		}
		
		return freehostls;
	}
    

	@Override
	public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
	}

	
	
}
