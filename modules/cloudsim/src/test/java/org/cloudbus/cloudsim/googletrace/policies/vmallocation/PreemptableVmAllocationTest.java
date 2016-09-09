package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.GoogleHost;
import org.cloudbus.cloudsim.googletrace.GoogleVm;
import org.cloudbus.cloudsim.googletrace.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PreemptableVmAllocationTest {

	SortedSet<Host> sortedHosts;	
	HostSelectionPolicy hostSelector;
	GoogleHost host1, host2;
	PreemptableVmAllocationPolicy preemptablePolicy;
	
	@Before
	public void setUp() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		host2 = new GoogleHost(2, peList2,
				new VmSchedulerMipsBased(peList2), 1);
		
		List<Host> hosts = new ArrayList<Host>();
		hosts.add(host1);
		hosts.add(host2);
		
		sortedHosts = new TreeSet<Host>(hosts);

		hostSelector = Mockito.mock(HostSelectionPolicy.class);

		preemptablePolicy = new PreemptableVmAllocationPolicy(hosts, hostSelector);
	}
	
	@Test
	public void testAllocateHostForVm() {
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);

		// checking
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testAllocateHostForVm2() {
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm2)).thenReturn(host2);

		// checking
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		
		// allocating the 2nd vm
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2));
		
		// checking 
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());

		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
	}
	
	@Test
	public void testDeallocateHostForVm() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocating
		preemptablePolicy.deallocateHostForVm(vm1);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}
	
	
	@Test
	public void testDeallocateHostForVm2() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host2.vmCreate(vm2));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host2);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// deallocating
		preemptablePolicy.deallocateHostForVm(vm1);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocating
		preemptablePolicy.deallocateHostForVm(vm2);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}
	
	@Test
	public void testOptimizeAllocation() {
		Assert.assertNull(preemptablePolicy
				.optimizeAllocation(new ArrayList<Vm>()));
	}
	
	@Test
	public void testPreempt() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host2.vmCreate(vm2));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host2);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// preempting
		Assert.assertTrue(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting
		Assert.assertTrue(preemptablePolicy.preempt(vm2));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testPreemptInvalidVm() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, 0, 0);
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting invalid vm
		Assert.assertFalse(preemptablePolicy.preempt(vm2));
		
		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testPreemptSameVmMoreThanOneTime() {
		// setting environment
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting vm1
		Assert.assertTrue(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// preempting vm1 again
		Assert.assertFalse(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	
}
