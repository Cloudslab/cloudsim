/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * Taskstage represents various stages a networkCloudlet can have during execution. Four stage types
 * which are possible-> EXECUTION=0; WAIT_SEND=1; WAIT_RECV=2; FINISH=-2; Check NeworkConstants.java
 * file for that.
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
public class TaskStage {

	public TaskStage(int type, double data, double time, double stageid, long memory, int peer, int vpeer) {
		super();
		this.type = type;
		this.data = data;
		this.time = time;
		this.stageid = stageid;
		this.memory = memory;
		this.peer = peer;
		this.vpeer = vpeer;
	}

	int vpeer;

	int type;// execution, recv, send,

	double data;// data generated or send or recv

	double time;// execution time for this stage

	double stageid;

	long memory;

	int peer;// from whom data needed to be recieved or send

}
