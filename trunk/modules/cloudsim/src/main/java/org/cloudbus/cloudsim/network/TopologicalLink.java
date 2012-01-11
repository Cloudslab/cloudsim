/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

/**
 * This class represents an link (edge) from an graph
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalLink {

	/**
	 * id of the link src node-id
	 */
	private int srcNodeID = 0;

	/**
	 * id of the link dest node-id
	 */
	private int destNodeID = 0;

	/**
	 * representing the link-delay of the connection
	 */
	private float linkDelay = 0;

	private float linkBw = 0;

	/**
	 * creates an new link-object
	 */
	public TopologicalLink(int srcNode, int destNode, float delay, float bw) {
		// lets initialize all internal attributes
		linkDelay = delay;
		srcNodeID = srcNode;
		destNodeID = destNode;
		linkBw = bw;
	}

	/**
	 * returns the node-ID from the SrcNode
	 * 
	 * @return nodeID
	 */
	public int getSrcNodeID() {
		return srcNodeID;
	}

	/**
	 * return the node-ID from the DestNode
	 * 
	 * @return nodeID
	 */
	public int getDestNodeID() {
		return destNodeID;
	}

	/**
	 * return the link-delay of the defined linke
	 * 
	 * @return the delay-amount
	 */
	public float getLinkDelay() {
		return linkDelay;
	}

	/**
	 * return the link-bw of the defined linke
	 * 
	 * @return the bw
	 */
	public float getLinkBw() {
		return linkBw;
	}

}
