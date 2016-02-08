/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.util.Iterator;

/**
 * This class represents a delay matrix between every pair or nodes
 * inside a network topology, storing every distance between connected nodes.
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class DelayMatrix_Float {

	/**
	 * Matrix holding delay information between any two nodes.
	 */
	protected float[][] mDelayMatrix = null;

	/**
	 * Number of nodes in the distance-aware-topology.
	 */
	protected int mTotalNodeNum = 0;

	/**
	 * Private constructor to ensure that only an correct initialized delay-matrix could be created.
	 */
	@SuppressWarnings("unused")
	private DelayMatrix_Float() {
	}

	/**
	 * Creates an correctly initialized Float-Delay-Matrix.
	 * 
	 * @param graph the network topological graph
	 * @param directed indicates if an directed matrix should be computed (true) or not (false)
	 */
	public DelayMatrix_Float(TopologicalGraph graph, boolean directed) {

		// lets preinitialize the Delay-Matrix
		createDelayMatrix(graph, directed);

		// now its time to calculate all possible connection-delays
		calculateShortestPath();
	}

	/**
         * Gets the delay between two nodes.
         * 
	 * @param srcID the id of the source node
	 * @param destID the id of the destination node
	 * @return the delay between the given two nodes
	 */
	public float getDelay(int srcID, int destID) {
		// check the nodeIDs against internal array-boundarys
		if (srcID > mTotalNodeNum || destID > mTotalNodeNum) {
			throw new ArrayIndexOutOfBoundsException("srcID or destID is higher than highest stored node-ID!");
		}

		return mDelayMatrix[srcID][destID];
	}

	/**
	 * Creates all internal necessary network-distance structures from the given graph. 
         * For similarity, we assume all communication-distances are symmetrical, 
         * thus leading to an undirected network.
	 * 
	 * @param graph the network topological graph
	 * @param directed indicates if an directed matrix should be computed (true) or not (false)
	 */
	private void createDelayMatrix(TopologicalGraph graph, boolean directed) {

		// number of nodes inside the network
		mTotalNodeNum = graph.getNumberOfNodes();

		mDelayMatrix = new float[mTotalNodeNum][mTotalNodeNum];

		// cleanup the complete distance-matrix with "0"s
		for (int row = 0; row < mTotalNodeNum; ++row) {
			for (int col = 0; col < mTotalNodeNum; ++col) {
				mDelayMatrix[row][col] = Float.MAX_VALUE;
			}
		}

		Iterator<TopologicalLink> itr = graph.getLinkIterator();

		TopologicalLink edge;
		while (itr.hasNext()) {
			edge = itr.next();

			mDelayMatrix[edge.getSrcNodeID()][edge.getDestNodeID()] = edge.getLinkDelay();

			if (!directed) {
				// according to aproximity of symmetry to all communication-paths
				mDelayMatrix[edge.getDestNodeID()][edge.getSrcNodeID()] = edge.getLinkDelay();
			}

		}
	}

	/**
	 * Calculates the shortest path between all pairs of nodes.
	 */
	private void calculateShortestPath() {
		FloydWarshall_Float floyd = new FloydWarshall_Float();

		floyd.initialize(mTotalNodeNum);
		mDelayMatrix = floyd.allPairsShortestPaths(mDelayMatrix);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("just a simple printout of the distance-aware-topology-class\n");
		buffer.append("delay-matrix is:\n");

		for (int column = 0; column < mTotalNodeNum; ++column) {
			buffer.append("\t" + column);
		}

		for (int row = 0; row < mTotalNodeNum; ++row) {
			buffer.append("\n" + row);

			for (int col = 0; col < mTotalNodeNum; ++col) {
				if (mDelayMatrix[row][col] == Float.MAX_VALUE) {
					buffer.append("\t" + "-");
				} else {
					buffer.append("\t" + mDelayMatrix[row][col]);
				}
			}
		}

		return buffer.toString();
	}
}
