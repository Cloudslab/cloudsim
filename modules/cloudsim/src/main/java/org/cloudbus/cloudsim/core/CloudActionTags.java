/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.Datacenter;

/**
 * Contains various static tags that indicate a type of action that needs to be undertaken
 * by CloudSim entities when they receive or send events.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Anthony Sulistio
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public enum CloudActionTags implements CloudSimTags {
	BLANK,

	/** Schedules an entity without any delay. */
	SCHEDULE_NOW,

	/** Denotes the end of simulation. */
	END_OF_SIMULATION,

	/**
	 * Denotes an abrupt end of simulation. That is, one event of this type is enough for
	 * {@link CloudSimShutdown} to trigger the end of the simulation
	 */
	ABRUPT_END_OF_SIMULATION,

	/**
	 * Denotes insignificant simulation entity or time. This tag will not be used for identification
	 * purposes.
	 */
	INSIGNIFICANT,

	/** Sends an Experiment object between UserEntity and Broker entity */
	EXPERIMENT,

	/**
	 * Denotes a cloud resource to be registered. This tag is normally used between
	 * {@link CloudInformationService} and CloudResouce entities.
	 */
	REGISTER_RESOURCE,

	/**
	 * Denotes a cloud resource to be registered, that can support advance reservation. This tag is
	 * normally used between {@link CloudInformationService} and CloudResouce entity.
	 */
	REGISTER_RESOURCE_AR,

	/**
	 * Denotes a list of all hostList's, including the ones that can support advance reservation. This
	 * tag is normally used between {@link CloudInformationService} and CloudSim entity.
	 */
	RESOURCE_LIST,

	/**
	 * Denotes a list of hostList's that only support advance reservation. This tag is normally used
	 * between {@link CloudInformationService} and CloudSim entity.
	 */
	RESOURCE_AR_LIST,

	/**
	 * Denotes cloud resource characteristics information. This tag is normally used between CloudSim
	 * and CloudResource entity.
	 */
	RESOURCE_CHARACTERISTICS,

	/**
	 * Denotes cloud resource allocation policy. This tag is normally used between CloudSim and
	 * CloudResource entity.
	 */
	RESOURCE_DYNAMICS,

	/**
	 * Denotes a request to get the total number of Processing Elements (PEs) of a resource. This
	 * tag is normally used between CloudSim and CloudResource entity.
	 */
	RESOURCE_NUM_PE,

	/**
	 * Denotes a request to get the total number of free Processing Elements (PEs) of a resource.
	 * This tag is normally used between CloudSim and CloudResource entity.
	 */
	RESOURCE_NUM_FREE_PE,

	/**
	 * Denotes a request to record events for statistical purposes. This tag is normally used
	 * between CloudSim and CloudStatistics entity.
	 */
	RECORD_STATISTICS,

	/** Denotes a request to get a statistical list. */
	RETURN_STAT_LIST,

	/**
	 * Denotes a request to send an Accumulator object based on category into an event scheduler.
	 * This tag is normally used between ReportWriter and CloudStatistics entity.
	 */
	RETURN_ACC_STATISTICS_BY_CATEGORY,

	/**
	 * Denotes a request to register a CloudResource entity to a regional 
	 * {@link CloudInformationService} (CIS) entity.
	 */
	REGISTER_REGIONAL_GIS,

	/**
	 * Denotes a request to get a list of other regional CIS entities from the system CIS entity.
	 */
	REQUEST_REGIONAL_GIS,

	/**
	 * Denotes request for cloud resource characteristics information. This tag is normally used
	 * between CloudSim and CloudResource entity.
	 */
	RESOURCE_CHARACTERISTICS_REQUEST,

	/** This tag is used by an entity to send ping requests. */
	INFOPKT_SUBMIT,

	/** This tag is used to return the ping request back to sender. */
	INFOPKT_RETURN,

	/**
	 * Denotes the return of a Cloudlet back to sender. 
         * This tag is normally used by CloudResource entity.
	 */
	CLOUDLET_RETURN,

	/**
	 * Denotes the submission of a Cloudlet. 
         * This tag is normally used between CloudSim User and CloudResource entity.
	 */
	CLOUDLET_SUBMIT,

	/**
	 * Denotes the submission of a Cloudlet with an acknowledgement. This tag is normally used
	 * between CloudSim User and CloudResource entity.
	 */
	CLOUDLET_SUBMIT_ACK,

	/** Cancels a Cloudlet submitted in the CloudResource entity. */
	CLOUDLET_CANCEL,

	/** Denotes the status of a Cloudlet. */
	CLOUDLET_STATUS,

	/** Pauses a Cloudlet submitted in the CloudResource entity. */
	CLOUDLET_PAUSE,

	/**
	 * Pauses a Cloudlet submitted in the CloudResource entity with an acknowledgement.
	 */
	CLOUDLET_PAUSE_ACK,

	/** Resumes a Cloudlet submitted in the CloudResource entity. */
	CLOUDLET_RESUME,

	/**
	 * Resumes a Cloudlet submitted in the CloudResource entity with an acknowledgement.
	 */
	CLOUDLET_RESUME_ACK,

	/** Moves a Cloudlet to another CloudResource entity. */
	CLOUDLET_MOVE,

	/**
	 * Moves a Cloudlet to another CloudResource entity with an acknowledgement.
	 */
	CLOUDLET_MOVE_ACK,

	/**
	 * Denotes a request to create a new VM in a {@link Datacenter}
	 * with acknowledgement information sent by the Datacenter.
	 */
	VM_CREATE,

	/**
	 * Denotes a request to create a new VM in a {@link Datacenter} 
	 * without acknowledgement information sent by the Datacenter.
	 */
	VM_CREATE_ACK,

	/**
	 * Denotes a request to destroy a new VM in a {@link Datacenter}.
	 * without acknowledgement information sent by the Datacener.
	 */
	VM_DESTROY,

	/**
	 * Denotes a request to destroy a new VM in a {@link Datacenter} 
	 * with acknowledgement information sent by the Datacener.
	 */
	VM_DESTROY_ACK,

	/**
	 * Denotes a request to migrate a new VM in a {@link Datacenter}.
	 */
	VM_MIGRATE,

	/**
	 * Denotes a request to migrate a new VM in a {@link Datacenter}  
         * with acknowledgement information sent by the Datacener.
	 */
	VM_MIGRATE_ACK,

	/**
	 * Denotes an event to send a file from a user to a {@link Datacenter}.
	 */
	VM_DATA_ADD,

	/**
	 * Denotes an event to send a file from a user to a {@link Datacenter}
         * with acknowledgement information sent by the Datacener.
	 */
	VM_DATA_ADD_ACK,

	/**
	 * Denotes an event to remove a file from a {@link Datacenter} .
	 */
	VM_DATA_DEL,

	/**
	 * Denotes an event to remove a file from a {@link Datacenter}
         * with acknowledgement information sent by the Datacener.
	 */
	VM_DATA_DEL_ACK,

	/**
	 * Denotes an internal event generated in a {@link Datacenter}.
	 */
	VM_DATACENTER_EVENT,

	/**
	 * Denotes an internal event generated in a Broker.
	 */
	VM_BROKER_EVENT,

	NETWORK_PKT_UP,

	NETWORK_PKT_FORWARD,

	NETWORK_ATTACH_HOST,

	NETWORK_PKT_DOWN,

    NETWORK_PKT_REACHED_HOST
}
