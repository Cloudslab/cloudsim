/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
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
	private TaskStageStatus type;

	/**
	 * The length of the task based on the type of operation performed.
	 * It may be:
	 * -) the execution length, in MI (type == EXECUTION)
	 * -) the amount of data to be sent, in bytes (type == WAIT_RECV)
	*/
	private long taskLength;

	/** Execution time for this stage.
	 * @NOTE: this variable is modified at run-time
	 */
	private double time;

	/** Stage (task) id. */
	private final double stageId;

	/**
	 * The targeted cloudlet based on the type of operation performed by the task.
	 * It may be:
	 * -) The cloudlet where processing is done (type == EXECUTION)
	 * -) The cloudlet from whom taskLength need to be received (type == WAIT_RECV)
	 * -) The cloudlet to whom taskLength need to be sent to (type == WAIT_SEND).
	 */
	private NetworkCloudlet targetCloudlet;
	
	public TaskStage(TaskStageStatus type, long taskLength, double stageId, NetworkCloudlet cl) {
		super();
		this.type = type;
		this.taskLength = taskLength;
		this.time = 0;
		this.stageId = stageId;

		this.targetCloudlet = cl;
	}

	public TaskStageStatus getType() { return type; }

	public long getTaskLength() { return taskLength; }

	public double getTime() { return time; }
	public void setTime(double time) { this.time = time; }

	public double getStageId() { return stageId; }

	public NetworkCloudlet getTargetCloudlet() { return targetCloudlet; }
}
