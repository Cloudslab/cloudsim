/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;


import java.util.ArrayList;

import org.cloudbus.cloudsim.Vm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author		Anton Beloglazov
 * @author      Remo Andreoli
 * @since		CloudSim Toolkit 2.0
 */
public class PeProvisionerSimpleTest {

	private static final double MIPS = 1000;

	private PeProvisionerSimple peProvisioner;

	@BeforeEach
	public void setUp() throws Exception {
		peProvisioner = new PeProvisionerSimple(MIPS);
	}

	@Test
	public void testGetMips() {
		assertEquals(MIPS, peProvisioner.getMips(), 0);
	}

	@Test
	public void testGetAvailableMips() {
		assertEquals(MIPS, peProvisioner.getAvailableMips(), 0);
	}

	@Test
	public void testGetTotalAllocatedMips() {
		assertEquals(0, peProvisioner.getTotalAllocatedMips(), 0);
	}

	@Test
	public void testGetUtilization() {
		assertEquals(0, peProvisioner.getUtilization(), 0);
	}

	@Test
	public void testAllocateMipsForVm() {
		Vm vm1 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm3 = new Vm(2, 0, MIPS / 2, 2, 0, 0, 0, "", null);

		assertTrue(peProvisioner.allocateMipsForGuest(vm1, MIPS / 2));
		assertEquals(MIPS / 2, peProvisioner.getAvailableMips(), 0);
		assertEquals(MIPS / 2, peProvisioner.getTotalAllocatedMips(), 0);
		assertEquals(0.5, peProvisioner.getUtilization(), 0);

		assertTrue(peProvisioner.allocateMipsForGuest(vm2, MIPS / 4));
		assertEquals(MIPS / 4, peProvisioner.getAvailableMips(), 0);
		assertEquals(MIPS * 3 / 4, peProvisioner.getTotalAllocatedMips(), 0);
		assertEquals(0.75, peProvisioner.getUtilization(), 0);

		assertFalse(peProvisioner.allocateMipsForGuest(vm3, MIPS / 2));
		assertEquals(MIPS / 4, peProvisioner.getAvailableMips(), 0);
		assertEquals(MIPS * 3 / 4, peProvisioner.getTotalAllocatedMips(), 0);
		assertEquals(0.75, peProvisioner.getUtilization(), 0);

		peProvisioner.deallocateMipsForGuest(vm1);
		peProvisioner.deallocateMipsForGuest(vm2);

		assertTrue(peProvisioner.allocateMipsForGuest(vm3, MIPS / 4));
		assertEquals(MIPS * 3 / 4, peProvisioner.getAvailableMips(), 0);
		assertEquals(MIPS / 4, peProvisioner.getTotalAllocatedMips(), 0);
		assertEquals(0.25, peProvisioner.getUtilization(), 0);

		assertTrue(peProvisioner.allocateMipsForGuest(vm3, MIPS / 4));
		assertEquals(MIPS / 2, peProvisioner.getAvailableMips(), 0);
		assertEquals(MIPS / 2, peProvisioner.getTotalAllocatedMips(), 0);
		assertEquals(0.5, peProvisioner.getUtilization(), 0);

		ArrayList<Double> mipsArray = new ArrayList<>();
		mipsArray.add(MIPS / 2.0);
		mipsArray.add(MIPS / 2.0);

		assertTrue(peProvisioner.allocateMipsForGuest(vm3, mipsArray));
		assertEquals(0, peProvisioner.getAvailableMips(), 0);
		assertEquals(MIPS, peProvisioner.getTotalAllocatedMips(), 0);
		assertEquals(1, peProvisioner.getUtilization(), 0);
	}

	@Test
	public void testGetAllocatedMipsForVm() {
		Vm vm1 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm3 = new Vm(2, 0, MIPS / 2, 2, 0, 0, 0, "", null);

		assertNull(peProvisioner.getAllocatedMipsForGuest(vm1));
		assertEquals(0, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm1, 0), 0);

