package org.cloudbus.cloudsim.power.lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerPe;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSqrt;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Before;
import org.junit.Test;

public class PeListTest {
	
	private static final double MIPS = 1000;
	
	private static final double MAX_POWER = 200;
	private static final double STATIC_POWER_PERCENT = 0.3;
	
	private List<PowerPe> peList;
	private PowerModel powerModel0;
	private PowerModel powerModel1;

	@Before
	public void setUp() throws Exception {
		peList = new ArrayList<PowerPe>();
		
		powerModel0 = new PowerModelSqrt(MAX_POWER, STATIC_POWER_PERCENT);
		powerModel1 = new PowerModelSqrt(MAX_POWER, STATIC_POWER_PERCENT);
		
		peList.add(new PowerPe(0, new PeProvisionerSimple(MIPS), powerModel0));
		peList.add(new PowerPe(1, new PeProvisionerSimple(MIPS), powerModel1));
	}
		
	@Test
	public void testGetPower() throws Exception {
		Vm vm0 = new Vm(0, 0, MIPS / 2, 1, 0, 0, 0, 0, "", null);
		Vm vm1 = new Vm(1, 0, MIPS / 2, 1, 0, 0, 0, 0, "", null);
	
		assertTrue(peList.get(0).getPeProvisioner().allocateMipsForVm(vm0, MIPS / 3)); 
		assertTrue(peList.get(1).getPeProvisioner().allocateMipsForVm(vm1, MIPS / 5));
		
		double expectedPower = powerModel0.getPower((double) (MIPS / 3) / MIPS) + powerModel1.getPower((double) (MIPS / 5) / MIPS);
		assertEquals(expectedPower, PowerPeList.getPower(peList), 0);
	}
	
}
