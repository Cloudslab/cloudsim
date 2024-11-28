/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

import org.cloudbus.cloudsim.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class PowerModelLinearTest {

	private static final double MAX_POWER = 250;
	private static final double STATIC_POWER_PERCENT = 0.7;

	private PowerModelLinear powerModel;

	@BeforeEach
	public void setUp() throws Exception {
		powerModel = new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT);
	}

	@Test
	public void testGetMaxPower() {
		assertEquals(MAX_POWER, powerModel.getMaxPower(), 0);
	}

	@Test
	public void testGetPowerArgumentLessThenZero() throws IllegalArgumentException {
		assertThrows(IllegalArgumentException.class, () -> powerModel.getPower(-1));
	}

	@Test
	public void testGetPowerArgumentLargerThenOne() throws IllegalArgumentException {
		assertThrows(IllegalArgumentException.class, () -> powerModel.getPower(2));
	}

	@Test
	public void testGetPower() {
		assertEquals(0, powerModel.getPower(0.0), 0);
		assertEquals(MAX_POWER, powerModel.getPower(1.0), 0);
		assertEquals(MAX_POWER * STATIC_POWER_PERCENT + ((MAX_POWER - MAX_POWER * STATIC_POWER_PERCENT) / 100) * 0.5 * 100, powerModel.getPower(0.5), 0);
	}

	@Test
	public void testPrintPower() {
		for (int i = 0; i <= 100; i++) {
			Log.print(String.format("%d;%.2f\n", i, powerModel.getPower((double) i / 100)));
		}
	}

}