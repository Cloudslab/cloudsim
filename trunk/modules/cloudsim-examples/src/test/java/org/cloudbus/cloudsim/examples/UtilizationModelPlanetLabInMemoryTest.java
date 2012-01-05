package org.cloudbus.cloudsim.examples;

import static org.junit.Assert.assertEquals;

import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.junit.Before;
import org.junit.Test;

public class UtilizationModelPlanetLabInMemoryTest {

	public static final double SCHEDULING_INTERVAL = 300;

	public static final String FILE = "146-179_surfsnel_dsl_internl_net_colostate_557.dat";

	private UtilizationModelPlanetLabInMemory utilizationModel;

	@Before
	public void setUp() throws Exception {
		utilizationModel = new UtilizationModelPlanetLabInMemory(getClass().getClassLoader()
				.getResource(FILE).getPath(), SCHEDULING_INTERVAL);
	}

	@Test
	public void testGetPowerModel() {
		assertEquals(24, utilizationModel.getUtilization(0), 0);
		assertEquals(34, utilizationModel.getUtilization(1 * SCHEDULING_INTERVAL), 0);
		assertEquals(
				24 + 0.2 * SCHEDULING_INTERVAL * (34 - 24) / SCHEDULING_INTERVAL,
				utilizationModel.getUtilization(0.2 * SCHEDULING_INTERVAL),
				0);
		assertEquals(29, utilizationModel.getUtilization(2 * SCHEDULING_INTERVAL), 0);
		assertEquals(18, utilizationModel.getUtilization(136 * SCHEDULING_INTERVAL), 0);
		assertEquals(
				18 + 0.7 * SCHEDULING_INTERVAL * (21 - 18) / SCHEDULING_INTERVAL,
				utilizationModel.getUtilization(136.7 * SCHEDULING_INTERVAL),
				0);
		assertEquals(51, utilizationModel.getUtilization(287 * SCHEDULING_INTERVAL), 0);
	}

}
