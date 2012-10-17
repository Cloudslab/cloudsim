package org.cloudbus.cloudsim.power;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyRandomSelection;
import org.junit.Before;
import org.junit.Test;

public class PowerVmSelectionPolicyMaximumCorrelationTest {

	public static final double[][] DATA = { { 1, 2, 2, 4, 3, 6 },			// 0.9834528493463638
			{ 14, 23, 30, 50, 39, 67 },		// 0.986553560148001 MAX
			{ 4, 4, 7, 7, 10, 10 }			// 0.732289527720739
	};

	public static final double[] CORRELATION = { 0.9834528493463638, 0.986553560148001, 0.732289527720739 };

	private PowerVmSelectionPolicyMaximumCorrelation vmSelectionPolicyMaximumCorrelation;

	@Before
	public void setUp() throws Exception {
		vmSelectionPolicyMaximumCorrelation = new PowerVmSelectionPolicyMaximumCorrelation(
				new PowerVmSelectionPolicyRandomSelection());
	}

	@Test
	public void testGetPowerModel() {
		List<Double> result = vmSelectionPolicyMaximumCorrelation.getCorrelationCoefficients(DATA);
		for (int i = 0; i < result.size(); i++) {
			assertEquals(CORRELATION[i], result.get(i), 0.00001);
		}
	}

}
