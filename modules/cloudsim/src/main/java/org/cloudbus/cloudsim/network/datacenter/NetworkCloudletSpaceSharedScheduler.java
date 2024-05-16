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
import org.cloudbus.cloudsim.core.CloudSimTags;

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
		// update
		double capacity = 0.0;
		int cpus = 0;

		for (Double mips : mipsShare) { // count the CPUs available to the VMM
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu

		for (ResCloudlet rcl : getCloudletExecList()) { // each machine in the
			// exec list has the
			// same amount of cpu

			NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();

			// check status
			// if execution stage
			// update the cloudlet finishtime
			// CHECK WHETHER IT IS WAITING FOR THE PACKET
			// if packet received change the status of job and update the time.
			//
			if ((cl.currStagenum != -1)) {
				if (cl.currStagenum == NetworkTags.FINISH) {
					break;
				}
				TaskStage st = cl.stages.get(cl.currStagenum);
				if (st.type == NetworkTags.EXECUTION) {

					// update the time
					cl.timespentInStage = Math.round(CloudSim.clock() - cl.timetostartStage);
					if (cl.timespentInStage >= st.time) {
						changetonextstage(cl, st);
						// change the stage
					}
				}
				if (st.type == NetworkTags.WAIT_RECV) {
					List<HostPacket> pktlist = pktrecv.get(st.targetCloudlet.getGuestId());
					List<HostPacket> pkttoremove = new ArrayList<>();
					if (pktlist != null) {
						Iterator<HostPacket> it = pktlist.iterator();
						HostPacket pkt = null;
						if (it.hasNext()) {
							pkt = it.next();
							// Asumption packet will not arrive in the same cycle
							if (pkt.receiverVmId == cl.getGuestId()) {
								pkt.recvTime = CloudSim.clock();
								st.time = CloudSim.clock() - pkt.sendTime;
								changetonextstage(cl, st);
								pkttoremove.add(pkt);
							}
						}
						pktlist.removeAll(pkttoremove);
						// if(pkt!=null)
						// else wait for recieving the packet
					}
				}

			} else {
				cl.currStagenum = 0;
				cl.timetostartStage = CloudSim.clock();

				if (cl.stages.get(0).type == NetworkTags.EXECUTION) {
					NetworkDatacenterBroker.linkDC.schedule(
							NetworkDatacenterBroker.linkDC.getId(),
							cl.stages.get(0).time,
							CloudSimTags.VM_DATACENTER_EVENT);
				} else {
					NetworkDatacenterBroker.linkDC.schedule(
							NetworkDatacenterBroker.linkDC.getId(),
							0.0001,
							CloudSimTags.VM_DATACENTER_EVENT);
					// /sendstage///
				}
			}

		}

		if (getCloudletExecList().size() == 0 && getCloudletWaitingList().size() == 0) { // no
			// more cloudlets in this scheduler
			setPreviousTime(currentTime);
			return 0.0;
		}

		// update each cloudlet
		int finished = 0;
		List<ResCloudlet> toRemove = new ArrayList<>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			// rounding issue...
			if (((NetworkCloudlet) (rcl.getCloudlet())).currStagenum == NetworkTags.FINISH) {
				// stage is changed and packet to send
				((NetworkCloudlet) (rcl.getCloudlet())).finishtime = CloudSim.clock();
				toRemove.add(rcl);
				cloudletFinish(rcl);
				finished++;
			}
		}
		getCloudletExecList().removeAll(toRemove);
		// add all the CloudletExecList in waitingList.
		// sort the waitinglist

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingList().isEmpty()) {
			for (int i = 0; i < finished; i++) {
				toRemove.clear();
				for (ResCloudlet rcl : getCloudletWaitingList()) {
					if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
						rcl.setCloudletStatus(Cloudlet.INEXEC);
						for (int k = 0; k < rcl.getNumberOfPes(); k++) {
							rcl.setMachineAndPeId(0, i);
						}
						getCloudletExecList().add(rcl);
						usedPes += rcl.getNumberOfPes();
						toRemove.add(rcl);
						break;
					}
				}
				getCloudletWaitingList().removeAll(toRemove);
			}// for(cont)
		}

		// estimate finish time of cloudlets in the execution queue
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double remainingLength = rcl.getRemainingCloudletLength();
			double estimatedFinishTime = currentTime + (remainingLength / (capacity * rcl.getNumberOfPes()));
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
         * //TODO It has to be corrected the method name case. Method too long
         * to understand what is its responsibility.*/
	private void changetonextstage(NetworkCloudlet cl, TaskStage st) {
		cl.timespentInStage = 0;
		cl.timetostartStage = CloudSim.clock();
		int currstage = cl.currStagenum;
		if (currstage >= (cl.stages.size() - 1)) {
			cl.currStagenum = NetworkTags.FINISH;
		} else {
			cl.currStagenum = currstage + 1;
			int i = 0;
			for (i = cl.currStagenum; i < cl.stages.size(); i++) {
				if (cl.stages.get(i).type == NetworkTags.WAIT_SEND) {
					HostPacket pkt = new HostPacket(cl, i);
					List<HostPacket> pktlist = pkttosend.get(cl.getGuestId());
					if (pktlist == null) {
						pktlist = new ArrayList<>();
					}
					pktlist.add(pkt);
					pkttosend.put(cl.getGuestId(), pktlist);

				} else {
					break;
				}

			}
			NetworkDatacenterBroker.linkDC.schedule(
					NetworkDatacenterBroker.linkDC.getId(),
					0.0001,
					CloudSimTags.VM_DATACENTER_EVENT);
			if (i == cl.stages.size()) {
				cl.currStagenum = NetworkTags.FINISH;
			} else {
				cl.currStagenum = i;
				if (cl.stages.get(i).type == NetworkTags.EXECUTION) {
					NetworkDatacenterBroker.linkDC.schedule(
							NetworkDatacenterBroker.linkDC.getId(),
							cl.stages.get(i).time,
							CloudSimTags.VM_DATACENTER_EVENT);
				}

			}
		}

	}
}
