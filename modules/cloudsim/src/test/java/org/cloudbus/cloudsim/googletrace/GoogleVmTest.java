package org.cloudbus.cloudsim.googletrace;

import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
import org.junit.Assert;
import org.junit.Test;

public class GoogleVmTest {

	@Test
	public void testCompareTo() {
		int submitTime = 0;
		int priority = 0;
		
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, submitTime, priority);		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, submitTime + 1, priority + 1);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}
	
	@Test
	public void testCompareTo2() {
		int submitTime = 0;
		int priority = 0;
		
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, submitTime, priority);		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, submitTime, priority);
		
		Assert.assertEquals(0, vm1.compareTo(vm2));
		Assert.assertEquals(0, vm2.compareTo(vm1));
		
	}

	@Test
	public void testCompareTo3() {
		int submitTime = 0;
		int priority = 0;
		
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, submitTime, priority);		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, submitTime + 1, priority);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}
	
	@Test
	public void testCompareTo4() {
		int submitTime = 0;
		int priority = 0;
		
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, submitTime, priority);		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, submitTime, priority + 1);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}
	
	@Test
	public void testCompareTo5() {
		int submitTime = 0;
		int priority = 0;
		
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, submitTime + 1, priority);		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, submitTime, priority + 1);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}
	
	@Test
	public void testOrdering() {
		int submitTime = 10;
		int priority = 0;
		
		GoogleVm vm1 = new GoogleVm(1, 1, 1.0, 1.0, submitTime, priority);		
		GoogleVm vm2 = new GoogleVm(2, 1, 1.0, 1.0, submitTime + 1, priority);
		GoogleVm vm3 = new GoogleVm(3, 1, 1.0, 1.0, submitTime, priority + 1);
		
		// testing compareTo
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(-1, vm1.compareTo(vm3));
		
		Assert.assertEquals(1, vm3.compareTo(vm1));
		Assert.assertEquals(1, vm3.compareTo(vm2));
		Assert.assertEquals(0, vm3.compareTo(vm3));
				
		// test ordering
		SortedSet<Vm> sortedVms = new TreeSet<Vm>();		
		Assert.assertTrue(sortedVms.isEmpty());
		
		sortedVms.add(vm3);
		sortedVms.add(vm1);
		sortedVms.add(vm2);

		// checking ordering
		Assert.assertEquals(3, sortedVms.size());
		Assert.assertTrue(sortedVms.contains(vm1));
		Assert.assertTrue(sortedVms.contains(vm2));
		Assert.assertTrue(sortedVms.contains(vm3));
		
		Assert.assertEquals(vm1, sortedVms.first());
		Assert.assertEquals(vm3, sortedVms.last());
		
		// adding more elements
		GoogleVm vm4 = new GoogleVm(4, 1, 1.0, 1.0, submitTime + 1, priority + 1);
		GoogleVm vm5 = new GoogleVm(5, 1, 1.0, 1.0, submitTime, priority + 2);
		
		// testing compareTo
		Assert.assertEquals(-1, vm1.compareTo(vm4));
		Assert.assertEquals(-1, vm1.compareTo(vm5));
		
		Assert.assertEquals(-1, vm3.compareTo(vm4));
		Assert.assertEquals(-1, vm3.compareTo(vm5));
		
		Assert.assertEquals(1, vm5.compareTo(vm1));
		Assert.assertEquals(1, vm5.compareTo(vm2));
		Assert.assertEquals(1, vm5.compareTo(vm3));
		Assert.assertEquals(1, vm5.compareTo(vm4));

		sortedVms.add(vm5);
		sortedVms.add(vm4);
		
		// checking ordering
		Assert.assertEquals(5, sortedVms.size());
		Assert.assertTrue(sortedVms.contains(vm1));
		Assert.assertTrue(sortedVms.contains(vm2));
		Assert.assertTrue(sortedVms.contains(vm3));
		Assert.assertTrue(sortedVms.contains(vm4));
		Assert.assertTrue(sortedVms.contains(vm5));

		Assert.assertEquals(vm1, sortedVms.first());
		Assert.assertEquals(vm5, sortedVms.last());
	}

}
