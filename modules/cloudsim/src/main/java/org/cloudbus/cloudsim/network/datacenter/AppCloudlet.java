package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

public class AppCloudlet {
	public static final int APP_MC = 1;
	public static final int APP_Workflow = 3;
	public AppCloudlet(int type, int appID, 
			double deadline, int numbervm, int userId) {
		super();
		this.type = type;
		this.appID = appID;
		this.deadline = deadline;
		this.numbervm = numbervm;
		this.userId = userId;
		clist=new ArrayList<NetworkCloudlet>();
	}
	public int type; //fft,fem
    public int appID;
    public ArrayList<NetworkCloudlet> clist;
    public double deadline;
    public double accuracy;
    public int numbervm;
    public int userId;
	public double exeTime;
    public int requestclass;
	
    public void createCloudletList(List<Integer> vmIdList)
    {
    	for(int i=0;i<numbervm;i++){
    		long length = 4;
    		long fileSize = 300;
    		long outputSize = 300;
    		long memory = 256;
    		int pesNumber = 4;
    		UtilizationModel utilizationModel = new UtilizationModelFull();
    		//HPCCloudlet cl=new HPCCloudlet();
    		NetworkCloudlet cl = new NetworkCloudlet(Constants.currentCloudletId, length, pesNumber, fileSize, outputSize, memory, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
    		Constants.currentCloudletId++;
    		cl.setUserId(userId);
			cl.submittime=CloudSim.clock();
//    		TaskStage ts=new TaskStage(Constants.EXECUTION, 0, length, 0,memory, (i+1)%numbervm);
//    		TaskStage ts1=new TaskStage(Constants.WAIT_SEND, 100, 0, 0, memory, (i+1)%numbervm);
//    		TaskStage ts2=new TaskStage(Constants.WAIT_RECV, 100, 0, 0, memory, (i+1)%numbervm);
//    		TaskStage ts3=new TaskStage(Constants.EXECUTION, 0, length, 0, memory, (i+1)%numbervm);
//			cl.stages.add(ts);
//    		cl.stages.add(ts1);
//    		cl.stages.add(ts2);
//    		cl.stages.add(ts3);
//    		cl.submittime=CloudSim.clock();
    		cl.currStagenum=-1;
    		clist.add(cl);
    		
    		
    	}
    	//based on type
    	
    }
}
