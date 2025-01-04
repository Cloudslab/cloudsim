/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.core.CloudSim;

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
 * //@TODO Attributes should be private
 * //@TODO The different cloudlet classes should have a class hierarchy, by means
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

	/** Communication channel */
	private NetworkInterfaceCard nic;

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

		currStageNum = 0;
		startTimeCurrStage = -1;
		timeSpentCurrStage = -1;

		stages = new ArrayList<>();
		nic = new NetworkInterfaceCard();
	}

	public NetworkInterfaceCard getNic() { return nic; }

	@Override
	public boolean updateCloudlet(Object info) {
		if (currStageNum >= stages.size()) {
			return false;
		}

		if (startTimeCurrStage == -1) {
			startTimeCurrStage = getSubmissionTime();
		}

		// if execution stage, update the cloudlet execFinishTime
		// CHECK WHETHER IT IS WAITING FOR THE PACKET
		// if packet received change the status of job and update the time.
		TaskStage st = stages.get(currStageNum);
		if (st.getType() == TaskStage.TaskStageStatus.EXECUTION) {
			// update the time
			timeSpentCurrStage = CloudSim.clock() - startTimeCurrStage;

			if (getRemainingCloudletLength() == 0) {
				st.setTime(timeSpentCurrStage);
				goToNextStage();
			} else {
				return true;
			}
		}
		if (st.getType() == TaskStage.TaskStageStatus.WAIT_RECV) {
			Iterator<HostPacket> iter = nic.getReceivedPkts().iterator();
			HostPacket pkt;
			if (iter.hasNext()) {
				pkt = iter.next();
				// Assumption: packet will not arrive in the same cycle
				if (pkt.receiverGuestId == getGuestId()) {
					pkt.recvTime = CloudSim.clock();
					st.setTime(CloudSim.clock() - pkt.sendTime);
					goToNextStage();
					iter.remove();
				}
			}

			return false;
		}

		return true;
	}

	public int getNumberOfStages() { return stages.size(); }

	@Override
	public long getCloudletLength() {
		int currStage = Math.min(currStageNum, stages.size()-1);
		return stages.get(currStage).getTaskLength();
	}

	@Override
	public boolean isFinished() {
		return currStageNum >= stages.size();
	}

	public void addExecutionStage(long execLength) {
		stages.add(
				new TaskStage(TaskStage.TaskStageStatus.EXECUTION, execLength, stages.size(), this));
		//@TODO: setCloudletLength((long) (getCloudletLength()+execTime));
	}

	public void addSendStage(long data, NetworkCloudlet receiverCl) {
		stages.add(
				new TaskStage(TaskStage.TaskStageStatus.WAIT_SEND, data, stages.size(), receiverCl));

		//@TODO: setCloudletLength((long) (getCloudletLength()+ data*transmissionTime));
	}

	public void addRecvStage(NetworkCloudlet senderCl) {
		stages.add(
				new TaskStage(TaskStage.TaskStageStatus.WAIT_RECV, 0, stages.size(), senderCl));

		//@TODO: setCloudletLength((long) (getCloudletLength()+ expectedWaitTime));
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

	/**
	 * Changes a cloudlet to the next stage.
	 *
	 */
	private void goToNextStage() {
		timeSpentCurrStage = 0;
		startTimeCurrStage = CloudSim.clock();

		currStageNum++;
		while(currStageNum < stages.size() && stages.get(currStageNum).getType() == TaskStage.TaskStageStatus.WAIT_SEND) {
			HostPacket pkt = new HostPacket(this, currStageNum);

			nic.getPktsToSend().add(pkt);
			currStageNum++;
		}
	}
}
