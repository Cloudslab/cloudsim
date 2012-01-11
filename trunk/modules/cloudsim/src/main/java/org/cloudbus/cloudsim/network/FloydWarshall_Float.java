/*
 * @(#)FloydWarshall.java	ver 1.2  6/20/2005
 *
 * Modified by Weishuai Yang (wyang@cs.binghamton.edu).
 * Originally written by Rahul Simha
 *
 */

package org.cloudbus.cloudsim.network;

/**
 * FloydWarshall algorithm to calculate all pairs delay and predecessor matrix.
 * 
 * @author Rahul Simha
 * @author Weishuai Yang
 * @version 1.2, 6/20/2005
 * @since CloudSim Toolkit 1.0
 */
public class FloydWarshall_Float {

	/**
	 * Number of vertices (when initialized)
	 */
	private int numVertices;

	// /**
	// * The adjacency matrix (given as input),
	// * here I use float rather than double to save memory,
	// * since there won't be a lot of spilting for delay,
	// * and float is accurate enough.
	// */
	// private float[][] adjMatrix;

	/**
	 * Matrices used in dynamic programming
	 */
	private float[][] Dk, Dk_minus_one;

	/**
	 * Matrices used in dynamic programming
	 */
	private int[][] Pk, Pk_minus_one;

	/**
	 * initialization matrix
	 * 
	 * @param numVertices number of nodes
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
	 * calculates all pairs delay
	 * 
	 * @param adjMatrix original delay matrix
	 * @return all pairs delay matrix
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
	 * gets predecessor matrix
	 * 
	 * @return predecessor matrix
	 */
	public int[][] getPK() {
		return Pk;
	}


/*
  public static void main (String[] argv)
  {
    // A test case.
     *
      double[][] adjMatrix = {
        {0, 1, 0, 0, 1},
        {1, 0, 1, 3, 0},
        {0, 1, 0, 2, 0},
        {0, 3, 2, 0, 1},
        {1, 0, 0, 1, 0},
      };


      int n = adjMatrix.length;
      FloydWarshall fwAlg = new FloydWarshall ();
      fwAlg.initialize (n);
      adjMatrix=fwAlg.allPairsShortestPaths (adjMatrix);

	    //debug begin
	    StringBuffer s0=new StringBuffer("Delay Information before floydwarshall:\n");
	    for(int i=0;i<n;i++){
	    	s0.append("Node "+i+" to others:");
	    	for(int j=0;j<n;j++){
	    			s0.append(LogFormatter.sprintf(" % 6.1f     ", adjMatrix[i][j]));

	    	}
	    	s0.append("\n");
	    }
	    Log.printLine(""+s0);


	    int[][] Pk=fwAlg.getPK();


	    Log.printLine("Path information");
	    for(int i=0;i<n;i++){
	    	for(int j=0;j<n;j++){
	    		Log.print("From "+i+" to "+j+": ");
		    	int pre=Pk[i][j];
		    	while((pre!=-1)&&(pre!=i)){
		    		Log.print(" <-  "+ pre);
		    		pre=Pk[i][pre];
		    		if((pre==-1)||(pre==i))
		    			Log.print(" <-  "+ pre);
		    	}
				Log.printLine("\n");
		    }
	    }

  }

*/
}
