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
 * This interface abstracts an reader for different graph-file-formats
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public interface GraphReaderIF {

	/**
	 * this method just reads the file and creates an TopologicalGraph object
	 * 
	 * @param filename name of the file to read
	 * @return created TopologicalGraph
	 * @throws IOException
	 */
	TopologicalGraph readGraphFile(String filename) throws IOException;

}
