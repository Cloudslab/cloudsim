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
public final class NetworkGlobals {
	public static final int MAX_ITERATION = 10;

	/** Global counter */
	public static int totaldatatransfer = 0;

	/** Private Constructor. */
	private NetworkGlobals() {
		throw new UnsupportedOperationException("NetworkGlobals cannot be instantiated");
	}

}
