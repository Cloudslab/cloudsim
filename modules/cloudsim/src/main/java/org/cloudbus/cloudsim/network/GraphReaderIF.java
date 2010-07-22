package org.cloudbus.cloudsim.network;

import java.io.IOException;

/**
 * this interface abstracts an reader for different graph-file-formats
 *
 * @author Thomas Hohnstein
 *
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
