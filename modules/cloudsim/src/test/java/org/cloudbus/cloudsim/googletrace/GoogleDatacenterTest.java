package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.googletrace.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class GoogleDatacenterTest {

	GoogleDatacenter datacenter;
	GoogleHost host;
	SimulationTimeUtil timeUtil;
	HostSelectionPolicy hostSelector;
	PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		int num_user = 1; // number of grid users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; // mean trace events
		
		// Initialize the CloudSim library
		CloudSim.init(num_user, calendar, trace_flag);		

		List<Host> hostList = new ArrayList<Host>();
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(10)));
		
		host = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 3);		
		hostList.add(host);

		// mocking
		DatacenterCharacteristics characteristics = Mockito.mock(DatacenterCharacteristics.class);
		Mockito.when(characteristics.getHostList()).thenReturn(hostList);
		
		Mockito.when(characteristics.getNumberOfPes()).thenReturn(1);
		
		timeUtil = Mockito.mock(SimulationTimeUtil.class);
		Mockito.when(timeUtil.clock()).thenReturn(0d);
		
		hostSelector = Mockito.mock(HostSelectionPolicy.class);
		Mockito.when(hostSelector.select(Mockito.any(SortedSet.class), Mockito.any(Vm.class))).thenReturn(host);
		
		List<GoogleHost> googleHostList = new ArrayList<GoogleHost>();
		for (Host host : hostList) {
			googleHostList.add((GoogleHost) host);
		}
		
		preemptableVmAllocationPolicy = new PreemptableVmAllocationPolicy(googleHostList, hostSelector);
		
		datacenter = new GoogleDatacenter("datacenter",
				characteristics, preemptableVmAllocationPolicy,
				new LinkedList<Storage>(), 0);
		
		datacenter.setSimulationTimeUtil(timeUtil);

		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
	}
	
	@Test
	public void testAllocateVm() {		
		int priority = 0;
		double runtime = 10;
		
		GoogleVm vm0 = new GoogleVm(1, 1, 5, 1, 0, priority, runtime);		

		datacenter.allocateHostForVm(false, vm0);
		
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
	}
	
	@Test
	public void testAllocateTwoVmWithSamePriority() {		
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5, 1.0, 0, priority, runtime);		

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		
		GoogleVm vm1 = new GoogleVm(1, 1, 5, 1.0, 0, priority, runtime);		

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

	}
	
	@Test
	public void testAllocateThreeVmWithSamePriorityWithWating() {		
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5, 1.0, 0, priority, runtime);		

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		
		GoogleVm vm1 = new GoogleVm(1, 1, 5, 1.0, 0, priority, runtime);		

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm2 = new GoogleVm(2, 1, 5, 1.0, 0, priority, runtime);
		
		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);
		
		// allocating third vm
		datacenter.allocateHostForVm(false, vm2);
		
		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
	}

	@Ignore
	@Test
	public void testAllocateVmsWithDifferentPriorities() {		
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5, 1.0, 0, priority, runtime);		

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		
		GoogleVm vm1 = new GoogleVm(1, 1, 5, 1.0, 0, priority, runtime);		

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm2 = new GoogleVm(2, 1, 5, 1.0, 0, priority, runtime);
		
		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);
		
		// allocating third vm
		datacenter.allocateHostForVm(false, vm2);
		
		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
	}
}
