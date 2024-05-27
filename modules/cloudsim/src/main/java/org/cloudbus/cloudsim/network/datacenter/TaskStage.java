/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import lombok.Getter;
import lombok.Setter;

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
	@Getter
	private TaskStageStatus type;

	/**
	 * The data length generated for the task (in bytes).
	*/
	@Getter
	private double data;

	/** Execution time for this stage.
	 * @NOTE: this variable is modified at run-time
	 */
	@Getter @Setter
	private double time;

	/** Stage (task) id. */
	@Getter
	private final double stageId;

	/** Cloudlet where processing is done (if type == EXECUTION), or
	 * from whom data need to be received (if type == WAIT_RECV) or sent to (if type == WAIT_SEND). */
	@Getter
	private NetworkCloudlet targetCloudlet;
	
	public TaskStage(TaskStageStatus type, double data, double time, double stageId, NetworkCloudlet cl) {
		super();
		this.type = type;
		this.data = data;
		this.time = time;
		this.stageId = stageId;
		this.targetCloudlet = cl;
	}
}
