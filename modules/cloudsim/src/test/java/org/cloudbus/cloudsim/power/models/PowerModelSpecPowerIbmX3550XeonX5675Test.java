/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class PowerModelSpecPowerIbmX3550XeonX5675Test {

	private PowerModel powerModel;

	@Before
	public void setUp() throws Exception {
		powerModel = new PowerModelSpecPowerIbmX3550XeonX5675();
	}

	@Test (expected = IllegalArgumentException.class)
	public void testGetPowerArgumentLessThenZero() throws IllegalArgumentException {
		powerModel.getPower(-1);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testGetPowerArgumentLargerThenOne() throws IllegalArgumentException {
		powerModel.getPower(2);
	}

	@Test
	public void testGetPower() {
		assertEquals(58.4, powerModel.getPower(0), 0);
		assertEquals(58.4 + (98 - 58.4) / 5, powerModel.getPower(0.02), 0);
		assertEquals(98, powerModel.getPower(0.1), 0);
		assertEquals(140, powerModel.getPower(0.5), 0);
		assertEquals(189, powerModel.getPower(0.8), 0);
		assertEquals(189 + 0.7 * 10 * (205 - 189) / 10, powerModel.getPower(0.87), 0);
		assertEquals(205, powerModel.getPower(0.9), 0);
		assertEquals(222, powerModel.getPower(1), 0);

	}

}