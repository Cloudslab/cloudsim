/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * A file reader for the special BRITE-format. A BRITE file is structured as
 * follows:<br/> 
 * <ul>
 * <li>Node-section: NodeID, xpos, ypos, indegree, outdegree, ASid, type(router/AS)
 * <li>Edge-section: EdgeID, fromNode, toNode, euclideanLength, linkDelay, linkBandwith, AS_from, AS_to,
 * type
 * </ul>
 * 
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class GraphReaderBrite implements GraphReaderIF {

	private static final int PARSE_NOTHING = 0;

	private static final int PARSE_NODES = 1;

	private static final int PARSE_EDGES = 2;

	private int state = PARSE_NOTHING;

        /**
         * The network Topological Graph.
         */
	private TopologicalGraph graph = null;

	@Override
	public TopologicalGraph readGraphFile(String filename) throws IOException {

		graph = new TopologicalGraph();

		// lets read the file
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);

		String lineSep = System.getProperty("line.separator");
		String nextLine = null;
		StringBuffer sb = new StringBuffer();

		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine);
			//
			// note:
			// BufferedReader strips the EOL character.
			//
			sb.append(lineSep);

			// functionality to diferentiate between all the parsing-states
			// state that should just find the start of node-declaration
			if (state == PARSE_NOTHING) {
				if (nextLine.contains("Nodes:")) {
					// Log.printLine("found start of Nodes... switch to parse nodes!");
					state = PARSE_NODES;
				}
			}

			// the state to retrieve all node-information
			else if (state == PARSE_NODES) {
				// perform the parsing of this node-line
				parseNodeString(nextLine);
			}

			// the state to retrieve all edges-information
			else if (state == PARSE_EDGES) {
				parseEdgesString(nextLine);
			}

		}

		br.close();

		// Log.printLine("read file successfully...");
		// Log.printLine(sb.toString());

		return graph;
	}

        /**
         * Parses nodes inside a line from the BRITE file.
         * 
         * @param nodeLine A line read from the file
         */
	private void parseNodeString(String nodeLine) {
		StringTokenizer tokenizer = new StringTokenizer(nodeLine);

		// number of node parameters to parse (counts at linestart)
		int parameters = 3;

		// first test to step to the next parsing-state (edges)
		if (nodeLine.contains("Edges:")) {
			// Log.printLine("found start of Edges... switch to parse edges!");
			state = PARSE_EDGES;

			return;
		}

		// test against an empty line
		if (!tokenizer.hasMoreElements()) {
			// Log.printLine("this line contains no tokens...");
			return;
		}

		// parse this string-line to read all node-parameters
		// NodeID, xpos, ypos, indegree, outdegree, ASid, type(router/AS)

		int nodeID = 0;
		String nodeLabel = "";
		int xPos = 0;
		int yPos = 0;

		for (int actualParam = 0; tokenizer.hasMoreElements() && actualParam < parameters; actualParam++) {
			String token = tokenizer.nextToken();
			switch (actualParam) {
				case 0:	// Log.printLine("nodeID: "+token);
						// Log.printLine("nodeLabel: "+token);
					nodeID = Integer.valueOf(token);
					nodeLabel = Integer.toString(nodeID);
					break;

				case 1:	// Log.printLine("x-Pos: "+token);
					xPos = Integer.valueOf(token);
					break;

				case 2:	// Log.printLine("y-Pos: "+token);
					yPos = Integer.valueOf(token);
					break;
			}
		}

		// instanciate an new node-object with previous parsed parameters
		TopologicalNode topoNode = new TopologicalNode(nodeID, nodeLabel, xPos, yPos);
		graph.addNode(topoNode);

	}

        /**
         * Parses edges inside a line from the BRITE file.
         * 
         * @param nodeLine A line read from the file
         */
	private void parseEdgesString(String nodeLine) {
		StringTokenizer tokenizer = new StringTokenizer(nodeLine);

		// number of node parameters to parse (counts at linestart)
		int parameters = 6;

		// test against an empty line
		if (!tokenizer.hasMoreElements()) {
			// Log.printLine("this line contains no tokens...");
			return;
		}

		// parse this string-line to read all node-parameters
		// EdgeID, fromNode, toNode, euclideanLength, linkDelay, linkBandwith, AS_from, AS_to, type

		// int edgeID = 0;
		int fromNode = 0;
		int toNode = 0;
		// float euclideanLength = 0;
		float linkDelay = 0;
		int linkBandwith = 0;

		for (int actualParam = 0; tokenizer.hasMoreElements() && actualParam < parameters; actualParam++) {
			String token = tokenizer.nextToken();
			switch (actualParam) {
				case 0:	// Log.printLine("edgeID: "+token);
						// edgeID = Integer.valueOf(token);
					break;

				case 1:	// Log.printLine("fromNode: "+token);
					fromNode = Integer.valueOf(token);
					break;

				case 2:	// Log.printLine("toNode: "+token);
					toNode = Integer.valueOf(token);
					break;

				case 3:	// Log.printLine("euclideanLength: "+token);
						// euclideanLength = Float.valueOf(token);
					break;

				case 4:	// Log.printLine("linkDelay: "+token);
					linkDelay = Float.valueOf(token);
					break;

				case 5:	// Log.printLine("linkBandwith: "+token);
					linkBandwith = Float.valueOf(token).intValue();
					break;
			}// switch-END
		}// for-END

		graph.addLink(new TopologicalLink(fromNode, toNode, linkDelay, linkBandwith));

	}

}
