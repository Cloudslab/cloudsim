/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;

/**
 * This class represents the coordinator of a federation of clouds.
 * It interacts with other clouds coordinators in order to exchange
 * virtual machines and user applicatoins, if required.
 *
 * @author		Rodrigo N. Calheiros
 * @since		CloudSim Toolkit 1.0
 */
public abstract class CloudCoordinator {

	/** The datacenter. */
	protected FederatedDatacenter datacenter;

	/** The federation. */
	protected List<Integer> federation;

	/**
	 * Defines the FederatedDataCenter this coordinator works for.
	 *
	 * @param datacenter FederatedDataCenter associated to this coordinator.
	 *
	 * @pre $none
	 * @post $none
	 */
	public void setDatacenter(FederatedDatacenter datacenter){
		this.datacenter = datacenter;
	}

	/**
	 * Informs about the other data centers that are part of the federation.
	 *
	 * @param federationList List of DataCenters ids that are part of the federation
	 *
	 * @pre federationList != null
	 * @post $none
	 */
	public void setFederation(List<Integer> federationList) {
		this.federation = federationList;
	}

	/**
	 * This method is periodically called by the FederatedDataCenter to
	 * makethe coordinator update the sensors measuring in order to decide
	 * if modification in the data center must be processed. This modification
	 * requires migration of virtual machines and/or user applications from
	 * one data center to another.
	 *
	 * @pre $none
	 * @post $none
	 */
	public void updateDatacenter() {
		for (Sensor<Double> s : this.datacenter.getSensors()) {
			int result = s.monitor();
			if (result != 0) {
				migrate(s, result);
			}
		}
	}

	/**
	 * Implements a specific migration policy to be deployed by the cloud coordinator.
	 *
	 * @param result the result vof the last measurement:
	 * -1 if the measurement fell below the lower threshold
	 * +1 if the measurement fell above the higher threshold
	 * @param sensor the sensor
	 *
	 * @pre sensor != null
	 * @post $none
	 */
	protected abstract void migrate(Sensor<Double> sensor,int result);

}