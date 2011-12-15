package org.cloudbus.cloudsim.network.datacenter;
/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.DataCloudTags;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.InfoPacket;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * Datacenter class is a CloudResource whose hostList
 * are virtualized. It deals with processing of VM queries (i.e., handling
 * of VMs) instead of processing Cloudlet-related queries. So, even though an
 * AllocPolicy will be instantiated (in the init() method of the superclass,
 * it will not be used, as processing of cloudlets are handled by the CloudletScheduler
 * and processing of VirtualMachines are handled by the VmAllocationPolicy.
 *
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class NetworkDatacenter extends Datacenter {

	public NetworkDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		// TODO Auto-generated constructor stub
		VmToSwitchid=new HashMap<Integer,Integer>();
		HostToSwitchid=new HashMap<Integer,Integer>();
		VmtoHostlist=new HashMap<Integer,Integer>();
		Switchlist=new HashMap<Integer,Switch>();
	}
    public Map<Integer,Integer> VmToSwitchid;
    public Map<Integer,Integer> HostToSwitchid;
    public Map<Integer,Switch> Switchlist;
	
    
	public Map<Integer,Integer> VmtoHostlist;
	
	public Map<Integer, Switch> getEdgeSwitch(){
		Map<Integer,Switch> edgeswitch=new HashMap<Integer,Switch>();
	    for(Entry<Integer, Switch> es:Switchlist.entrySet()){
	    	if(es.getValue().level==Constants.EDGE_LEVEL)
	    	{
	    		edgeswitch.put(es.getKey(), es.getValue());
	    	}
	    }
		return edgeswitch;
		
	}
	
	
	public boolean processVmCreateHPC(Vm vm) {
    	

 	    boolean result = getVmAllocationPolicy().allocateHostForVm(vm);
 	   

 	    if (result) {
 	 	   this.VmToSwitchid.put(vm.getId(), ((NetworkHost)vm.getHost()).sw.getId());
 	 	   this.VmtoHostlist.put(vm.getId(),vm.getHost().getId());
 	 	   System.out.println(vm.getId()+" VM is created on "+vm.getHost().getId());
			double amount = 0.0;
			if (getDebts().containsKey(vm.getUserId())) {
				amount = getDebts().get(vm.getUserId());
			}
			amount += getCharacteristics().getCostPerMem() * vm.getRam();
			amount += getCharacteristics().getCostPerStorage() * vm.getSize();

			getDebts().put(vm.getUserId(), amount);

			getVmList().add(vm);

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
 	    }
 	   return result;
    }
   
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
    	updateCloudletProcessing();

        try {
            // gets the Cloudlet object
            Cloudlet cl = (Cloudlet) ev.getData();
            

            // checks whether this Cloudlet has finished or not
            if (cl.isFinished()){
                String name = CloudSim.getEntityName(cl.getUserId());
                Log.printLine(getName()+": Warning - Cloudlet #"+cl.getCloudletId()+" owned by "+name+" is already completed/finished.");
                Log.printLine("Therefore, it is not being executed again");
                Log.printLine();

                // NOTE: If a Cloudlet has finished, then it won't be processed.
                // So, if ack is required, this method sends back a result.
                // If ack is not required, this method don't send back a result.
                // Hence, this might cause CloudSim to be hanged since waiting
                // for this Cloudlet back.
                if (ack) {
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = cl.getCloudletId();
                    data[2] = CloudSimTags.FALSE;

                    // unique tag = operation tag
                    int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                    sendNow(cl.getUserId(), tag, data);
                }

                sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

                return;
            }

            // process this Cloudlet to this CloudResource
            cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics().getCostPerBw());

            int userId = cl.getUserId();
            int vmId = cl.getVmId();

            double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles()); //time to transfer the files

            Host host = getVmAllocationPolicy().getHost(vmId, userId);
            Vm vm = host.getVm(vmId, userId);
            CloudletScheduler scheduler = vm.getCloudletScheduler();
            //System.out.println("cloudlet recieved by VM"+vmId);
            double estimatedFinishTime = scheduler.cloudletSubmit(cl,fileTransferTime);

            //if (estimatedFinishTime > 0.0 && estimatedFinishTime < getSchedulingInterval()) { //if this cloudlet is in the exec queue
            if (estimatedFinishTime > 0.0) { //if this cloudlet is in the exec queue
            	//double estimatedFinishTime = (cl.getCloudletTotalLength()/(capacity*cl.getPesNumber())); //time to process the cloudlet
            	//Log.printLine(estimatedFinishTime+"="+gl.getCloudletLength()+"/("+capacity+"*"+gl.getNumPE()+")");
            	estimatedFinishTime += fileTransferTime;
            	//estimatedFinishTime += CloudSim.clock();
            	//Log.printLine(CloudSim.clock()+": Next event scheduled to +"+estimatedFinishTime);
            	send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
            	
            	//event to update the stages
            	send(getId(), 0.0001, CloudSimTags.VM_DATACENTER_EVENT);
            }
            
            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = cl.getCloudletId();
                data[2] = CloudSimTags.TRUE;

                // unique tag = operation tag
                int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
                sendNow(cl.getUserId(), tag, data);
            }
        }
        catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        }
        catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }

    	checkCloudletCompletion();
    }

}
