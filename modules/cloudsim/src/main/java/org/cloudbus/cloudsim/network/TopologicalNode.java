/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

/**
 * Represents an topological network node that retrieves its information from a
 * topological-generated file (eg. topology-generator)
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalNode {

    /**
     * The BRITE id of the node inside the network.
     */
    private int nodeID = 0;

    /**
     * The name of the node inside the network.
     */
    private String nodeName = null;

    /**
     * Represents the x world-coordinate.
     */
    private int worldX = 0;

    /**
     * Represents the y world-coordinate.
     */
    private int worldY = 0;

    /**
     * Constructs an new node.
     * @param nodeID The BRITE id of the node inside the network
     */
    public TopologicalNode(int nodeID) {
            this.nodeID = nodeID;
            nodeName = String.valueOf(nodeID);
    }

    /**
     * Constructs an new node including world-coordinates.
     * @param nodeID The BRITE id of the node inside the network
     * @param x x world-coordinate
     * @param y y world-coordinate
     */
    public TopologicalNode(int nodeID, int x, int y) {
            this.nodeID = nodeID;
            nodeName = String.valueOf(nodeID);
            worldX = x;
            worldY = y;
    }

    /**
     * Constructs an new node including world-coordinates and the nodeName.
     * @param nodeID The BRITE id of the node inside the network
     * @param nodeName The name of the node inside the network
     * @param x x world-coordinate
     * @param y y world-coordinate
     */
    public TopologicalNode(int nodeID, String nodeName, int x, int y) {
            this.nodeID = nodeID;
            this.nodeName = nodeName;
            worldX = x;
            worldY = y;
    }

    /**
     * Gets the node BRITE id.
     * 
     * @return the nodeID
     */
    public int getNodeID() {
            return nodeID;
    }

    /**
     * Gets the name of the node
     * 
     * @return name of the node
     */
    public String getNodeLabel() {
            return nodeName;
    }

    /**
     * Gets the x world coordinate of this network-node.
     * 
     * @return the x world coordinate
     */
    public int getCoordinateX() {
            return worldX;
    }

    /**
     * Gets the y world coordinate of this network-node
     * 
     * @return the y world coordinate
     */
    public int getCoordinateY() {
            return worldY;
    }

}
