/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * NetworkCloudlet class extends Cloudlet to support simulation of complex applications. Each such
 * a network Cloudlet represents a task of the application. Each task consists of several stages.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 * //TODO Attributes should be private
 * //TODO The different cloudlet classes should have a class hierarchy, by means
 * of a super class and/or interface.
 */
public class NetworkCloudlet extends Cloudlet implements Comparable<NetworkCloudlet> {
	/** Current stage of cloudlet execution. */
	public int currStageNum;

	/** Star time of the current stage.*/
	public double startTimeCurrStage;

	/** Time spent in the current stage.*/
	public double timeSpentCurrStage;

	/** All sequential stages that the cloudlet executes.
	 * ASSUMPTION: array order is significant */
	public ArrayList<TaskStage> stages;

	public NetworkCloudlet(
			int cloudletId,
			long cloudletLength,
			int pesNumber,
			long cloudletFileSize,
			long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(
				cloudletId,
				cloudletLength,
				pesNumber,
				cloudletFileSize,
				cloudletOutputSize,
				utilizationModelCpu,
				utilizationModelRam,
				utilizationModelBw);

		currStageNum = -1;
		stages = new ArrayList<>();
	}

	public int getNumberOfStages() { return stages.size(); }

	@Override
	public long getCloudletLength() {
		if (currStageNum == -1) { // not started yet
			return (long) stages.get(0).getTaskLength();
		}

		if (currStageNum >= stages.size()) { // finished
			return 0;
		}

		return (long) stages.get(currStageNum).getTaskLength();
				/*return (long) stages.stream()
				.filter(taskStage -> taskStage.getType() == TaskStage.TaskStageStatus.EXECUTION)
				.mapToDouble(TaskStage::getTime).sum();*/
	}

	@Override
	public boolean isFinished() {
		return currStageNum != -1 && currStageNum >= stages.size();
	}

	public void addExecutionStage(double execLength) {
		stages.add(
				new TaskStage(TaskStage.TaskStageStatus.EXECUTION, execLength, stages.size(), this));
		//@TODO: Remo Andreoli: setCloudletLength((long) (getCloudletLength()+execTime));
	}

	public void addSendStage(double data, NetworkCloudlet receiverCl) {
		stages.add(
				new TaskStage(TaskStage.TaskStageStatus.WAIT_SEND, data, stages.size(), receiverCl));

		//@TODO: Remo Andreoli: setCloudletLength((long) (getCloudletLength()+ data*transmissionTime));
	}

	public void addRecvStage(NetworkCloudlet senderCl) {
		stages.add(
				new TaskStage(TaskStage.TaskStageStatus.WAIT_RECV, 0, stages.size(), senderCl));

		//@TODO: Remo Andreoli: setCloudletLength((long) (getCloudletLength()+ expectedWaitTime));
	}

	/** Provide statistics on the execution times of each stage */
	public void stats() {
		System.out.println("NetworkCloudlet #"+getCloudletId());
		for (TaskStage stage : stages) {
			System.out.println("	"+stage.getType().toString()+" "+Math.max(0, stage.getTime()));
		}
	}

	@Override
	public int compareTo(NetworkCloudlet arg0) {
		return 0;
	}
}
