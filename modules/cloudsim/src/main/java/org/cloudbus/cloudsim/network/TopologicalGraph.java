/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents an graph containing nodes and edges, used for input with an network-layer
 * Graphical-Output Restricions! EdgeColors: GraphicalProperties.getColorEdge NodeColors:
 * GraphicalProperties.getColorNode
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalGraph {

	private List<TopologicalLink> linkList = null;

	private List<TopologicalNode> nodeList = null;

	/**
	 * just the constructor to create an empty graph-object
	 */
	public TopologicalGraph() {
		linkList = new LinkedList<TopologicalLink>();
		nodeList = new LinkedList<TopologicalNode>();
	}

	/**
	 * adds an link between two topological nodes
	 * 
	 * @param edge the topological link
	 */
	public void addLink(TopologicalLink edge) {
		linkList.add(edge);
	}

	/**
	 * adds an Topological Node to this graph
	 * 
	 * @param node the topological node to add
	 */
	public void addNode(TopologicalNode node) {
		nodeList.add(node);
	}

	/**
	 * returns the number of nodes contained inside the topological-graph
	 * 
	 * @return number of nodes
	 */
	public int getNumberOfNodes() {
		return nodeList.size();
	}

	/**
	 * returns the number of links contained inside the topological-graph
	 * 
	 * @return number of links
	 */
	public int getNumberOfLinks() {
		return linkList.size();
	}

	/**
	 * return an iterator through all network-graph links
	 * 
	 * @return the iterator throug all links
	 */
	public Iterator<TopologicalLink> getLinkIterator() {
		return linkList.iterator();
	}

	/**
	 * returns an iterator through all network-graph nodes
	 * 
	 * @return the iterator through all nodes
	 */
	public Iterator<TopologicalNode> getNodeIterator() {
		return nodeList.iterator();
	}

	/**
	 * prints out all internal node and link information
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("topological-node-information: \n");

		for (TopologicalNode node : nodeList) {
			buffer.append(node.getNodeID() + " | x is: " + node.getCoordinateX() + " y is: "
					+ node.getCoordinateY() + "\n");
		}

		buffer.append("\n\n node-link-information:\n");

		for (TopologicalLink link : linkList) {
			buffer.append("from: " + link.getSrcNodeID() + " to: " + link.getDestNodeID() + " delay: "
					+ link.getLinkDelay() + "\n");
		}
		return buffer.toString();
	}

}
