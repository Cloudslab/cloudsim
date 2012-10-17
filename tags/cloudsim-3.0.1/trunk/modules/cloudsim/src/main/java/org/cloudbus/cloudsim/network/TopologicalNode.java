/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

/**
 * Just represents an topological network node retrieves its information from an
 * topological-generated file (eg. topology-generator)
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalNode {

	/**
	 * its the nodes-ID inside this network
	 */
	private int nodeID = 0;

	/**
	 * describes the nodes-name inside the network
	 */
	private String nodeName = null;

	/**
	 * representing the x an y world-coordinates
	 */
	private int worldX = 0;

	private int worldY = 0;

	/**
	 * constructs an new node
	 */
	public TopologicalNode(int nodeID) {
		// lets initialize all private class attributes
		this.nodeID = nodeID;
		nodeName = String.valueOf(nodeID);
	}

	/**
	 * constructs an new node including world-coordinates
	 */
	public TopologicalNode(int nodeID, int x, int y) {
		// lets initialize all private class attributes
		this.nodeID = nodeID;
		nodeName = String.valueOf(nodeID);
		worldX = x;
		worldY = y;
	}

	/**
	 * constructs an new node including world-coordinates and the nodeName
	 */
	public TopologicalNode(int nodeID, String nodeName, int x, int y) {
		// lets initialize all private class attributes
		this.nodeID = nodeID;
		this.nodeName = nodeName;
		worldX = x;
		worldY = y;
	}

	/**
	 * delivers the nodes id
	 * 
	 * @return just the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * delivers the name of the node
	 * 
	 * @return name of the node
	 */
	public String getNodeLabel() {
		return nodeName;
	}

	/**
	 * returns the x coordinate of this network-node
	 * 
	 * @return the x coordinate
	 */
	public int getCoordinateX() {
		return worldX;
	}

	/**
	 * returns the y coordinate of this network-node
	 * 
	 * @return the y coordinate
	 */
	public int getCoordinateY() {
		return worldY;
	}

}
