/*
 * @(#)FloydWarshall.java	ver 1.2  6/20/2005
 *
 * Modified by Weishuai Yang (wyang@cs.binghamton.edu).
 * Originally written by Rahul Simha
 *
 */

package org.cloudbus.cloudsim.network;

/**
 * FloydWarshall algorithm to calculate the predecessor matrix 
 * and the delay between all pairs of nodes.
 * 
 * @author Rahul Simha
 * @author Weishuai Yang
 * @version 1.2, 6/20/2005
 * @since CloudSim Toolkit 1.0
 */
public class FloydWarshall_Float {

	/**
	 * Number of vertices (nodes).
	 */
	private int numVertices;

	/**
	 * Matrices used in dynamic programming.
	 */
	private float[][] Dk, Dk_minus_one;

	/**
	 * The predecessor matrix. Matrix used by dynamic programming.
	 */
	private int[][] Pk;
        
        /**
         * Matrix used by dynamic programming.
         */
        private int[][] Pk_minus_one;

	/**
	 * Initialization the matrix.
	 * 
	 * @param numVertices number of nodes
         * @todo The class doesn't have a constructor. This should be the constructor.
	 */
	public void initialize(int numVertices) {
		this.numVertices = numVertices;

		// Initialize Dk matrices.
		Dk = new float[numVertices][];
		Dk_minus_one = new float[numVertices][];
		for (int i = 0; i < numVertices; i++) {
			Dk[i] = new float[numVertices];
			Dk_minus_one[i] = new float[numVertices];
		}

		// Initialize Pk matrices.
		Pk = new int[numVertices][];
		Pk_minus_one = new int[numVertices][];
		for (int i = 0; i < numVertices; i++) {
			Pk[i] = new int[numVertices];
			Pk_minus_one[i] = new int[numVertices];
		}

	}

	/**
	 * Calculates the delay between all pairs of nodes.
	 * 
	 * @param adjMatrix original delay matrix
	 * @return the delay matrix
	 */
	public float[][] allPairsShortestPaths(float[][] adjMatrix) {
		// Dk_minus_one = weights when k = -1
		for (int i = 0; i < numVertices; i++) {
			for (int j = 0; j < numVertices; j++) {
				if (adjMatrix[i][j] != 0) {
					Dk_minus_one[i][j] = adjMatrix[i][j];
					Pk_minus_one[i][j] = i;
				} else {
					Dk_minus_one[i][j] = Float.MAX_VALUE;
					Pk_minus_one[i][j] = -1;
				}
				// NOTE: we have set the value to infinity and will exploit
				// this to avoid a comparison.
			}
		}

		// Now iterate over k.

		for (int k = 0; k < numVertices; k++) {

			// Compute Dk[i][j], for each i,j

			for (int i = 0; i < numVertices; i++) {
				for (int j = 0; j < numVertices; j++) {
					if (i != j) {

						// D_k[i][j] = min ( D_k-1[i][j], D_k-1[i][k] + D_k-1[k][j].
						if (Dk_minus_one[i][j] <= Dk_minus_one[i][k] + Dk_minus_one[k][j]) {
							Dk[i][j] = Dk_minus_one[i][j];
							Pk[i][j] = Pk_minus_one[i][j];
						} else {
							Dk[i][j] = Dk_minus_one[i][k] + Dk_minus_one[k][j];
							Pk[i][j] = Pk_minus_one[k][j];
						}
					} else {
						Pk[i][j] = -1;
					}
				}
			}

			// Now store current Dk into D_k-1
			for (int i = 0; i < numVertices; i++) {
				for (int j = 0; j < numVertices; j++) {
					Dk_minus_one[i][j] = Dk[i][j];
					Pk_minus_one[i][j] = Pk[i][j];
				}
			}

		} // end-outermost-for

		return Dk;

	}

	/**
	 * Gets predecessor matrix.
	 * 
	 * @return predecessor matrix
	 */
	public int[][] getPK() {
		return Pk;
	}
}
