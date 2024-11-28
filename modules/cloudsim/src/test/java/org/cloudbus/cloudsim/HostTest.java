/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;


import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author		Anton Beloglazov
 * @author 		Remo Andreoli
 * @since		CloudSim Toolkit 2.0
 */
public class HostTest {

	private static final int ID = 0;
	private static final long STORAGE = Consts.MILLION;
	private static final int RAM = 1024;
	private static final int BW = 10000;
	private static final double MIPS = 1000;

	//private static final int PES_NUMBER = 2;

	//private static final double CLOUDLET_LENGTH = 1000;
	//private static final long CLOUDLET_FILE_SIZE = 300;
	//private static final long CLOUDLET_OUTPUT_SIZE = 300;

	private Host host;
	private List<Pe> peList;

	@BeforeEach
	public void setUp() throws Exception {
		peList = new ArrayList<>();
		peList.add(new Pe(0, new PeProvisionerSimple(MIPS)));
		peList.add(new Pe(1, new PeProvisionerSimple(MIPS)));

		host = new Host(
			ID,
			new RamProvisionerSimple(RAM),
			new BwProvisionerSimple(BW),
			STORAGE,
			peList,
			new VmSchedulerTimeShared(peList)
		);
	}

	@Test
	public void testIsSuitableForVm() {
		Vm vm0 = new Vm(0, 0, MIPS, 2, RAM, BW, 0, "", new CloudletSchedulerDynamicWorkload(MIPS, 2));
		Vm vm1 = new Vm(1, 0, MIPS * 2, 1, RAM * 2, BW * 2, 0, "", new CloudletSchedulerDynamicWorkload(MIPS * 2, 2));

		assertTrue(host.isSuitableForGuest(vm0));
		assertFalse(host.isSuitableForGuest(vm1));
	}

	@Test
	public void testVmCreate() {
		Vm vm0 = new Vm(0, 0, MIPS / 2, 1, RAM / 2, BW / 2, 0, "", new CloudletSchedulerDynamicWorkload(MIPS / 2, 1));
		Vm vm1 = new Vm(1, 0, MIPS, 1, RAM, BW, 0, "", new CloudletSchedulerDynamicWorkload(MIPS, 1));
		Vm vm2 = new Vm(2, 0, MIPS * 2, 1, RAM, BW, 0, "", new CloudletSchedulerDynamicWorkload(MIPS * 2, 1));
		Vm vm3 = new Vm(3, 0, MIPS / 2, 2, RAM / 2, BW / 2, 0, "", new CloudletSchedulerDynamicWorkload(MIPS / 2, 2));

		assertTrue(host.guestCreate(vm0));
		assertFalse(host.guestCreate(vm1));
		assertFalse(host.guestCreate(vm2));
		assertTrue(host.guestCreate(vm3));
	}

	@Test
	public void testVmDestroy() {
		Vm vm = new Vm(0, 0, MIPS, 1, RAM / 2, BW / 2, 0, "", new CloudletSchedulerDynamicWorkload(MIPS, 1));

		assertTrue(host.guestCreate(vm));
		assertSame(vm, host.getGuest(0, 0));
		assertEquals(MIPS, host.getGuestScheduler().getAvailableMips(), 0);

		host.guestDestroy(vm);
		assertNull(host.getGuest(0, 0));
		assertEquals(0, host.getGuestList().size());
		assertEquals(MIPS * 2, host.getGuestScheduler().getAvailableMips(), 0);
	}

