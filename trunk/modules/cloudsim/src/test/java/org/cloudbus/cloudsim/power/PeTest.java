package org.cloudbus.cloudsim.power;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSqrt;
import org.junit.Test;

public class PeTest {
	
	@Test
	public void testGetPowerModel() {
		PowerPe powerPe = new PowerPe(0, null, null);
		assertNull(powerPe.getPowerModel());
		PowerModel powerModel = new PowerModelSqrt(0, 0);
		powerPe.setPowerModel(powerModel);
		assertSame(powerModel, powerPe.getPowerModel());	
	}

}
