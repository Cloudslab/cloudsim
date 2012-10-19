/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class HostDynamicWorkloadTest {

	private static final int ID = 0;
	private static final long STORAGE = Consts.MILLION;
	private static final int RAM = 1024;
	private static final int BW = 10000;
	private static final double MIPS = 1000;

	private HostDynamicWorkload host;
	private List<Pe> peList;

	@Before
	public void setUp() throws Exception {
		peList = new ArrayList<Pe>();
		peList.add(new Pe(0, new PeProvisionerSimple(MIPS)));
		peList.add(new Pe(1, new PeProvisionerSimple(MIPS)));

		host = new HostDynamicWorkload (
			ID,
			new RamProvisionerSimple(RAM),
			new BwProvisionerSimple(BW),
			STORAGE,
			peList,
			new VmSchedulerTimeShared(peList)
		);
	}

	@Test
	public void testGetUtilizationOfCPU() {
		assertEquals(0, host.getUtilizationOfCpu(), 0);
	}

	@Test
	public void testGetUtilizationOfCPUMips() {
		assertEquals(0, host.getUtilizationOfCpuMips(), 0);
	}

	@Test
	public void testGetUtilizationOfRam() {
		assertEquals(0, host.getUtilizationOfRam(), 0);
	}

	@Test
	public void testGetUtilizationOfBW() {
		assertEquals(0, host.getUtilizationOfBw(), 0);
	}

	@Test
	public void testGetMaxUtilization() {
		Vm vm0 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm1 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);

		assertTrue(peList.get(0).getPeProvisioner().allocateMipsForVm(vm0, MIPS / 3));
		assertTrue(peList.get(1).getPeProvisioner().allocateMipsForVm(vm1, MIPS / 5));

		assertEquals((MIPS / 3) / MIPS, host.getMaxUtilization(), 0.001);
	}

	@Test
	public void testGetMaxUtilizationAmongVmsPes() {
		Vm vm0 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, "", null);
		Vm vm1 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, "", null);

		assertTrue(peList.get(0).getPeProvisioner().allocateMipsForVm(vm0, MIPS / 3));
		assertTrue(peList.get(1).getPeProvisioner().allocateMipsForVm(vm1, MIPS / 5));

		assertEquals((MIPS / 3) / MIPS, host.getMaxUtilizationAmongVmsPes(vm0), 0.001);
		assertEquals((MIPS / 5) / MIPS, host.getMaxUtilizationAmongVmsPes(vm1), 0.001);
	}

}
