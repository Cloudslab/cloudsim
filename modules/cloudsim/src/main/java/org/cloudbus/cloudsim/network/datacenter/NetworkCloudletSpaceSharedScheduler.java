/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a virtual machine
 * to run its {@link Cloudlet Cloudlets}. 
 * It consider that there will be only one cloudlet per VM. Other cloudlets will be in a waiting list.
 * We consider that file transfer from cloudlets waiting happens before cloudlet execution. I.e.,
 * even though cloudlets must wait for CPU, data transfer happens as soon as cloudlets are
 * submitted.
 * 
 * Each VM has to have its own instance of a CloudletScheduler.
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 3.0
 * //TODO Attributes should be private
 */
public class NetworkCloudletSpaceSharedScheduler extends CloudletSchedulerSpaceShared {
        /**
         * The map of packets to send, where each key is a destination VM
         * and each value is the list of packets to sent to that VM.
         */
	public Map<Integer, List<HostPacket>> pkttosend;

        /**
         * The map of packets received, where each key is a sender VM
         * and each value is the list of packets sent by that VM.
         */
	public Map<Integer, List<HostPacket>> pktrecv;

	/**
	 * Creates a new CloudletSchedulerSpaceShared object. 
         * This method must be invoked before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public NetworkCloudletSpaceSharedScheduler() {
		super();
		pkttosend = new HashMap<>();
		pktrecv = new HashMap<>();
	}

	@Override
	public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
                /*//TODO Method to long. Several "extract method" refactorings may be performed.*/
		setCurrentMipsShare(mipsShare);
		double capacity = getCPUCapacity(mipsShare);

		for (ResCloudlet rcl : getCloudletExecList()) { // each machine in the
			// exec list has the
			// same amount of cpu

			NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();

			// check status
			// if execution stage
			// update the cloudlet execFinishTime
			// CHECK WHETHER IT IS WAITING FOR THE PACKET
			// if packet received change the status of job and update the time.
			//
			if (cl.currStageNum != -1) {
				if (cl.currStageNum == -2) {
					break;
				}
				TaskStage st = cl.stages.get(cl.currStageNum);
				if (st.getType() == TaskStage.TaskStageStatus.EXECUTION) {

					// update the time
					cl.timeSpentCurrStage = Math.round(CloudSim.clock() - cl.startTimeCurrStage);
					if (cl.timeSpentCurrStage >= st.getTime()) {
						goToNextStage(cl);
					}
				}
				if (st.getType() == TaskStage.TaskStageStatus.WAIT_RECV) {
					List<HostPacket> pktlist = pktrecv.get(st.getTargetCloudlet().getGuestId());
					List<HostPacket> pkttoremove = new ArrayList<>();
					if (pktlist != null) {
						Iterator<HostPacket> it = pktlist.iterator();
						HostPacket pkt = null;
						if (it.hasNext()) {
							pkt = it.next();
							// Asumption packet will not arrive in the same cycle
							if (pkt.receiverVmId == cl.getGuestId()) {
								pkt.recvTime = CloudSim.clock();
								st.setTime(CloudSim.clock() - pkt.sendTime);
								goToNextStage(cl);
								pkttoremove.add(pkt);
							}
						}
						pktlist.removeAll(pkttoremove);
						// if(pkt!=null)
						// else wait for recieving the packet
					}
				}

			} else {
				cl.currStageNum = 0;
				cl.startTimeCurrStage = CloudSim.clock();
			}

		}

		// check finished cloudlets
		List<ResCloudlet> toRemove = new ArrayList<>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			// rounding issue...
			if (((NetworkCloudlet) rcl.getCloudlet()).currStageNum == -2) {
				toRemove.add(rcl);
				cloudletFinish(rcl);
			}
		}
		getCloudletExecList().removeAll(toRemove);

		// no more cloudlets in this scheduler
		if (getCloudletExecList().isEmpty() && getCloudletWaitingList().isEmpty()) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// add all the CloudletExecList in waitingList.
		// sort the waitinglist
		// @TODO: Remo Andreoli: Missing ???

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingList().isEmpty()) {
			int finished = toRemove.size();
			List<ResCloudlet> toUnpause = getCloudletWaitingList().stream()
							.filter(rcl -> (currentCPUs - usedPes) >= rcl.getNumberOfPes()).limit(finished).toList();

			for (ResCloudlet rcl : toUnpause) {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.INEXEC);
				for (int k = 0; k < rcl.getNumberOfPes(); k++) {
					rcl.setMachineAndPeId(0, k);
				}
				getCloudletExecList().add(rcl);
				usedPes += rcl.getNumberOfPes();
			}

			getCloudletWaitingList().removeAll(toUnpause);
		}

		// estimate finish time of cloudlets in the execution queue
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double estimatedFinishTime = getEstimatedFinishTime(rcl, currentTime);
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}

		setPreviousTime(currentTime);
		return nextEvent;
	}

	/**
	 * Changes a cloudlet to the next stage.
	 *
	 */
	private void goToNextStage(NetworkCloudlet cl) {
		cl.timeSpentCurrStage = 0;
		cl.startTimeCurrStage = CloudSim.clock();
		cl.currStageNum++;

		while(cl.currStageNum < cl.stages.size() && cl.stages.get(cl.currStageNum).getType() == TaskStage.TaskStageStatus.WAIT_SEND) {
			HostPacket pkt = new HostPacket(cl, cl.currStageNum);
			List<HostPacket> pktlist = pkttosend.get(cl.getGuestId());
			if (pktlist == null) {
				pktlist = new ArrayList<>();
			}
			pktlist.add(pkt);
			pkttosend.put(cl.getGuestId(), pktlist);

			cl.currStageNum++;
		}

		if (cl.currStageNum >= cl.stages.size()) {
			cl.currStageNum = -2;
		}
	}
}
