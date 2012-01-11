/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * NetworkCloudlet class extends Cloudlet to support simulation of complex applications. Each such
 * network Cloudlet represents a task of the application. Each task consists of several stages.
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
public class NetworkCloudlet extends Cloudlet implements Comparable<Object> {

	long memory;

	public NetworkCloudlet(
			int cloudletId,
			long cloudletLength,
			int pesNumber,
			long cloudletFileSize,
			long cloudletOutputSize,
			long memory,
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

		currStagenum = -1;
		this.memory = memory;
		stages = new ArrayList<TaskStage>();
	}

	public double submittime; // time when cloudlet will be submitted

	public double finishtime; // time when cloudlet finish execution

	public double exetime; // execution time for cloudlet

	public double numStage;// number of stages in cloudlet

	public int currStagenum; // current stage of cloudlet execution

	public double timetostartStage;

	public double timespentInStage; // how much time spent in particular stage

	public Map<Double, HostPacket> timeCommunicate;

	public ArrayList<TaskStage> stages; // all stages which cloudlet execution

	// consists of.

	public double starttime;

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	public double getSubmittime() {
		return submittime;
	}

}
