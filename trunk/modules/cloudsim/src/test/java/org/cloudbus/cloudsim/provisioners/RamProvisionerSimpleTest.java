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
public class RamProvisionerSimpleTest {

	private static final int RAM = 1000;

	private RamProvisionerSimple ramProvisioner;

	@Before
	public void setUp() throws Exception {
		ramProvisioner = new RamProvisionerSimple(RAM);
	}

	@Test
	public void testGetRam() {
		assertEquals(RAM, ramProvisioner.getRam());
	}

	@Test
	public void testGetAvailableRam() {
		assertEquals(RAM, ramProvisioner.getAvailableRam());
	}

	@Test
	public void testAllocateRamForVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, RAM / 2, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, RAM, 0, 0, "", null);

		assertTrue(ramProvisioner.isSuitableForVm(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm1, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAvailableRam());

		assertFalse(ramProvisioner.isSuitableForVm(vm2, RAM));
		assertFalse(ramProvisioner.allocateRamForVm(vm2, RAM));
		assertEquals(RAM / 2, ramProvisioner.getAvailableRam());

		assertTrue(ramProvisioner.isSuitableForVm(vm2, RAM / 4));
		assertTrue(ramProvisioner.allocateRamForVm(vm2, RAM / 4));
		assertEquals(RAM * 1 / 4, ramProvisioner.getAvailableRam());

		assertTrue(ramProvisioner.isSuitableForVm(vm2, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm2, RAM / 2));
		assertEquals(0, ramProvisioner.getAvailableRam());
	}

	@Test
	public void testGetAllocatedRamForVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, RAM / 2, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, RAM, 0, 0, "", null);

		assertTrue(ramProvisioner.isSuitableForVm(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm1, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAllocatedRamForVm(vm1));

		assertFalse(ramProvisioner.isSuitableForVm(vm2, RAM));
		assertFalse(ramProvisioner.allocateRamForVm(vm2, RAM));
		assertEquals(0, ramProvisioner.getAllocatedRamForVm(vm2));

		assertTrue(ramProvisioner.isSuitableForVm(vm2, RAM / 4));
		assertTrue(ramProvisioner.allocateRamForVm(vm2, RAM / 4));
		assertEquals(RAM / 4, ramProvisioner.getAllocatedRamForVm(vm2));

		assertTrue(ramProvisioner.isSuitableForVm(vm2, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm2, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAllocatedRamForVm(vm2));
	}

	@Test
	public void testDeallocateBwForVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, RAM / 2, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, RAM / 2, 0, 0, "", null);

		assertEquals(0, vm1.getCurrentAllocatedRam());
		assertEquals(0, vm2.getCurrentAllocatedRam());

		assertTrue(ramProvisioner.isSuitableForVm(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm1, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAvailableRam());

		ramProvisioner.deallocateRamForVm(vm1);
		assertEquals(RAM, ramProvisioner.getAvailableRam());

		assertTrue(ramProvisioner.isSuitableForVm(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm1, RAM / 2));
		assertTrue(ramProvisioner.isSuitableForVm(vm2, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForVm(vm2, RAM / 2));
		assertEquals(0, ramProvisioner.getAvailableRam());

		ramProvisioner.deallocateRamForVm(vm1);
		ramProvisioner.deallocateRamForVm(vm2);
		assertEquals(RAM, ramProvisioner.getAvailableRam());
		assertEquals(0, vm1.getCurrentAllocatedRam());
		assertEquals(0, vm2.getCurrentAllocatedRam());
	}

}
