/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

import static org.junit.Assert.assertEquals;

import org.cloudbus.cloudsim.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class PowerModelCubicTest {

	private static final double MAX_POWER = 200;
	private static final double STATIC_POWER_PERCENT = 0.3;

	private PowerModelCubic powerModel;

	@Before
	public void setUp() throws Exception {
		powerModel = new PowerModelCubic(MAX_POWER, STATIC_POWER_PERCENT);
	}

	@Test
	public void testGetMaxPower() {
		assertEquals(MAX_POWER, powerModel.getMaxPower(), 0);
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
		assertEquals(0, powerModel.getPower(0.0), 0);
		assertEquals(MAX_POWER, powerModel.getPower(1.0), 0);
		assertEquals(MAX_POWER * STATIC_POWER_PERCENT + (MAX_POWER - MAX_POWER * STATIC_POWER_PERCENT) / Math.pow(100, 3) * Math.pow(0.5 * 100, 3), powerModel.getPower(0.5), 0);
	}

	@Test
	@Ignore
	public void testPrintPower() {
		for (int i = 0; i <= 100; i++) {
			Log.print(String.format("%d;%.2f\n", i, powerModel.getPower((double) i / 100)));
		}
	}

}