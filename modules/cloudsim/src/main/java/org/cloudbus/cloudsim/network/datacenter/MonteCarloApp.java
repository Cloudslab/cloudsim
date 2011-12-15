package org.cloudbus.cloudsim.network.datacenter;

import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

public class MonteCarloApp extends AppCloudlet {

	public MonteCarloApp(int type, int appID, double deadline, 	int numbervm, int userId) {
		super(type, appID, deadline, numbervm,userId);
		
		this.numbervm=this.getnumvm();
		this.exeTime=getExecTime()/this.numbervm;
	}
	
	@Override
    public void createCloudletList(List<Integer> vmIdList){
		//basically, each task runs the simulation and then data is consolidated in one task
		int executionTime = getExecTime();
		long memory = accuracyToMemory();
		long fileSize = Constants.FILE_SIZE;
		long outputSize = Constants.OUTPUT_SIZE;
		int pesNumber = Constants.PES_NUMBER;
		int stgId=0;
		int t=Constants.currentCloudletId;
	  	for(int i=0;i<numbervm;i++){
    		UtilizationModel utilizationModel = new UtilizationModelFull();
    		NetworkCloudlet cl = new NetworkCloudlet(Constants.currentCloudletId, executionTime/numbervm, pesNumber, fileSize, outputSize, memory, utilizationModel, utilizationModel, utilizationModel);
    		Constants.currentCloudletId++;
    		cl.setUserId(userId);
			cl.submittime=CloudSim.clock();
    		cl.currStagenum=-1;
			cl.setVmId(vmIdList.get(i));
			//compute and send data to node 0
    		cl.stages.add(new TaskStage(Constants.EXECUTION, Constants.COMMUNICATION_LENGTH, executionTime/numbervm, stgId++, memory, vmIdList.get(0),cl.getCloudletId()));
    		
    		//0 has an extra stage of waiting for results; others send
    		if (i==0){
    			for(int j=1;j<numbervm;j++)
    				cl.stages.add(new TaskStage(Constants.WAIT_RECV, Constants.COMMUNICATION_LENGTH, 0, stgId++, memory, vmIdList.get(j),cl.getCloudletId()+j));
    		} else {
    			cl.stages.add(new TaskStage(Constants.WAIT_SEND, Constants.COMMUNICATION_LENGTH, 0, stgId++, memory, vmIdList.get(0),t));
    		}
    		
    		clist.add(cl);    			
    	}	
	}
	public int getnumvm(){
		double exetime=getExecTime()/2;//for two vms
		if(this.deadline>exetime)
			return 2;
		else if(this.deadline>(exetime/4)) return 4;
			
		return 4;
	}
	
	private int getExecTime() {
		//use exec constraints as Binomial
		
		return 100;
	}

	private long accuracyToMemory() {
		//use same memory constraints as Binomial
		
		return 240076;
	}
}
