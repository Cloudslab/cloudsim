package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Test;

public class GoogleHostTest {

	@Test
	public void testCompareTo() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1));
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		GoogleHost host2 = new GoogleHost(2, peList2,
				new VmSchedulerMipsBased(peList2));
		
		Assert.assertEquals(1, host1.compareTo(host2));
		Assert.assertEquals(-1, host2.compareTo(host1));
	}
	
	@Test
	public void testOrdering() {
		// host 1
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1));
		
		// host 2
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		GoogleHost host2 = new GoogleHost(2, peList2, new VmSchedulerMipsBased(
				peList2));

		// host 3
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(700)));
		GoogleHost host3 = new GoogleHost(3, peList3, new VmSchedulerMipsBased(
				peList3));

		// host 4
		List<Pe> peList4 = new ArrayList<Pe>();
		peList4.add(new Pe(0, new PeProvisionerSimple(900)));
		GoogleHost host4 = new GoogleHost(4, peList4, new VmSchedulerMipsBased(
				peList4));
			
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
}
