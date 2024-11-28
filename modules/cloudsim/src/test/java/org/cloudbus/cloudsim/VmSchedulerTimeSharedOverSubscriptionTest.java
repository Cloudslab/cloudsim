/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class VmSchedulerTimeSharedOverSubscriptionTest {

	private static final double MIPS = 1000;

	private VmSchedulerTimeSharedOverSubscription vmScheduler;

	private List<Pe> peList;

	private Vm vm1;

	private Vm vm2;

	// private Vm vm3;

	@BeforeEach
	public void setUp() throws Exception {
		peList = new ArrayList<>();
		peList.add(new Pe(0, new PeProvisionerSimple(MIPS)));
		peList.add(new Pe(1, new PeProvisionerSimple(MIPS)));
		vmScheduler = new VmSchedulerTimeSharedOverSubscription(peList);
		vm1 = new Vm(0, 0, MIPS / 4, 1, 0, 0, 0, "", null);
		vm2 = new Vm(1, 0, MIPS / 2, 2, 0, 0, 0, "", null);
		// vm3 = new Vm(2, 0, MIPS, 2, 0, 0, 0, 0, "", null);
	}

	@Test
	public void testInit() {
		assertSame(peList, vmScheduler.getPeList());
		assertEquals(PeList.getTotalMips(peList), vmScheduler.getAvailableMips(), 0);
		assertEquals(PeList.getTotalMips(peList), vmScheduler.getMaxAvailableMips(), 0);
		assertEquals(0, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);
	}

	@Test
	public void testAllocatePesForVm() {
		List<Double> mipsShare1 = new ArrayList<>();
		mipsShare1.add(MIPS / 4);

		assertTrue(vmScheduler.allocatePesForGuest(vm1, mipsShare1));

		assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getAvailableMips(), 0);
		assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getMaxAvailableMips(), 0);
		assertEquals(MIPS / 4, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);

		List<Double> mipsShare2 = new ArrayList<>();
		mipsShare2.add(MIPS / 2);
		mipsShare2.add(MIPS / 8);

		assertTrue(vmScheduler.allocatePesForGuest(vm2, mipsShare2));

		assertEquals(
				PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
				vmScheduler.getAvailableMips(),
				0);
		assertEquals(
				PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
				vmScheduler.getMaxAvailableMips(),
				0);
		assertEquals(MIPS / 2 + MIPS / 8, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);

		// List<Double> mipsShare3 = new ArrayList<Double>();
		// mipsShare3.add(MIPS);
		// mipsShare3.add(MIPS);
		//
		// assertTrue(vmScheduler.allocatePesForVm(vm3, mipsShare3));
		//
		// assertEquals(0, vmScheduler.getAvailableMips(), 0);
		// assertEquals(0, vmScheduler.getMaxAvailableMips(), 0);
		// assertEquals(MIPS / 4 - (MIPS / 4 + MIPS / 2 + MIPS / 8 + MIPS + MIPS - MIPS * 2) / 5,
		// vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);
		// assertEquals(MIPS / 2 + MIPS / 8 - (MIPS / 4 + MIPS / 2 + MIPS / 8 + MIPS + MIPS - MIPS *
		// 2) * 2 / 5, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);
		// assertEquals(MIPS * 2 - (MIPS / 4 + MIPS / 2 + MIPS / 8 + MIPS + MIPS - MIPS * 2) * 2 /
		// 5, vmScheduler.getTotalAllocatedMipsForVm(vm3), 0);
		//
		// vmScheduler.deallocatePesForVm(vm1);
		//
		// assertEquals(0, vmScheduler.getAvailableMips(), 0);
		// assertEquals(0, vmScheduler.getMaxAvailableMips(), 0);
		// assertEquals(MIPS / 2 + MIPS / 8 - (MIPS / 2 + MIPS / 8 + MIPS + MIPS - MIPS * 2) * 2 /
		// 4, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);
		// assertEquals(MIPS * 2 - (MIPS / 2 + MIPS / 8 + MIPS + MIPS - MIPS * 2) * 2 / 4,
		// vmScheduler.getTotalAllocatedMipsForVm(vm3), 0);
		//
		// vmScheduler.deallocatePesForVm(vm3);
		//
		// assertEquals(MIPS * 2 - MIPS / 2 - MIPS / 8, vmScheduler.getAvailableMips(), 0);
		// assertEquals(MIPS * 2 - MIPS / 2 - MIPS / 8, vmScheduler.getMaxAvailableMips(), 0);
		// assertEquals(0, vmScheduler.getTotalAllocatedMipsForVm(vm3), 0);
		//
		// vmScheduler.deallocatePesForVm(vm2);

		vmScheduler.deallocatePesForAllGuests();

		assertEquals(PeList.getTotalMips(peList), vmScheduler.getAvailableMips(), 0);
		assertEquals(PeList.getTotalMips(peList), vmScheduler.getMaxAvailableMips(), 0);
		assertEquals(0, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);
	}

	@Test
	public void testAllocatePesForVmInMigration() {
		vm1.setInMigration(true);
		vm2.setInMigration(true);

		List<Double> mipsShare1 = new ArrayList<>();
		mipsShare1.add(MIPS / 4);

		assertTrue(vmScheduler.allocatePesForGuest(vm1, mipsShare1));

		assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getAvailableMips(), 0);
		assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getMaxAvailableMips(), 0);
		assertEquals(0.9 * MIPS / 4, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);

		List<Double> mipsShare2 = new ArrayList<>();
		mipsShare2.add(MIPS / 2);
		mipsShare2.add(MIPS / 8);

		assertTrue(vmScheduler.allocatePesForGuest(vm2, mipsShare2));

		assertEquals(
				PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
				vmScheduler.getAvailableMips(),
				0);
		assertEquals(
				PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
				vmScheduler.getMaxAvailableMips(),
				0);
		assertEquals(0.9 * MIPS / 2 + 0.9 * MIPS / 8, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);

		vmScheduler.deallocatePesForAllGuests();

		assertEquals(PeList.getTotalMips(peList), vmScheduler.getAvailableMips(), 0);
		assertEquals(PeList.getTotalMips(peList), vmScheduler.getMaxAvailableMips(), 0);
		assertEquals(0, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);
	}

	@Test
	public void testAllocatePesForVmShortageEqualsToAllocatedMips() {
		List<Pe> peList = new ArrayList<>();
		peList.add(new Pe(0, new PeProvisionerSimple(3500)));
		VmScheduler vmScheduler = new VmSchedulerTimeSharedOverSubscription(peList);
		Vm vm1 = new Vm(0, 0, 170, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 2000, 1, 0, 0, 0, "", null);
		Vm vm3 = new Vm(2, 0, 10, 1, 0, 0, 0, "", null);
		Vm vm4 = new Vm(3, 0, 2000, 1, 0, 0, 0, "", null);

		List<Double> mipsShare1 = new ArrayList<>();
		mipsShare1.add(170.0);

		List<Double> mipsShare2 = new ArrayList<>();
		mipsShare2.add(2000.0);

		List<Double> mipsShare3 = new ArrayList<>();
		mipsShare3.add(10.0);

		List<Double> mipsShare4 = new ArrayList<>();
		mipsShare4.add(2000.0);

		assertTrue(vmScheduler.allocatePesForGuest(vm1, mipsShare1));
		assertEquals(3330, vmScheduler.getAvailableMips(), 0);
		assertEquals(170, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);

		assertTrue(vmScheduler.allocatePesForGuest(vm2, mipsShare2));
		assertEquals(1330, vmScheduler.getAvailableMips(), 0);
		assertEquals(2000, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);

		assertTrue(vmScheduler.allocatePesForGuest(vm3, mipsShare3));
		assertEquals(1320, vmScheduler.getAvailableMips(), 0);
		assertEquals(10, vmScheduler.getTotalAllocatedMipsForGuest(vm3), 0);

		assertTrue(vmScheduler.allocatePesForGuest(vm4, mipsShare4));
		assertEquals(0, vmScheduler.getAvailableMips(), 0);
		assertEquals(1674, vmScheduler.getTotalAllocatedMipsForGuest(vm4), 0);

		vmScheduler.deallocatePesForAllGuests();

		assertEquals(3500, vmScheduler.getAvailableMips(), 0);
		assertEquals(3500, vmScheduler.getMaxAvailableMips(), 0);
	}
	
	@Test
	public void testAllocatePesForSameSizedVmsOversubscribed() {
		List<Pe> peList = new ArrayList<>();
		peList.add(new Pe(0, new PeProvisionerSimple(1000)));
		VmScheduler vmScheduler = new VmSchedulerTimeSharedOverSubscription(peList);
		Vm vm1 = new Vm(0, 0, 1500, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 1000, 1, 0, 0, 0, "", null);
		Vm vm3 = new Vm(2, 0, 1000, 1, 0, 0, 0, "", null);

		List<Double> mipsShare1 = new ArrayList<>();
		mipsShare1.add(1500.0);

		List<Double> mipsShare2 = new ArrayList<>();
		mipsShare2.add(1000.0);

		List<Double> mipsShare3 = new ArrayList<>();
		mipsShare3.add(1000.0);

		assertTrue(vmScheduler.allocatePesForGuest(vm1, mipsShare1));
		assertEquals(0, vmScheduler.getAvailableMips(), 0);
		assertEquals(1000, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);

		assertTrue(vmScheduler.allocatePesForGuest(vm2, mipsShare2));
		assertEquals(0, vmScheduler.getAvailableMips(), 0);
		assertEquals(500, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);
		assertEquals(500, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);

		assertTrue(vmScheduler.allocatePesForGuest(vm3, mipsShare3));
		assertEquals(0, vmScheduler.getAvailableMips(), 0);
		assertEquals(333, vmScheduler.getTotalAllocatedMipsForGuest(vm1), 0);
		assertEquals(333, vmScheduler.getTotalAllocatedMipsForGuest(vm2), 0);
		assertEquals(333, vmScheduler.getTotalAllocatedMipsForGuest(vm3), 0);

		vmScheduler.deallocatePesForAllGuests();

		assertEquals(1000, vmScheduler.getAvailableMips(), 0);
		assertEquals(1000, vmScheduler.getMaxAvailableMips(), 0);
	}

}
