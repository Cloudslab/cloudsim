/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author		Anton Beloglazov
 * @author 		Remo Andreoli
 * @since		CloudSim Toolkit 2.0
 */
public class PeTest {

	private static final double MIPS = 1000;

	@Test
	public void testGetPeProvisioner() {
		PeProvisionerSimple peProvisioner = new PeProvisionerSimple(MIPS);
		Pe pe = new Pe(0, peProvisioner);
		assertSame(peProvisioner, pe.getPeProvisioner());
		assertEquals(MIPS, pe.getPeProvisioner().getAvailableMips(), 0);
	}

	@Test
	public void testSetId() {
		Pe pe = new Pe(0, null);
		assertEquals(0, pe.getId());
		pe.setId(1);
		assertEquals(1, pe.getId());
	}

	@Test
	public void testSetMips() {
		PeProvisionerSimple peProvisioner = new PeProvisionerSimple(MIPS);
		Pe pe = new Pe(0, peProvisioner);
		assertEquals(MIPS, pe.getMips(), 0);
		pe.setMips(MIPS / 2);
		assertEquals(MIPS / 2, pe.getMips(), 0);
	}

	@Test
	public void testSetStatus() {
		Pe pe = new Pe(0, null);
		assertEquals(Pe.FREE, pe.getStatus());
		pe.setStatusBusy();
		assertEquals(Pe.BUSY, pe.getStatus());
		pe.setStatusFailed();
		assertEquals(Pe.FAILED, pe.getStatus());
		pe.setStatusFree();
		assertEquals(Pe.FREE, pe.getStatus());
	}

}
