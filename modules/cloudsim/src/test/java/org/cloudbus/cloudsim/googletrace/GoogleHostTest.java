package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Test;

public class GoogleHostTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.00000001;

	@Test
	public void testInitializing() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		Assert.assertEquals(1, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToInUseMips().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializingWithInvalidPriority() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		new GoogleHost(1, peList1, new VmSchedulerMipsBased(peList1), 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializingWithInvalidPriority2() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		new GoogleHost(1, peList1, new VmSchedulerMipsBased(peList1), -1);
	}
	
	@Test
	public void testCompareTo() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		GoogleHost host2 = new GoogleHost(2, peList2,
				new VmSchedulerMipsBased(peList2), 1);
		
		Assert.assertEquals(1, host1.compareTo(host2));
		Assert.assertEquals(-1, host2.compareTo(host1));
	}
	
	@Test
	public void testOrdering() {
		// host 1
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		// host 2
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		GoogleHost host2 = new GoogleHost(2, peList2, new VmSchedulerMipsBased(
				peList2), 1);

		// host 3
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(700)));
		GoogleHost host3 = new GoogleHost(3, peList3, new VmSchedulerMipsBased(
				peList3), 1);

		// host 4
		List<Pe> peList4 = new ArrayList<Pe>();
		peList4.add(new Pe(0, new PeProvisionerSimple(900)));
		GoogleHost host4 = new GoogleHost(4, peList4, new VmSchedulerMipsBased(
				peList4), 1);
			
		// checking sorting
		SortedSet<Host> hosts = new TreeSet<Host>();
		hosts.add(host4);
		hosts.add(host2);
		hosts.add(host3);
		
		Assert.assertEquals(3, hosts.size());
		Assert.assertEquals(host4, hosts.first());
		Assert.assertEquals(host2, hosts.last());
		
		// adding one more host
		hosts.add(host1);
		
		Assert.assertEquals(4, hosts.size());
		Assert.assertEquals(host4, hosts.first());
		Assert.assertEquals(host1, hosts.last());
	}
	
	@Test
	public void testNextVmForPreempting() {
		// setting environment
		Map<Integer, Double> priorityToMipsInUse = new HashMap<Integer, Double>();
		Map<Integer, SortedSet<Vm>> priorityToVms = new HashMap<Integer, SortedSet<Vm>>();
		double cpuReq = 1.0;

		//priority 0
		GoogleVm vm0 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		priorityToMipsInUse.put(0, cpuReq);
		SortedSet<Vm> priority0Vms = new TreeSet<Vm>();
		priority0Vms.add(vm0);
		priorityToVms.put(0, priority0Vms);
		
		// priority 1
		GoogleVm vm1 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 1, 0);
		priorityToMipsInUse.put(1, cpuReq);
		SortedSet<Vm> priority1Vms = new TreeSet<Vm>();
		priority1Vms.add(vm1);
		priorityToVms.put(1, priority1Vms);
				
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 2);

		//host is empty
		Assert.assertNull(host1.nextVmForPreempting());

		host1.setPriorityToInUseMips(priorityToMipsInUse);
		host1.setPriorityToVms(priorityToVms);
		
		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		
		// preempting
		Assert.assertEquals(vm1, host1.nextVmForPreempting());
		
		// simulating removing vm1
		priorityToMipsInUse.put(1, 0d);
		priority1Vms = new TreeSet<Vm>();
		priorityToVms.put(1, priority1Vms);
		
		host1.setPriorityToInUseMips(priorityToMipsInUse);
		host1.setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());

		// preempting
		Assert.assertEquals(vm0, host1.nextVmForPreempting());

		// removing all vms
		host1.vmDestroy(vm0);
		host1.vmDestroy(vm1);

		//host is empty
		Assert.assertNull(host1.nextVmForPreempting());
	}
	
	@Test
	public void testVmCreate() {
		double cpuReq = 1.0;
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 2);
		
		// checking initial environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());
		
		// creating vm0 (priority 0)
		GoogleVm vm0 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		
		Assert.assertTrue(host1.vmCreate(vm0));

		// checking environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPriorityToVms().get(0).first());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());
		
		Assert.assertEquals(100 - cpuReq, host1.getAvailableMips(),ACCEPTABLE_DIFFERENCE);
		
		// creating vm1 (priority 1)
		GoogleVm vm1 = new GoogleVm(2, 1, cpuReq, 1.0, 0, 1, 0);
		
		Assert.assertTrue(host1.vmCreate(vm1));

		// checking environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPriorityToVms().get(0).first());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - 2 * cpuReq, host1.getAvailableMips(),ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testVmDestroy() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 2);

		double cpuReq = 1.0;
		
		GoogleVm vm0 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		GoogleVm vm1 = new GoogleVm(2, 1, cpuReq, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm0));
		Assert.assertTrue(host1.vmCreate(vm1));
		
		// checking initial environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPriorityToVms().get(0).first());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - 2 * cpuReq, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		// destroying vm0
		host1.vmDestroy(vm0);
		
		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - cpuReq, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		

		// destroying vm1
		host1.vmDestroy(vm1);
		
		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());

		Assert.assertEquals(100, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		
	}
	
	@Test
	public void testIsSuitableFor() {
		double cpuReq = 1.0;

		int totalVms = 20;
		int freeCapacity = 5;
		
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(totalVms + freeCapacity)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(1).size());
		
		Assert.assertEquals(freeCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		
		
		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));
	}

	@Test
	public void testIsSuitableFor2(){

		//testing with double
		double cpuReq = 1.0;

		int totalVms = 20;
		double freeCapacity = 0.5;

		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(totalVms + freeCapacity)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(1).size());

		Assert.assertEquals(freeCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));

	}

	@Test
	public void testGetMipsInUseByLessPriorityVms() {
		// setting environment
		Map<Integer, Double> priorityToMipsInUse = new HashMap<Integer, Double>();
		Map<Integer, SortedSet<Vm>> priorityToVms = new HashMap<Integer, SortedSet<Vm>>();
		double cpuReq = 1.0;

		SortedSet<Vm> priority0Vms = new TreeSet<Vm>();
		SortedSet<Vm> priority1Vms = new TreeSet<Vm>();

		for (int id = 0; id < 20; id++) {
			if (id % 2 == 0) {
				priority0Vms.add(new GoogleVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				priority1Vms.add(new GoogleVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}			
		}
		
		priorityToMipsInUse.put(0, 10 * cpuReq);
		priorityToMipsInUse.put(1, 10 * cpuReq);
		
		priorityToVms.put(0,priority0Vms);
		priorityToVms.put(1,priority1Vms);
		
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		host1.setPriorityToInUseMips(priorityToMipsInUse);
		host1.setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(1).size());

		// checking
		Assert.assertEquals(0, host1.getMipsInUseByLessPriorityVms(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getMipsInUseByLessPriorityVms(0), ACCEPTABLE_DIFFERENCE);
	}

	@Test
	public void testHashCode(){

		// creating hosts
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		GoogleHost host2 = new GoogleHost(2, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		// assert expected hashcode
		Assert.assertEquals(1, host1.hashCode());
		Assert.assertEquals(2, host2.hashCode());

		// comparing hashcode of different hosts
		Assert.assertFalse(host1.hashCode() == host2.hashCode());
	}
	
	@Test
	public void testGetAvailableMipsByPriority(){
		// creating hosts
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 3);
		
		//priority 0
		GoogleVm vm0 = new GoogleVm(0, 1, 50, 1.0, 0, 0, 0);
				
		Assert.assertTrue(host1.vmCreate(vm0));
		
		Assert.assertEquals(50, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(50, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(50, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	
		//priority 1
		GoogleVm vm1 = new GoogleVm(1, 1, 20, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm1)); 
		
		Assert.assertEquals(50, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		//priority 1
		GoogleVm vm2 = new GoogleVm(2, 1, 20, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm2)); 
		
		Assert.assertEquals(50, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	}
}