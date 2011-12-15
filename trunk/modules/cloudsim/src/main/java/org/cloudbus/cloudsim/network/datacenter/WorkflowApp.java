package org.cloudbus.cloudsim.network.datacenter;

import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

public class WorkflowApp extends AppCloudlet{

	public WorkflowApp(int type, int appID, double deadline, 
			int numbervm,  int userId) {
		super(type, appID, deadline,  numbervm, userId);
		// TODO Auto-generated constructor stub
		this.exeTime=100;
		this.numbervm=3;
	}
	public void createCloudletList(List<Integer> vmIdList){
		long fileSize = Constants.FILE_SIZE;
		long outputSize = Constants.OUTPUT_SIZE;
		int pesNumber = Constants.PES_NUMBER;
		int memory=100;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int i=0;
		//Task A
		NetworkCloudlet cl = new NetworkCloudlet(Constants.currentCloudletId, 0, 1, fileSize, outputSize, memory, utilizationModel, utilizationModel, utilizationModel);
	    cl.numStage=2;
	    Constants.currentCloudletId++;
  		cl.setUserId(userId);
  		cl.submittime=CloudSim.clock();
  		cl.currStagenum=-1;
  		cl.setVmId(vmIdList.get(i));
  		
  		//first stage: big computation
  		cl.stages.add(new TaskStage(Constants.EXECUTION, 0, 1000*0.8, 0, memory, vmIdList.get(0),cl.getCloudletId()));
  		cl.stages.add(new TaskStage(Constants.WAIT_SEND, 1000, 0, 1, memory, vmIdList.get(2),cl.getCloudletId()+2));
  		clist.add(cl);
  		i++;
  		//Task B
  		NetworkCloudlet clb = new NetworkCloudlet(Constants.currentCloudletId, 0, 1, fileSize, outputSize, memory, utilizationModel, utilizationModel, utilizationModel);
	    clb.numStage=2;
	    Constants.currentCloudletId++;
  		clb.setUserId(userId);
  		clb.submittime=CloudSim.clock();
  		clb.currStagenum=-1;
  		clb.setVmId(vmIdList.get(i));
  	
  		//first stage: big computation
  		
  		clb.stages.add(new TaskStage(Constants.EXECUTION, 0, 1000*0.8, 0, memory, vmIdList.get(1),clb.getCloudletId()));
  		clb.stages.add(new TaskStage(Constants.WAIT_SEND, 1000, 0, 1, memory, vmIdList.get(2),clb.getCloudletId()+1));
  		clist.add(clb);
  		i++;
  		
  		//Task C
  		NetworkCloudlet clc = new NetworkCloudlet(Constants.currentCloudletId, 0, 1, fileSize, outputSize, memory, utilizationModel, utilizationModel, utilizationModel);
	    clc.numStage=2;
	    Constants.currentCloudletId++;
  		clc.setUserId(userId);
  		clc.submittime=CloudSim.clock();
  		clc.currStagenum=-1;
  		clc.setVmId(vmIdList.get(i));
  	
  		//first stage: big computation
  		clc.stages.add(new TaskStage(Constants.WAIT_RECV, 1000, 0, 0, memory, vmIdList.get(0),cl.getCloudletId()));
  		clc.stages.add(new TaskStage(Constants.WAIT_RECV, 1000, 0, 1, memory, vmIdList.get(1),cl.getCloudletId()+1));
  		clc.stages.add(new TaskStage(Constants.EXECUTION, 0, 1000*0.8, 1, memory, vmIdList.get(0),clc.getCloudletId()));
  		
  		clist.add(clc);
  			
	}
}
