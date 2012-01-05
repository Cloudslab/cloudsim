package org.cloudbus.cloudsim.examples;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class UtilizationModelPlanetLabTest {

	public static final String FILE = "UtilizationModelPlanetLabTest.dat";

	private UtilizationModelPlanetLab utilizationModel;

	@Before
	public void setUp() throws Exception {
		utilizationModel = new UtilizationModelPlanetLab(getClass().getClassLoader().getResource(FILE)
				.getPath());
	}

	@Test
	public void testGetPowerModel() {
		assertEquals(10.1, utilizationModel.getUtilization(0), 0);
		assertEquals(11.1, utilizationModel.getUtilization(1), 0);
		assertEquals(12.1, utilizationModel.getUtilization(2), 0);
		assertEquals(13.1, utilizationModel.getUtilization(3), 0);
		assertEquals(14.1, utilizationModel.getUtilization(4), 0);
		assertEquals(15.1, utilizationModel.getUtilization(5), 0);
		assertEquals(10.1, utilizationModel.getUtilization(0), 0);
		assertEquals(11.1, utilizationModel.getUtilization(1), 0);
		assertEquals(12.1, utilizationModel.getUtilization(2), 0);
		assertEquals(16.1, utilizationModel.getUtilization(6), 0);
		assertEquals(17.1, utilizationModel.getUtilization(7), 0);
		assertEquals(18.1, utilizationModel.getUtilization(8), 0);
		assertEquals(13.1, utilizationModel.getUtilization(3), 0);
		assertEquals(14.1, utilizationModel.getUtilization(4), 0);
		assertEquals(15.1, utilizationModel.getUtilization(5), 0);
		assertEquals(19.1, utilizationModel.getUtilization(9), 0);
		assertEquals(20.1, utilizationModel.getUtilization(10), 0);
		assertEquals(21.1, utilizationModel.getUtilization(11), 0);
		assertEquals(22.1, utilizationModel.getUtilization(12), 0);
		assertEquals(23.1, utilizationModel.getUtilization(13), 0);
		assertEquals(24.1, utilizationModel.getUtilization(14), 0);
		assertEquals(25.1, utilizationModel.getUtilization(15), 0);
		assertEquals(0.0, utilizationModel.getUtilization(16), 0);
		assertEquals(10.1, utilizationModel.getUtilization(0), 0);
		assertEquals(11.1, utilizationModel.getUtilization(1), 0);
		assertEquals(12.1, utilizationModel.getUtilization(2), 0);
		assertEquals(13.1, utilizationModel.getUtilization(3), 0);
	}

}