	@Test
	public void testVmDestroyAll() {
		Vm vm0 = new Vm(0, 0, MIPS, 1, RAM / 2, BW / 2, 0, "", new CloudletSchedulerDynamicWorkload(MIPS, 1));
		Vm vm1 = new Vm(1, 0, MIPS, 1, RAM / 2, BW / 2, 0, "", new CloudletSchedulerDynamicWorkload(MIPS, 1));

		assertTrue(host.guestCreate(vm0));
		assertSame(vm0, host.getGuest(0, 0));
		assertEquals(MIPS, host.getGuestScheduler().getAvailableMips(), 0);

		assertTrue(host.guestCreate(vm1));
		assertSame(vm1, host.getGuest(1, 0));
		assertEquals(0, host.getGuestScheduler().getAvailableMips(), 0);

		host.guestDestroyAll();
		assertNull(host.getGuest(0, 0));
		assertNull(host.getGuest(1, 0));
		assertEquals(0, host.getGuestList().size());
		assertEquals(MIPS * 2, host.getGuestScheduler().getAvailableMips(), 0);
	}

//	@Test
//	public void testUpdateVmsProcessing() {
//		UtilizationModelStochastic utilizationModel1 = new UtilizationModelStochastic();
//		UtilizationModelStochastic utilizationModel2 = new UtilizationModelStochastic();
//
//		VMGridlet gridlet1 = new VMGridlet(0, 0, GRIDLET_LENGTH, GRIDLET_FILE_SIZE, GRIDLET_OUTPUT_SIZE, PES_NUMBER, utilizationModel1, utilizationModel1, utilizationModel1);
//		VMGridlet gridlet2 = new VMGridlet(0, 0, GRIDLET_LENGTH, GRIDLET_FILE_SIZE, GRIDLET_OUTPUT_SIZE, PES_NUMBER, utilizationModel2, utilizationModel2, utilizationModel2);
//
//		gridlet1.setResourceParameter(0, 0, 0);
//		gridlet2.setResourceParameter(0, 0, 0);
//
//		int[] mipsShare = { (int) (GRIDLET_LENGTH / 2) };
//		vmScheduler.setPEMips(mipsShare[0]);
//
//		double utilization1 = utilizationModel1.getUtilization(0);
//		double utilization2 = utilizationModel2.getUtilization(0);
//
//		vmScheduler.gridletSubmit(gridlet1);
//		vmScheduler.gridletSubmit(gridlet2);
//
//		double actualCompletionTime = vmScheduler.updateVMProcessing(0, mipsShare);
//
//		double completionTime1 = GRIDLET_LENGTH / (utilization1 * GRIDLET_LENGTH / 2);
//		double completionTime2 = GRIDLET_LENGTH / (utilization2 * GRIDLET_LENGTH / 2);
//
//		double expectedCompletiontime;
//		if (completionTime1 < completionTime2) {
//			expectedCompletiontime = completionTime1;
//		} else {
//			expectedCompletiontime = completionTime2;
//		}
//
//		assertEquals(expectedCompletiontime, actualCompletionTime, 0);
//
//		actualCompletionTime = vmScheduler.updateVMProcessing(1, mipsShare);
//
//		completionTime1 = 1 + (GRIDLET_LENGTH - utilization1 * GRIDLET_LENGTH / 2 * 1) / (utilizationModel1.getUtilization(1) * GRIDLET_LENGTH / 2);
//		completionTime2 = 1 + (GRIDLET_LENGTH - utilization2 * GRIDLET_LENGTH / 2 * 1) / (utilizationModel2.getUtilization(1) * GRIDLET_LENGTH / 2);
//
//		if (completionTime1 < completionTime2) {
//			expectedCompletiontime = completionTime1;
//		} else {
//			expectedCompletiontime = completionTime2;
//		}
//
//		assertEquals(expectedCompletiontime, actualCompletionTime, 0);
//
//		assertFalse(vmScheduler.isFinishedGridlets());
//
//		assertEquals(0, vmScheduler.updateVMProcessing(GRIDLET_LENGTH, mipsShare), 0);
//
//		assertTrue(vmScheduler.isFinishedGridlets());
//	}

//	@Test
//	public void testUpdateVmsProcessing() {
//		UtilizationModelStochastic utilizationModel1 = new UtilizationModelStochastic();
//		UtilizationModelStochastic utilizationModel2 = new UtilizationModelStochastic();
//
//		Cloudlet cloudlet1 = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
//				utilizationModel1, utilizationModel1, utilizationModel1);
//
//		Cloudlet cloudlet2 = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
//				utilizationModel2, utilizationModel2, utilizationModel2);
//
//		cloudlet1.setResourceParameter(0, 0, 0);
//		cloudlet2.setResourceParameter(0, 0, 0);
//
//		CloudletSchedulerSingleService vmScheduler = new CloudletSchedulerSingleService(PES_NUMBER, MIPS);
//
//		int[] mipsShare = { (int) (CLOUDLET_LENGTH / 2) };
//		vmScheduler.setCurrentMipsShare(mipsShare);
//		//vmScheduler.setMips(mipsShare[0]);
//
//		double utilization1 = utilizationModel1.getUtilization(0);
//		double utilization2 = utilizationModel2.getUtilization(0);
//
//		vmScheduler.cloudletSubmit(cloudlet1);
//		vmScheduler.cloudletSubmit(cloudlet2);
//
//		double actualCompletionTime = vmScheduler.updateVmProcessing(0, mipsShare);
//
//		double completionTime1 = CLOUDLET_LENGTH / (utilization1 * CLOUDLET_LENGTH / 2);
//		double completionTime2 = CLOUDLET_LENGTH / (utilization2 * CLOUDLET_LENGTH / 2);
//
//		double expectedCompletiontime;
//		if (completionTime1 < completionTime2) {
//			expectedCompletiontime = completionTime1;
//		} else {
//			expectedCompletiontime = completionTime2;
//		}
//
//		assertEquals(expectedCompletiontime, actualCompletionTime, 0);
//
//		actualCompletionTime = vmScheduler.updateVmProcessing(1, mipsShare);
//
//		completionTime1 = 1 + (CLOUDLET_LENGTH - utilization1 * CLOUDLET_LENGTH / 2 * 1) / (utilizationModel1.getUtilization(1) * CLOUDLET_LENGTH / 2);
//		completionTime2 = 1 + (CLOUDLET_LENGTH - utilization2 * CLOUDLET_LENGTH / 2 * 1) / (utilizationModel2.getUtilization(1) * CLOUDLET_LENGTH / 2);
//
//		if (completionTime1 < completionTime2) {
//			expectedCompletiontime = completionTime1;
//		} else {
//			expectedCompletiontime = completionTime2;
//		}
//
//		assertEquals(expectedCompletiontime, actualCompletionTime, 0);
//
//		assertFalse(vmScheduler.isFinishedCloudlets());
//
//		assertEquals(0, vmScheduler.updateVmProcessing(CLOUDLET_LENGTH, mipsShare), 0);
//
//		assertTrue(vmScheduler.isFinishedCloudlets());
//
//	}

}
