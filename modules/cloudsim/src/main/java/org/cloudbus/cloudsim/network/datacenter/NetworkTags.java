/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * This class contains additional tags for the DataCloud functionalities, such as file information
 * retrieval, file transfers, and storage info.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public final class NetworkTags {
	/** Types of TaskStage */
	public static final int EXECUTION = 0;
	public static final int WAIT_SEND = 1;
	public static final int WAIT_RECV = 2;
	public static final int FINISH = -2;

	/** Switch level in datacenter topology.
	 * Root switch connects the Datacenter to external network.
	 * Aggregate switches reside in-between the root switch and the edge switches.
	 * Edge switches have their downlink ports connected to hosts.
	 */
	public static final int ROOT_LEVEL = 0;
	public static final int AGGR_LEVEL = 1;
	public static final int EDGE_LEVEL = 2;

	public static final int MAX_ITERATION = 10;

	/** Global counter */
	public static int totaldatatransfer = 0;

	/** Private Constructor. */
	private NetworkTags() {
		throw new UnsupportedOperationException("NetworkTags cannot be instantiated");
	}

}
