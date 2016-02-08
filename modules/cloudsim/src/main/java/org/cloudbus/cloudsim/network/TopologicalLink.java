/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

/**
 * This class represents an link (edge) from a network graph.
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalLink {

	/**
	 * The BRITE id of the source node of the link.
	 */
	private int srcNodeID = 0;

	/**
	 * The BRITE id of the destination node of the link.
	 */
	private int destNodeID = 0;

	/**
	 * The link delay of the connection.
	 */
	private float linkDelay = 0;

	/**
	 * The link bandwidth (bw).
	 */
	private float linkBw = 0;

	/**
	 * Creates a new Topological Link.
	 */
	public TopologicalLink(int srcNode, int destNode, float delay, float bw) {
		// lets initialize all internal attributes
		linkDelay = delay;
		srcNodeID = srcNode;
		destNodeID = destNode;
		linkBw = bw;
	}

	/**
	 * Gets the BRITE id of the source node of the link.
	 * 
	 * @return nodeID
	 */
	public int getSrcNodeID() {
		return srcNodeID;
	}

	/**
	 * Gets the BRITE id of the destination node of the link.
	 * 
	 * @return nodeID
	 */
	public int getDestNodeID() {
		return destNodeID;
	}

	/**
	 * Gets the delay of the link.
	 * 
	 * @return the link delay
	 */
	public float getLinkDelay() {
		return linkDelay;
	}

	/**
	 * Gets the bandwidth of the link.
	 * 
	 * @return the bw
	 */
	public float getLinkBw() {
		return linkBw;
	}

}
