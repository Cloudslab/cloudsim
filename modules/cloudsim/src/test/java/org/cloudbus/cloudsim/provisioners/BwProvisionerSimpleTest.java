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
 * @author      Remo Andreoli
 * @since		CloudSim Toolkit 2.0
 */
public class BwProvisionerSimpleTest {

	private static final long BW = 1000;

	private BwProvisionerSimple bwProvisioner;

	@BeforeEach
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

		assertTrue(bwProvisioner.isSuitableForGuest(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		assertFalse(bwProvisioner.isSuitableForGuest(vm2, BW));
		assertFalse(bwProvisioner.allocateBwForGuest(vm2, BW));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForGuest(vm2, BW / 4));
		assertTrue(bwProvisioner.allocateBwForGuest(vm2, BW / 4));
		assertEquals(BW / 4, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForGuest(vm2, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm2, BW / 2));
		assertEquals(0, bwProvisioner.getAvailableBw());
	}

	@Test
	public void testAllocateBwforExistingVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, 0, BW / 2, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, 0, BW / 4, 0, "", null);

		assertTrue(bwProvisioner.isSuitableForGuest(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForGuest(vm2, BW / 4));
		assertTrue(bwProvisioner.allocateBwForGuest(vm2, BW / 4));
		assertEquals(BW / 4, bwProvisioner.getAvailableBw());

		assertFalse(bwProvisioner.isSuitableForGuest(vm2, BW));
		assertFalse(bwProvisioner.allocateBwForGuest(vm2, BW));
		assertEquals(BW / 4, bwProvisioner.getAvailableBw());
	}

	@Test
	public void testGetAllocatedBwforVm() {
		Vm vm1 = new Vm(0, 0, 0, 0, 0, BW / 2, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, 0, BW, 0, "", null);

		assertTrue(bwProvisioner.isSuitableForGuest(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAllocatedBwForGuest(vm1));

		assertFalse(bwProvisioner.isSuitableForGuest(vm2, BW));
		assertFalse(bwProvisioner.allocateBwForGuest(vm2, BW));
		assertEquals(0, bwProvisioner.getAllocatedBwForGuest(vm2));

		assertTrue(bwProvisioner.isSuitableForGuest(vm2, BW / 4));
		assertTrue(bwProvisioner.allocateBwForGuest(vm2, BW / 4));
		assertEquals(BW / 4, bwProvisioner.getAllocatedBwForGuest(vm2));

		assertTrue(bwProvisioner.isSuitableForGuest(vm2, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm2, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAllocatedBwForGuest(vm2));
	}

	@Test
	public void testDeallocateBwForGuest() {
		Vm vm1 = new Vm(0, 0, 0, 0, 0, BW / 2, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 0, 0, BW / 2, 0, "", null);

		assertEquals(0, vm1.getCurrentAllocatedBw());
		assertEquals(0, vm2.getCurrentAllocatedBw());

		assertTrue(bwProvisioner.isSuitableForGuest(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm1, BW / 2));
		assertEquals(BW / 2, bwProvisioner.getAvailableBw());

		bwProvisioner.deallocateBwForGuest(vm1);
		assertEquals(BW, bwProvisioner.getAvailableBw());

		assertTrue(bwProvisioner.isSuitableForGuest(vm1, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm1, BW / 2));
		assertTrue(bwProvisioner.isSuitableForGuest(vm2, BW / 2));
		assertTrue(bwProvisioner.allocateBwForGuest(vm2, BW / 2));
		assertEquals(0, bwProvisioner.getAvailableBw());

		bwProvisioner.deallocateBwForGuest(vm1);
		bwProvisioner.deallocateBwForGuest(vm2);
		assertEquals(BW, bwProvisioner.getAvailableBw());
		assertEquals(0, vm1.getCurrentAllocatedBw());
		assertEquals(0, vm2.getCurrentAllocatedBw());
	}

}
