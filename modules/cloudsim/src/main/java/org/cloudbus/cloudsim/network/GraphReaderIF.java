/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.io.IOException;

/**
 * An interface to abstract a reader for different graph file formats.
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public interface GraphReaderIF {

	/**
	 * Reads a file and creates an {@link TopologicalGraph} object.
	 * 
	 * @param filename Name of the file to read
	 * @return The created TopologicalGraph
	 * @throws IOException when the file cannot be accessed
	 */
	TopologicalGraph readGraphFile(String filename) throws IOException;

}
