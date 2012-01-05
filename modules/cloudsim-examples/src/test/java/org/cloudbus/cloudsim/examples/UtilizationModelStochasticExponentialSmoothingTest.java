package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Log;
import org.junit.Ignore;
import org.junit.Test;

public class UtilizationModelStochasticExponentialSmoothingTest {

	@Test
	@Ignore
	public void testGeneratedValues() {
		double alpha = 0.1;
		for (int i = 0; i < 10; i++) {
			UtilizationModelStochasticExponentialSmoothing utilizationModel = new UtilizationModelStochasticExponentialSmoothing();
			UtilizationModelStochasticExponentialSmoothing.ALPHA = alpha;
			Log.printLine("\n\nalpha = " + UtilizationModelStochasticExponentialSmoothing.ALPHA + "\n\n");
			for (int j = 0; j < 100; j++) {
				Log.printLine(j + ";" + utilizationModel.getUtilization(j));
			}
			alpha += 0.1;
		}
	}

}
