/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cloudbus.cloudsim.network.DelayMatrix_Float;
import org.cloudbus.cloudsim.network.GraphReaderBrite;
import org.cloudbus.cloudsim.network.TopologicalGraph;
import org.cloudbus.cloudsim.network.TopologicalLink;
import org.cloudbus.cloudsim.network.TopologicalNode;

/**
 * Implements the network layer in CloudSim. It reads a file in the <a href="http://www.cs.bu.edu/brite/user_manual/node29.html">BRITE format</a>,
 * the <a href="http://www.cs.bu.edu/brite/">Boston university Representative Topology gEnerator</a>, and
 * generates a topological network from it. Information of this network is used to simulate latency
 * in network traffic of CloudSim.
 * <p/>
 * The topology file may contain more nodes than the number of entities in the simulation. It allows
 * users to increase the scale of the simulation without changing the topology file.
 * Nevertheless, each CloudSim entity must be mapped to one (and only one) BRITE node to allow
 * proper work of the network simulation. Each BRITE node can be mapped to only one entity at a
 * time.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 * 
 * @todo The class only have static methods, that indicates it doesn't have to be instantiated.
 * In fact, it doesn't appear it is being instantiated anywhere.
 * A private default constructor would be created to avoid instantiation.
 */
public class NetworkTopology {
        /**
         * The BRITE id to use for the next node to be created in the network.
         */
	protected static int nextIdx = 0;

	private static boolean networkEnabled = false;

        /**
         * A matrix containing the delay between every pair of nodes in the network.
         */
	protected static DelayMatrix_Float delayMatrix = null;

        /**
         * A matrix containing the bandwidth between every pair of nodes in the network.
         */
	protected static double[][] bwMatrix = null;

        /**
         * The Topological Graph of the network.
         */
	protected static TopologicalGraph graph = null;

	/**
         * The map between CloudSim entities and BRITE entities.
         * Each key is a CloudSim entity ID and each value the corresponding
         * BRITE entity ID.
         */
        protected static Map<Integer, Integer> map = null;

	/**
	 * Creates the network topology if the file exists and can be successfully parsed. File is
	 * written in the BRITE format and contains topological information on simulation entities.
	 * 
	 * @param fileName name of the BRITE file
	 * @pre fileName != null
	 * @post $none
	 */
	public static void buildNetworkTopology(String fileName) {
		Log.printConcatLine("Topology file: ", fileName);

		// try to find the file
		GraphReaderBrite reader = new GraphReaderBrite();

		try {
			graph = reader.readGraphFile(fileName);
			map = new HashMap<Integer, Integer>();
			generateMatrices();
		} catch (IOException e) {
			// problem with the file. Does not simulate network
			Log.printLine("Problem in processing BRITE file. Network simulation is disabled. Error: "
					+ e.getMessage());
		}

	}

	/**
	 * Generates the matrices used internally to set latency and bandwidth between elements.
	 */
	private static void generateMatrices() {
		// creates the delay matrix
		delayMatrix = new DelayMatrix_Float(graph, false);

		// creates the bw matrix
		bwMatrix = createBwMatrix(graph, false);

		networkEnabled = true;
	}

	/**
	 * Adds a new link in the network topology.
         * The CloudSim entities that represent the source and destination of the link
         * will be mapped to BRITE entities.
	 * 
	 * @param srcId ID of the CloudSim entity that represents the link's source node
	 * @param destId ID of the CloudSim entity that represents the link's destination node
	 * @param bw Link's bandwidth
	 * @param lat link's latency
	 * @pre srcId > 0
	 * @pre destId > 0
	 * @post $none
	 */
	public static void addLink(int srcId, int destId, double bw, double lat) {

		if (graph == null) {
			graph = new TopologicalGraph();
		}

		if (map == null) {
			map = new HashMap<Integer, Integer>();
		}

		// maybe add the nodes
		if (!map.containsKey(srcId)) {
			graph.addNode(new TopologicalNode(nextIdx));
			map.put(srcId, nextIdx);
			nextIdx++;
		}

		if (!map.containsKey(destId)) {
			graph.addNode(new TopologicalNode(nextIdx));
			map.put(destId, nextIdx);
			nextIdx++;
		}

		// generate a new link
		graph.addLink(new TopologicalLink(map.get(srcId), map.get(destId), (float) lat, (float) bw));

		generateMatrices();

	}

