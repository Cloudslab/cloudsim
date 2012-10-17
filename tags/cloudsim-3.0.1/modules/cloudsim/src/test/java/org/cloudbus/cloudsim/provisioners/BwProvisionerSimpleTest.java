/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cloudbus.cloudsim.Vm;
import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class BwProvisionerSimpleTest {

	private static final long BW = 1000;

	private BwProvisionerSimple bwProvisioner;

	@Before
	public void setUp() throws Exception {
		bwProvisioner = new BwProvisionerSimple(BW);
	}

	@Test
	public void testGetBw() {
		assertEquals(BW, bwProvisioner.getBw());
	}

	@Test
	public void testGetAvailableBw() {
		assertEquals(BW, bwProvisioner.getAvailableBw());
	}

	@Test
	public void testAllocateBwforVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, 0, BW / 2, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, 0, BW, 0, "", null);

		assertTrue(bwProvisioner.isSuitableForVm(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		assertFalse(bwProvisioner.isSuitableForVm(vm2, BW));
		assertFalse(bwProvisioner.allocateBwForVm(vm2, BW));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForVm(vm2, BW / 4));
		assertTrue(bwProvisioner.allocateBwForVm(vm2, BW / 4));
		assertEquals(BW * 1 / 4, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForVm(vm2, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm2, BW / 2));
		assertEquals(0, bwProvisioner.getAvailableBw());
	}

	@Test
	public void testGetAllocatedBwforVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, 0, BW / 2, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, 0, BW, 0, "", null);

		assertTrue(bwProvisioner.isSuitableForVm(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAllocatedBwForVm(vm1));

		assertFalse(bwProvisioner.isSuitableForVm(vm2, BW));
		assertFalse(bwProvisioner.allocateBwForVm(vm2, BW));
		assertEquals(0, bwProvisioner.getAllocatedBwForVm(vm2));

		assertTrue(bwProvisioner.isSuitableForVm(vm2, BW / 4));
		assertTrue(bwProvisioner.allocateBwForVm(vm2, BW / 4));
		assertEquals(BW / 4, bwProvisioner.getAllocatedBwForVm(vm2));

		assertTrue(bwProvisioner.isSuitableForVm(vm2, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm2, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAllocatedBwForVm(vm2));
	}

	@Test
	public void testDeallocateBwForVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, 0, BW / 2, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, 0, BW / 2, 0, "", null);

		assertEquals(0, vm1.getCurrentAllocatedBw());
		assertEquals(0, vm2.getCurrentAllocatedBw());

		assertTrue(bwProvisioner.isSuitableForVm(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		bwProvisioner.deallocateBwForVm(vm1);
		assertEquals(BW, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForVm(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm1, BW / 2));
		assertTrue(bwProvisioner.isSuitableForVm(vm2, BW / 2));
		assertTrue(bwProvisioner.allocateBwForVm(vm2, BW / 2));
		assertEquals(0, bwProvisioner.getAvailableBw());

		bwProvisioner.deallocateBwForVm(vm1);
		bwProvisioner.deallocateBwForVm(vm2);
		assertEquals(BW, bwProvisioner.getAvailableBw());
		assertEquals(0, vm1.getCurrentAllocatedBw());
		assertEquals(0, vm2.getCurrentAllocatedBw());
	}

}
