/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class PowerHostTest {
	
	private static final double MIPS = 1000;
	private static final double MAX_POWER = 200;
	private static final double STATIC_POWER_PERCENT = 0.3;
	private static final double TIME = 10;
	
	private PowerHost host;
	
	@Before
	public void setUp() throws Exception {
		List<Pe> peList = new ArrayList<Pe>();
		peList.add(new Pe(0, new PeProvisionerSimple(MIPS)));
		host = new PowerHost(0, null, null, 0, peList, null, new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT));
	}

	@Test
	public void testGetMaxPower() {
		assertEquals(MAX_POWER, host.getMaxPower(), 0);		
	}
	
	@Test
	public void testGetEnergy() {
		assertEquals(0, host.getEnergyLinearInterpolation(0, 0, TIME), 0);
		double expectedEnergy = 0;
		try {
			expectedEnergy = (host.getPowerModel().getPower(0.2) + (host.getPowerModel().getPower(0.9) - host.getPowerModel().getPower(0.2)) / 2) * TIME;
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(expectedEnergy, host.getEnergyLinearInterpolation(0.2, 0.9, TIME), 0);
	}

}