	/**
	 * Creates the matrix containing the available bandwidth between every pair of nodes.
	 * 
	 * @param graph topological graph describing the topology
	 * @param directed true if the graph is directed; false otherwise
	 * @return the bandwidth graph
	 */
	private static double[][] createBwMatrix(TopologicalGraph graph, boolean directed) {
		int nodes = graph.getNumberOfNodes();

		double[][] mtx = new double[nodes][nodes];

		// cleanup matrix
		for (int i = 0; i < nodes; i++) {
			for (int j = 0; j < nodes; j++) {
				mtx[i][j] = 0.0;
			}
		}

		Iterator<TopologicalLink> iter = graph.getLinkIterator();
		while (iter.hasNext()) {
			TopologicalLink edge = iter.next();

			mtx[edge.getSrcNodeID()][edge.getDestNodeID()] = edge.getLinkBw();

			if (!directed) {
				mtx[edge.getDestNodeID()][edge.getSrcNodeID()] = edge.getLinkBw();
			}
		}

		return mtx;
	}

	/**
	 * Maps a CloudSim entity to a BRITE node in the network topology.
	 * 
	 * @param cloudSimEntityID ID of the entity being mapped
	 * @param briteID ID of the BRITE node that corresponds to the CloudSim entity
	 * @pre cloudSimEntityID >= 0
	 * @pre briteID >= 0
	 * @post $none
	 */
	public static void mapNode(int cloudSimEntityID, int briteID) {
		if (networkEnabled) {
			try {
				// this CloudSim entity was already mapped?
				if (!map.containsKey(cloudSimEntityID)) {
					if (!map.containsValue(briteID)) { // this BRITE node was already mapped?
						map.put(cloudSimEntityID, briteID);
					} else {
						Log.printConcatLine("Error in network mapping. BRITE node ", briteID, " already in use.");
					}
				} else {
					Log.printConcatLine("Error in network mapping. CloudSim entity ", cloudSimEntityID,
							" already mapped.");
				}
			} catch (Exception e) {
				Log.printConcatLine("Error in network mapping. CloudSim node ", cloudSimEntityID,
						" not mapped to BRITE node ", briteID, ".");
			}
		}
	}

	/**
	 * Unmaps a previously mapped CloudSim entity to a BRITE node in the network topology.
	 * 
	 * @param cloudSimEntityID ID of the entity being unmapped
	 * @pre cloudSimEntityID >= 0
	 * @post $none
	 */
	public static void unmapNode(int cloudSimEntityID) {
		if (networkEnabled) {
			try {
				map.remove(cloudSimEntityID);
			} catch (Exception e) {
				Log.printConcatLine("Error in network unmapping. CloudSim node: ", cloudSimEntityID);
			}
		}
	}

	/**
	 * Calculates the delay between two nodes.
	 * 
	 * @param srcID ID of the CloudSim entity that represents the link's source node
	 * @param destID ID of the CloudSim entity that represents the link's destination node
	 * @return communication delay between the two nodes
	 * @pre srcID >= 0
	 * @pre destID >= 0
	 * @post $none
	 */
	public static double getDelay(int srcID, int destID) {
		if (networkEnabled) {
			try {
				// add the network latency
				double delay = delayMatrix.getDelay(map.get(srcID), map.get(destID));

				return delay;
			} catch (Exception e) {
				// in case of error, just keep running and return 0.0
			}
		}
		return 0.0;
	}

	/**
	 * Checks if the network simulation is working. If there were some problem during
	 * creation of network (e.g., during parsing of BRITE file) that does not allow a proper
	 * simulation of the network, this method returns false.
	 * 
	 * @return $true if network simulation is working, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public static boolean isNetworkEnabled() {
		return networkEnabled;
	}

}
