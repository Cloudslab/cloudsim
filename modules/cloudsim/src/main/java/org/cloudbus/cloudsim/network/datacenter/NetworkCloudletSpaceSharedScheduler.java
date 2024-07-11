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

import lombok.Getter;
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
	@Getter
	private Map<Integer, List<HostPacket>> pktToSend;

	/**
	 * The map of packets received, where each key is a sender VM
	 * and each value is the list of packets sent by that VM.
	 */
	@Getter
	private Map<Integer, List<HostPacket>> receivedPkts;

	/**
	 * Creates a new CloudletSchedulerSpaceShared object. 
         * This method must be invoked before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public NetworkCloudletSpaceSharedScheduler() {
		super();
		pktToSend = new HashMap<>();
		receivedPkts = new HashMap<>();
	}

	@Override
	protected void updateExecutingCloudlet(ResCloudlet rcl, double currentTime, Object info) {
		// @TODO: Remo Andreoli: ugly instanceof check; fix it
		if (!(rcl.getCloudlet() instanceof NetworkCloudlet ncl)) {
			super.updateExecutingCloudlet(rcl, currentTime, info);
			return;
		}

        if (ncl.currStageNum >= ncl.stages.size()) {
			return;
		}

		if (ncl.currStageNum == -1) {
			ncl.currStageNum = 0;
			ncl.startTimeCurrStage = CloudSim.clock();
		}

		// if execution stage, update the cloudlet execFinishTime
		// CHECK WHETHER IT IS WAITING FOR THE PACKET
		// if packet received change the status of job and update the time.
		TaskStage st = ncl.stages.get(ncl.currStageNum);
		if (st.getType() == TaskStage.TaskStageStatus.EXECUTION) {

			// update the time
			ncl.timeSpentCurrStage = CloudSim.clock() - ncl.startTimeCurrStage;
			if (rcl.getRemainingCloudletLength() == 0) {
				st.setTime(ncl.timeSpentCurrStage);
				goToNextStage(ncl);
			} else {
				super.updateExecutingCloudlet(rcl, currentTime, info);
			}
			/*if (ncl.timeSpentCurrStage >= st.getTime()) {
				goToNextStage(ncl);
			}*/
		}
		if (st.getType() == TaskStage.TaskStageStatus.WAIT_RECV) {
			List<HostPacket> pktlist = getReceivedPkts().get(st.getTargetCloudlet().getGuestId());
			List<HostPacket> pkttoremove = new ArrayList<>();
			if (pktlist != null) {
				Iterator<HostPacket> it = pktlist.iterator();
				HostPacket pkt;
				if (it.hasNext()) {
					pkt = it.next();
					// Assumption: packet will not arrive in the same cycle
					if (pkt.receiverGuestId == ncl.getGuestId()) {
						pkt.recvTime = CloudSim.clock();
						st.setTime(CloudSim.clock() - pkt.sendTime);
						goToNextStage(ncl);
						pkttoremove.add(pkt);
					}
				}
				pktlist.removeAll(pkttoremove);
				// if(pkt!=null)
				// else wait for recieving the packet
			}
		}
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
			List<HostPacket> pktlist = getPktToSend().get(cl.getGuestId());
			if (pktlist == null) {
				pktlist = new ArrayList<>();
			}
			pktlist.add(pkt);
			getPktToSend().put(cl.getGuestId(), pktlist);

			cl.currStageNum++;
		}
	}
}