		assertTrue(peProvisioner.allocateMipsForGuest(vm1, MIPS / 2));
		ArrayList<Double> allocatedMips1 = new ArrayList<>();
		allocatedMips1.add(MIPS / 2);
		assertEquals(allocatedMips1, peProvisioner.getAllocatedMipsForGuest(vm1));
		assertEquals(MIPS / 2, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm1, 0), 0);
		assertEquals(0, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm1, 1), 0);
		assertEquals(MIPS / 2, peProvisioner.getTotalAllocatedMipsForGuest(vm1), 0);

		assertTrue(peProvisioner.allocateMipsForGuest(vm2, MIPS / 4));
		ArrayList<Double> allocatedMips2 = new ArrayList<>();
		allocatedMips2.add(MIPS / 4);
		assertEquals(allocatedMips2, peProvisioner.getAllocatedMipsForGuest(vm2));
		assertEquals(MIPS / 4, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm2, 0), 0);
		assertEquals(MIPS / 4, peProvisioner.getTotalAllocatedMipsForGuest(vm2), 0);

		peProvisioner.deallocateMipsForGuest(vm1);
		peProvisioner.deallocateMipsForGuest(vm2);

		assertTrue(peProvisioner.allocateMipsForGuest(vm3, MIPS / 4));
		ArrayList<Double> allocatedMips3 = new ArrayList<>();
		allocatedMips3.add(MIPS / 4);
		assertEquals(allocatedMips3, peProvisioner.getAllocatedMipsForGuest(vm3));
		assertEquals(MIPS / 4, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 0), 0);
		assertEquals(MIPS / 4, peProvisioner.getTotalAllocatedMipsForGuest(vm3), 0);

		assertTrue(peProvisioner.allocateMipsForGuest(vm3, MIPS / 4));
		allocatedMips3.add(MIPS / 4);
		assertEquals(allocatedMips3, peProvisioner.getAllocatedMipsForGuest(vm3));
		assertEquals(MIPS / 4, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 0), 0);
		assertEquals(MIPS / 4, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 1), 0);
		assertEquals(MIPS / 2, peProvisioner.getTotalAllocatedMipsForGuest(vm3), 0);

		ArrayList<Double> allocatedMips4 = new ArrayList<>();
		allocatedMips4.add(MIPS / 2.0);
		allocatedMips4.add(MIPS);
		assertFalse(peProvisioner.allocateMipsForGuest(vm3, allocatedMips4));

		ArrayList<Double> allocatedMips5 = new ArrayList<>();
		allocatedMips5.add(MIPS / 2.0);
		allocatedMips5.add(MIPS / 2.0);
		assertTrue(peProvisioner.allocateMipsForGuest(vm3, allocatedMips5));
		assertEquals(allocatedMips5, peProvisioner.getAllocatedMipsForGuest(vm3));
		assertEquals(MIPS / 2, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 0), 0);
		assertEquals(MIPS / 2, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 1), 0);
		assertEquals(MIPS, peProvisioner.getTotalAllocatedMipsForGuest(vm3), 0);

		peProvisioner.deallocateMipsForGuest(vm1);
		peProvisioner.deallocateMipsForGuest(vm2);
		peProvisioner.deallocateMipsForGuest(vm3);

		assertNull(peProvisioner.getAllocatedMipsForGuest(vm1));
		assertNull(peProvisioner.getAllocatedMipsForGuest(vm2));
		assertNull(peProvisioner.getAllocatedMipsForGuest(vm3));

		assertEquals(0, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm1, 0), 0);
		assertEquals(0, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm2, 0), 0);
		assertEquals(0, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 0), 0);
		assertEquals(0, peProvisioner.getAllocatedMipsForGuestByVirtualPeId(vm3, 1), 0);

		assertEquals(0, peProvisioner.getTotalAllocatedMipsForGuest(vm1), 0);
		assertEquals(0, peProvisioner.getTotalAllocatedMipsForGuest(vm2), 0);
		assertEquals(0, peProvisioner.getTotalAllocatedMipsForGuest(vm3), 0);

		assertEquals(MIPS, peProvisioner.getAvailableMips(), 0);
	}

	@Test
	public void testDeallocateMipsForVM() {
		Vm vm1 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);

		peProvisioner.allocateMipsForGuest(vm1, MIPS / 2);
		peProvisioner.allocateMipsForGuest(vm2, MIPS / 4);

		assertEquals(MIPS / 4, peProvisioner.getAvailableMips(), 0);

		peProvisioner.deallocateMipsForGuest(vm1);

		assertEquals(MIPS * 3 / 4, peProvisioner.getAvailableMips(), 0);

		peProvisioner.deallocateMipsForGuest(vm2);

		assertEquals(MIPS, peProvisioner.getAvailableMips(), 0);
	}

}
