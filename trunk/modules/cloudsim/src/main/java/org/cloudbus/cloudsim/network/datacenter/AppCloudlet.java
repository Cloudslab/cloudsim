/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * AppCloudlet class represents an application which user submit for execution within datacenter. It
 * consist of several networkClouds.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */
public class AppCloudlet {

	public static final int APP_MC = 1;

	public static final int APP_Workflow = 3;

	public AppCloudlet(int type, int appID, double deadline, int numbervm, int userId) {
		super();
		this.type = type;
		this.appID = appID;
		this.deadline = deadline;
		this.numbervm = numbervm;
		this.userId = userId;
		clist = new ArrayList<NetworkCloudlet>();
	}

	public int type;

	public int appID;

	public ArrayList<NetworkCloudlet> clist;

	public double deadline;

	public double accuracy;

	public int numbervm;

	public int userId;

	public double exeTime;

	public int requestclass;

	/**
	 * An example of creating APPcloudlet
	 * 
	 * @param vmIdList VMs where Cloudlet will be executed
	 */
	public void createCloudletList(List<Integer> vmIdList) {
		for (int i = 0; i < numbervm; i++) {
			long length = 4;
			long fileSize = 300;
			long outputSize = 300;
			long memory = 256;
			int pesNumber = 4;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			// HPCCloudlet cl=new HPCCloudlet();
			NetworkCloudlet cl = new NetworkCloudlet(
					NetworkConstants.currentCloudletId,
					length,
					pesNumber,
					fileSize,
					outputSize,
					memory,
					utilizationModel,
					utilizationModel,
					utilizationModel);
			// setting the owner of these Cloudlets
			NetworkConstants.currentCloudletId++;
			cl.setUserId(userId);
			cl.submittime = CloudSim.clock();
			cl.currStagenum = -1;
			clist.add(cl);

		}
		// based on type

	}
}
