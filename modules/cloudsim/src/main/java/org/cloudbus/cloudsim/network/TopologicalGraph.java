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
 * This class represents a graph containing vertices (nodes) and edges (links), 
 * used for input with a network-layer.
 * Graphical-Output Restricions! <br/>
 * <ul>
 *   <li>EdgeColors: GraphicalProperties.getColorEdge 
 *   <li>NodeColors: GraphicalProperties.getColorNode
 * </ul>
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalGraph {
        /**
         * The list of links of the network graph.
         */
	private List<TopologicalLink> linkList = null;

        /**
         * The list of nodes of the network graph.
         */
	private List<TopologicalNode> nodeList = null;

	/**
	 * Creates an empty graph-object.
	 */
	public TopologicalGraph() {
		linkList = new LinkedList<TopologicalLink>();
		nodeList = new LinkedList<TopologicalNode>();
	}

	/**
	 * Adds an link between two topological nodes.
	 * 
	 * @param edge the topological link
	 */
	public void addLink(TopologicalLink edge) {
		linkList.add(edge);
	}

	/**
	 * Adds an Topological Node to this graph.
	 * 
	 * @param node the topological node to add
	 */
	public void addNode(TopologicalNode node) {
		nodeList.add(node);
	}

	/**
	 * Gets the number of nodes contained inside the topological-graph.
	 * 
	 * @return number of nodes
	 */
	public int getNumberOfNodes() {
		return nodeList.size();
	}

	/**
	 * Gets the number of links contained inside the topological-graph.
	 * 
	 * @return number of links
	 */
	public int getNumberOfLinks() {
		return linkList.size();
	}

	/**
	 * Gets an iterator through all network-graph links.
	 * 
	 * @return the iterator throug all links
	 */
	public Iterator<TopologicalLink> getLinkIterator() {
		return linkList.iterator();
	}

	/**
	 * Gets an iterator through all network-graph nodes.
	 * 
	 * @return the iterator through all nodes
	 */
	public Iterator<TopologicalNode> getNodeIterator() {
		return nodeList.iterator();
	}

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
