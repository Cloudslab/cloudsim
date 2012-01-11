/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class VmListTest {

	private List<Vm> vmList;

	@Before
	public void setUp() throws Exception {
		vmList = new ArrayList<Vm>();
	}

	@Test
	public void testGetVMbyID() {
		assertNull(VmList.getById(vmList, 0));
		assertNull(VmList.getById(vmList, 1));
		assertNull(VmList.getById(vmList, 2));

		Vm vm1 = new Vm(0, 0, 0, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 1, 0, 0, 0, "", null);
		Vm vm3 = new Vm(2, 0, 0, 2, 0, 0, 0, "", null);

		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);

		assertSame(vm1, VmList.getById(vmList, 0));
		assertSame(vm2, VmList.getById(vmList, 1));
		assertSame(vm3, VmList.getById(vmList, 2));
	}

	@Test
	public void testGetVMByIdAndUserId() {
		assertNull(VmList.getByIdAndUserId(vmList, 0, 0));
		assertNull(VmList.getByIdAndUserId(vmList, 1, 0));
		assertNull(VmList.getByIdAndUserId(vmList, 0, 1));
		assertNull(VmList.getByIdAndUserId(vmList, 1, 1));

		Vm vm1 = new Vm(0, 0, 0, 1, 0, 0, 0, "", null);
		Vm vm2 = new Vm(1, 0, 0, 1, 0, 0, 0, "", null);
		Vm vm3 = new Vm(0, 1, 0, 2, 0, 0, 0, "", null);
		Vm vm4 = new Vm(1, 1, 0, 2, 0, 0, 0, "", null);

		vmList.add(vm1);
		vmList.add(vm2);
		vmList.add(vm3);
		vmList.add(vm4);

		assertSame(vm1, VmList.getByIdAndUserId(vmList, 0, 0));
		assertSame(vm2, VmList.getByIdAndUserId(vmList, 1, 0));
		assertSame(vm3, VmList.getByIdAndUserId(vmList, 0, 1));
		assertSame(vm4, VmList.getByIdAndUserId(vmList, 1, 1));
	}

}
