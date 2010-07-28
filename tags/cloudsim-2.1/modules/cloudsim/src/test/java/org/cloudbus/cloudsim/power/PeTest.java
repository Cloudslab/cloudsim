/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSqrt;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
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
