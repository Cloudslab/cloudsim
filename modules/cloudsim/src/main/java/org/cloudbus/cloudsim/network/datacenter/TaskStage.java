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
 * Four stage types which are possible: {@link NetworkConstants#EXECUTION}, 
 * {@link NetworkConstants#WAIT_SEND}, {@link NetworkConstants#WAIT_RECV}, 
 * {@link NetworkConstants#FINISH}.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 * @todo Attributes should be defined as private.
 */
public class TaskStage {
	int vpeer;

        /**
         * The task type, either {@link NetworkConstants#EXECUTION}, 
         * {@link NetworkConstants#WAIT_SEND} or {@link NetworkConstants#WAIT_RECV}.
         * @todo It would be used enum instead of int constants.
         */
	int type;

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

        /** From whom data needed to be received or sent. */
	int peer;

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
}
