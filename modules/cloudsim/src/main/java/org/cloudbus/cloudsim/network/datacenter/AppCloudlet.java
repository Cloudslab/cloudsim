/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

/**
 * AppCloudlet class represents an application which user submit for execution within a datacenter. It
 * consist of several {@link NetworkCloudlet NetworkCloudlets}. The solely purpose of this class is to function
 * as a wrapper to keep track of the cloudlets which model a certain workflow, the owner of the app and to check
 * for deadline misses.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.o
 *
 * 
 * //@TODO The attributes have to be defined as private.
 */
public class AppCloudlet {

	public int type;

	public int appID;

	public int userId;

        /**
         * The list of {@link NetworkCloudlet} that this AppCloudlet represents.
         */
	public ArrayList<NetworkCloudlet> cList;

	public double deadline;

	public static final int APP_MC = 1;

	public static final int APP_Workflow = 3;

	public AppCloudlet(int type, int appID, double deadline, int userId) {
		// Access these parameters from within cList, NOT the appCloudlet object
		this.type = type;
		this.appID = appID;
		this.deadline = deadline;
		this.userId = userId;

		cList = new ArrayList<>();
	}

	/**
	 * Get all the cloudlets that starts the worklow activity
	 * Assumption: the stages are in order of (sequential) execution
	 */
	public List<NetworkCloudlet> getSourceCloudlets() {
		return cList.stream()
					.filter(networkCloudlet -> networkCloudlet.stages.getFirst().getType() != TaskStage.TaskStageStatus.WAIT_RECV)
					.filter(networkCloudlet -> networkCloudlet.stages.getFirst().getType() != TaskStage.TaskStageStatus.WAIT_SEND)
					.toList();
	}

	/**
	 * Get all the cloudlets that ends the worklow activity
	 * Assumption: the stages are in order of (sequential) execution
	 */
	public List<NetworkCloudlet> getSinkCloudlets() {
		return cList.stream()
				.filter(networkCloudlet -> networkCloudlet.stages.getLast().getType() != TaskStage.TaskStageStatus.WAIT_RECV)
				.filter(networkCloudlet -> networkCloudlet.stages.getLast().getType() != TaskStage.TaskStageStatus.WAIT_SEND)
				.toList();
	}

	/**
	 * Compute the lateness of the appCloudlet in function of the specified deadline.
	 * A negative value express a deadline miss, a positive value express an early finish
	 * @return lateness
	 */
	public double getLateness() {
		double worstStartTime = getSinkCloudlets().stream()
				.mapToDouble(Cloudlet::getExecStartTime).max().orElse(0.0);
		double worstFinishTime = getSinkCloudlets().stream()
				.mapToDouble(Cloudlet::getExecFinishTime).max().orElse(0.0);

		return deadline - (worstFinishTime - worstStartTime);
	}

	public boolean isDeadlineMissed() {
		return getLateness() < 0;
	}
}
