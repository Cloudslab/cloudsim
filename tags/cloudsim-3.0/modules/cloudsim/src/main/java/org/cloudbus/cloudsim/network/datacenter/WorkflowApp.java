/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.List;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * WorkflowApp is an example of AppCloudlet having three communicating tasks. Task A and B sends the
 * data (packet) while Task C receives them
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
public class WorkflowApp extends AppCloudlet {

	public WorkflowApp(int type, int appID, double deadline, int numbervm, int userId) {
		super(type, appID, deadline, numbervm, userId);
		exeTime = 100;
		this.numbervm = 3;
	}

	@Override
	public void createCloudletList(List<Integer> vmIdList) {
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		int memory = 100;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int i = 0;
		// Task A
		NetworkCloudlet cl = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		cl.numStage = 2;
		NetworkConstants.currentCloudletId++;
		cl.setUserId(userId);
		cl.submittime = CloudSim.clock();
		cl.currStagenum = -1;
		cl.setVmId(vmIdList.get(i));

		// first stage: big computation
		cl.stages.add(new TaskStage(NetworkConstants.EXECUTION, 0, 1000 * 0.8, 0, memory, vmIdList.get(0), cl
				.getCloudletId()));
		cl.stages.add(new TaskStage(NetworkConstants.WAIT_SEND, 1000, 0, 1, memory, vmIdList.get(2), cl
				.getCloudletId() + 2));
		clist.add(cl);
		i++;
		// Task B
		NetworkCloudlet clb = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		clb.numStage = 2;
		NetworkConstants.currentCloudletId++;
		clb.setUserId(userId);
		clb.submittime = CloudSim.clock();
		clb.currStagenum = -1;
		clb.setVmId(vmIdList.get(i));

		// first stage: big computation

		clb.stages.add(new TaskStage(
				NetworkConstants.EXECUTION,
				0,
				1000 * 0.8,
				0,
				memory,
				vmIdList.get(1),
				clb.getCloudletId()));
		clb.stages.add(new TaskStage(NetworkConstants.WAIT_SEND, 1000, 0, 1, memory, vmIdList.get(2), clb
				.getCloudletId() + 1));
		clist.add(clb);
		i++;

		// Task C
		NetworkCloudlet clc = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		clc.numStage = 2;
		NetworkConstants.currentCloudletId++;
		clc.setUserId(userId);
		clc.submittime = CloudSim.clock();
		clc.currStagenum = -1;
		clc.setVmId(vmIdList.get(i));

		// first stage: big computation
		clc.stages.add(new TaskStage(NetworkConstants.WAIT_RECV, 1000, 0, 0, memory, vmIdList.get(0), cl
				.getCloudletId()));
		clc.stages.add(new TaskStage(NetworkConstants.WAIT_RECV, 1000, 0, 1, memory, vmIdList.get(1), cl
				.getCloudletId() + 1));
		clc.stages.add(new TaskStage(
				NetworkConstants.EXECUTION,
				0,
				1000 * 0.8,
				1,
				memory,
				vmIdList.get(0),
				clc.getCloudletId()));

		clist.add(clc);

	}
}
