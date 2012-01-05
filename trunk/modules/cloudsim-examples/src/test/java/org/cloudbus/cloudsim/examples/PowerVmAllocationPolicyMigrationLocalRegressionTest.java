package org.cloudbus.cloudsim.examples;

import org.junit.Test;

import flanagan.analysis.Regression;

public class PowerVmAllocationPolicyMigrationLocalRegressionTest {

	public static final double[] X = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
			21 };

	public static final double[] Y = { 55, 60, 62, 59, 67, 73, 85, 97, 73, 68, 69, 52, 51, 55, 48, 46, 52,
			55, 58, 65, 70 };

	@Test
	public void testData() {
		Regression regression = new Regression(X, Y);
		regression.constantPlot();
	}

}
