/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.Vm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class RamProvisionerSimpleTest {

	private static final int RAM = 1000;

	private RamProvisionerSimple ramProvisioner;

	@BeforeEach
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

		assertTrue(ramProvisioner.isSuitableForGuest(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm1, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAvailableRam());

		assertFalse(ramProvisioner.isSuitableForGuest(vm2, RAM));
		assertFalse(ramProvisioner.allocateRamForGuest(vm2, RAM));
		assertEquals(RAM / 2, ramProvisioner.getAvailableRam());

		assertTrue(ramProvisioner.isSuitableForGuest(vm2, RAM / 4));
		assertTrue(ramProvisioner.allocateRamForGuest(vm2, RAM / 4));
		assertEquals(RAM / 4, ramProvisioner.getAvailableRam());

		assertTrue(ramProvisioner.isSuitableForGuest(vm2, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm2, RAM / 2));
		assertEquals(0, ramProvisioner.getAvailableRam());
	}

	@Test
	public void testGetAllocatedRamForVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, RAM / 2, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, RAM, 0, 0, "", null);

		assertTrue(ramProvisioner.isSuitableForGuest(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm1, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAllocatedRamForGuest(vm1));

		assertFalse(ramProvisioner.isSuitableForGuest(vm2, RAM));
		assertFalse(ramProvisioner.allocateRamForGuest(vm2, RAM));
		assertEquals(0, ramProvisioner.getAllocatedRamForGuest(vm2));

		assertTrue(ramProvisioner.isSuitableForGuest(vm2, RAM / 4));
		assertTrue(ramProvisioner.allocateRamForGuest(vm2, RAM / 4));
		assertEquals(RAM / 4, ramProvisioner.getAllocatedRamForGuest(vm2));

		assertTrue(ramProvisioner.isSuitableForGuest(vm2, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm2, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAllocatedRamForGuest(vm2));
	}

	@Test
	public void testDeallocateBwForVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, RAM / 2, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, RAM / 2, 0, 0, "", null);

		assertEquals(0, vm1.getCurrentAllocatedRam());
		assertEquals(0, vm2.getCurrentAllocatedRam());

		assertTrue(ramProvisioner.isSuitableForGuest(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm1, RAM / 2));
		assertEquals(RAM / 2, ramProvisioner.getAvailableRam());

		ramProvisioner.deallocateRamForGuest(vm1);
		assertEquals(RAM, ramProvisioner.getAvailableRam());

		assertTrue(ramProvisioner.isSuitableForGuest(vm1, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm1, RAM / 2));
		assertTrue(ramProvisioner.isSuitableForGuest(vm2, RAM / 2));
		assertTrue(ramProvisioner.allocateRamForGuest(vm2, RAM / 2));
		assertEquals(0, ramProvisioner.getAvailableRam());

		ramProvisioner.deallocateRamForGuest(vm1);
		ramProvisioner.deallocateRamForGuest(vm2);
		assertEquals(RAM, ramProvisioner.getAvailableRam());
		assertEquals(0, vm1.getCurrentAllocatedRam());
		assertEquals(0, vm2.getCurrentAllocatedRam());
	}

}
