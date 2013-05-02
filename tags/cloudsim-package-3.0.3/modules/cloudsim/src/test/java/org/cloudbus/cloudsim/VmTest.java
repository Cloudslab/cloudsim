/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class VmTest {

	private static final int ID = 1;

	private static final int USER_ID = 1;

	private static final double MIPS = 1000;

	private static final int PES_NUMBER = 2;

	private static final int RAM = 1024;

	private static final int BW = 10000;

	private static final long SIZE = 1000;

	private static final String VMM = "Xen";

	private CloudletSchedulerDynamicWorkload vmScheduler;

	private Vm vm;

	@Before
	public void setUp() throws Exception {
		vmScheduler = new CloudletSchedulerDynamicWorkload(MIPS, PES_NUMBER);
		vm = new Vm(ID, USER_ID, MIPS, PES_NUMBER, RAM, BW, SIZE, VMM, vmScheduler);
	}

	@Test
	public void testGetMips() {
		assertEquals(MIPS, vm.getMips(), 0);
	}

	@Test
	public void testSetMips() {
		vm.setMips(MIPS / 2);
		assertEquals(MIPS / 2, vm.getMips(), 0);
	}

	@Test
	public void testGetNumberOfPes() {
		assertEquals(PES_NUMBER, vm.getNumberOfPes());
	}

	@Test
	public void testGetRam() {
		assertEquals(RAM, vm.getRam());
	}

	@Test
	public void testGetBw() {
		assertEquals(BW, vm.getBw());
	}

	@Test
	public void testGetSize() {
		assertEquals(SIZE, vm.getSize());
	}

	@Test
	public void testGetVmm() {
		assertEquals(VMM, vm.getVmm());
	}

	@Test
	public void testGetHost() {
		assertEquals(null, vm.getHost());
		Host host = new Host(0, null, null, 0, new ArrayList<Pe>(), null);
		vm.setHost(host);
		assertEquals(host, vm.getHost());
	}

	@Test
	public void testIsInMigration() {
		assertFalse(vm.isInMigration());
		vm.setInMigration(true);
		assertTrue(vm.isInMigration());
	}

	@Test
	public void testGetTotalUtilization() {
		assertEquals(0, vm.getTotalUtilizationOfCpu(0), 0);
	}

	@Test
	public void testGetTotalUtilizationMips() {
		assertEquals(0, vm.getTotalUtilizationOfCpuMips(0), 0);
	}

	@Test
	public void testGetUid() {
		assertEquals(USER_ID + "-" + ID, vm.getUid());
	}

	@Test
	public void testUpdateVmProcessing() {
		assertEquals(0, vm.updateVmProcessing(0, null), 0);
		ArrayList<Double> mipsShare1 = new ArrayList<Double>();
		mipsShare1.add(1.0);
		ArrayList<Double> mipsShare2 = new ArrayList<Double>();
		mipsShare2.add(1.0);
		assertEquals(vmScheduler.updateVmProcessing(0, mipsShare1), vm.updateVmProcessing(0, mipsShare2), 0);
	}

	@Test
	public void testGetCurrentAllocatedSize() {
		assertEquals(0, vm.getCurrentAllocatedSize());
		vm.setCurrentAllocatedSize(SIZE);
		assertEquals(SIZE, vm.getCurrentAllocatedSize());
	}

	@Test
	public void testGetCurrentAllocatedRam() {
		assertEquals(0, vm.getCurrentAllocatedRam());
		vm.setCurrentAllocatedRam(RAM);
		assertEquals(RAM, vm.getCurrentAllocatedRam());
	}

	@Test
	public void testGetCurrentAllocatedBw() {
		assertEquals(0, vm.getCurrentAllocatedBw());
		vm.setCurrentAllocatedBw(BW);
		assertEquals(BW, vm.getCurrentAllocatedBw());
	}

	@Test
	public void testGetCurrentAllocatedMips() {
		// ArrayList<Integer> currentAllocatedMips = new ArrayList<Integer>();
		// assertEquals(currentAllocatedMips, vm.getCurrentAllocatedMips());
		assertNull(vm.getCurrentAllocatedMips());
	}

	@Test
	public void testIsBeingInstantiated() {
		assertTrue(vm.isBeingInstantiated());
		vm.setBeingInstantiated(false);
		assertFalse(vm.isBeingInstantiated());
	}

	@Test
	public void testGetCurrentRequestedMips() {
		CloudletScheduler cloudletScheduler = createMock(CloudletScheduler.class);
		Vm vm = new Vm(ID, USER_ID, MIPS, PES_NUMBER, RAM, BW, SIZE, VMM, cloudletScheduler);
		vm.setBeingInstantiated(false);

		List<Double> expectedCurrentMips = new ArrayList<Double>();
		expectedCurrentMips.add(MIPS / 2);
		expectedCurrentMips.add(MIPS / 2);

		expect(cloudletScheduler.getCurrentRequestedMips()).andReturn(expectedCurrentMips);

		replay(cloudletScheduler);

		assertEquals(expectedCurrentMips, vm.getCurrentRequestedMips());

		verify(cloudletScheduler);
	}

	@Test
	public void testGetCurrentRequestedTotalMips() {
		CloudletScheduler cloudletScheduler = createMock(CloudletScheduler.class);
		Vm vm = new Vm(ID, USER_ID, MIPS, PES_NUMBER, RAM, BW, SIZE, VMM, cloudletScheduler);

		ArrayList<Double> currentMips = new ArrayList<Double>();
		currentMips.add(MIPS);
		currentMips.add(MIPS);

		expect(cloudletScheduler.getCurrentRequestedMips()).andReturn(currentMips);

		replay(cloudletScheduler);

		assertEquals(MIPS * 2, vm.getCurrentRequestedTotalMips(), 0);

		verify(cloudletScheduler);
	}

}
