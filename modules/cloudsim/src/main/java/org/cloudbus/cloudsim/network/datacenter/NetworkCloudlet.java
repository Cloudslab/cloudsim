package org.cloudbus.cloudsim.network.datacenter;


import java.util.ArrayList;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;


public class NetworkCloudlet extends Cloudlet implements Comparable{
	
	long memory;

	public NetworkCloudlet(int cloudletId, long cloudletLength,
			int pesNumber, long cloudletFileSize, long cloudletOutputSize, long memory,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		// TODO Auto-generated constructor stub
		currStagenum=-1;
		this.memory=memory;
		stages=new ArrayList<TaskStage>();
	}
	public double submittime;
	public double finishtime;
	public double exetime;
    public double numStage;
	public int currStagenum;
	public double timetostartStage;
	public double timespentInStage;
	public Map<Double, NetPacket> timeCommunicate;
	
	public ArrayList<TaskStage> stages;
	
	public double starttime;
	


	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		NetworkCloudlet s1=(NetworkCloudlet)arg0;
		int alpha=0;
		
		
		return 0;
	}



	public double getSubmittime() {
		// TODO Auto-generated method stub
		return submittime;
	};
	
    
}
