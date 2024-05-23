/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * TaskStage represents various stages a {@link NetworkCloudlet} can have during execution. 
 * Four stage types which are possible: EXECUTION, WAIT_SEND, WAIT_RECV,and FINISH.
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
 * //TODO Attributes should be defined as private.
 */
public class TaskStage {
	public enum TaskStageStatus {
		EXECUTION,
		WAIT_SEND,
		WAIT_RECV,
		FINISH;
	}
        /**
         * The task type
         */
	TaskStageStatus type;

        /**
         * The data length generated for the task (in bytes).
        */
	double data;

        /** Execution time for this stage. */
	double time;

        /** Stage (task) id. */
	double stageid;

        /** Memory used by the task. */
	long memory;

	/** Cloudlet where processing is done (if type == EXECUTION), or
	 * from whom data need to be received (if type == WAIT_RECV) or sent to (if type == WAIT_SEND). */
	NetworkCloudlet targetCloudlet;
	
	public TaskStage(TaskStageStatus type, double data, double time, double stageid, long memory, NetworkCloudlet cl) {
		super();
		this.type = type;
		this.data = data;
		this.time = time;
		this.stageid = stageid;
		this.memory = memory;
		this.targetCloudlet = cl;
	}
}
