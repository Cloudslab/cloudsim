package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.GoogleHost;
import org.cloudbus.cloudsim.googletrace.GoogleVm;
import org.cloudbus.cloudsim.googletrace.PriorityHostSkin;
import org.cloudbus.cloudsim.googletrace.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Alessandro Lia Fook Santos e João Victor Mafra
 */

public class WorstFitMipsBasedHostSelectionPolicyTest {

    public final double ACCEPTABLE_DIFFERENCE = 0.00001;

    public GoogleHost host1;
    public GoogleHost host2;
    public GoogleHost host3;
    public GoogleHost host4;
    public GoogleHost host5;
    public GoogleHost host6;

    public Vm vm1000;
    public Vm vm500;
    public Vm vm250;
    public Vm vm125;
    public Vm vm62;
    public Vm vm0;
    public Vm vm1200;
    public SortedSet<PriorityHostSkin> hostList;
    public WorstFitMipsBasedHostSelectionPolicy selectionPolicy;


    @Before
    public void setUp() {
        // creating object under test
        selectionPolicy = new WorstFitMipsBasedHostSelectionPolicy();

        //creating lists of hosts
        hostList = new TreeSet<PriorityHostSkin>();
        
        // populating host list
        List<Pe> peList = new ArrayList<Pe>();
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        host1 = new GoogleHost(1, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host1, 0));

        host2 = new GoogleHost(2, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host2, 0));

        host3 = new GoogleHost(3, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host3, 0));

        host4 = new GoogleHost(4, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host4, 0));

        host5 = new GoogleHost(5, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host5, 0));

        host6 = new GoogleHost(6, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host6, 0));

        // creating Vm's
        vm1000 = new GoogleVm(1, 1, 1000, 0, 0, 0, 0);
        vm500 = new GoogleVm(2, 1, 500, 0, 0, 0, 0);
        vm250 = new GoogleVm(3, 1, 250, 0, 0, 0, 0);
        vm125 = new GoogleVm(4, 1, 125, 0, 0, 0, 0);
        vm62 = new GoogleVm(5, 1, 62.5, 0, 0, 0, 0);
        vm0 = new GoogleVm(6, 1, 0, 0, 0, 0, 0);
        vm1200 = new GoogleVm(7, 1, 1200, 0, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVmEqualsNull() {
    	SortedSet<PriorityHostSkin> hostList2 = new TreeSet<>();
        selectionPolicy.select(hostList, null);
        selectionPolicy.select(hostList2, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostListEqualsNull() {
        selectionPolicy.select(null, vm1000);
    }

    @Test
    public void testHostListEmpty() {
    	SortedSet<PriorityHostSkin> hostList2 = new TreeSet<>();
        Assert.assertNull(selectionPolicy.select(hostList2, vm1000));
    }


    @Test
    public void testVmBiggerThanFirstHost() {
        Assert.assertNull(selectionPolicy.select(hostList, vm1200));
    }

    @Test
    public void testVmMipsEqualsZero() {
        Assert.assertEquals(host1.getId(), (selectionPolicy.select(hostList, vm0)).getHost().getId());
        Assert.assertEquals(host1.getId(), hostList.first().getHost().getId());
    }

    @Test
    public void testHostIsFull() {
    	SortedSet<PriorityHostSkin> hostList2 = new TreeSet<>();
        // adding a single host in the list
        hostList2.add(new PriorityHostSkin(host1, 0));
        GoogleHost host = selectionPolicy.select(hostList2, vm1000).getHost();

        //allocate a vm that fills the host
        Assert.assertEquals(host1.getId(), host.getId());
        host.vmCreate(vm1000);

        // try allocate a vm in a list of full host
        Assert.assertNull(selectionPolicy.select(hostList2, vm500));

        // try allocate a vm if mips equals zero
        Assert.assertNotNull(selectionPolicy.select(hostList2, vm0));

    }

    @Test
    public void testAllocatingModifyingFirstHost() {

        GoogleHost host = selectionPolicy.select(hostList, vm62).getHost();

        // test if the selected host is equals the first inserted
        Assert.assertEquals(host1.getId(), host.getId());

        //allocate Vm in the selected host
        hostList.remove(new PriorityHostSkin(host, 0));
        host.vmCreate(vm62);
        hostList.add(new PriorityHostSkin(host, 0));

        // test if the last Host in the list is the host1 now
        Assert.assertEquals(host.getId(), (hostList.last()).getHost().getId());
        Assert.assertEquals(host1.getId(), (hostList.last()).getHost().getId());

        //test if the host1 suffer mips changes
        Assert.assertEquals((hostList.last()).getHost().getAvailableMips(), 937.5, ACCEPTABLE_DIFFERENCE);

        // test if host2 is the new selected
        Assert.assertEquals(host2.getId(), (selectionPolicy.select(hostList, vm250)).getHost().getId());
    }

    @Test
    public void testAllocatingMultiplesHosts(){

        for (int i = 0; i < 6; i++){
            GoogleHost otherHost = selectionPolicy.select(hostList, vm1000).getHost();
            hostList.remove(new PriorityHostSkin(otherHost, 0));
            otherHost.vmCreate(vm1000);
            hostList.add(new PriorityHostSkin(otherHost, 0));
        }

        // once all hosts are fully occupied test allocation of vm's
		Assert.assertNull(selectionPolicy.select(hostList, vm1000));
		Assert.assertNull(selectionPolicy.select(hostList, vm500));
		Assert.assertNull(selectionPolicy.select(hostList, vm250));
		Assert.assertNull(selectionPolicy.select(hostList, vm125));
		Assert.assertNull(selectionPolicy.select(hostList, vm62));
        
        GoogleHost otherHost = selectionPolicy.select(hostList, vm0).getHost();
        Assert.assertEquals(otherHost.getId(), host1.getId());
    }


    @Test
    public void testAllocatingVMsWhereFirstHostIsNotSuitable(){
    	
        //creating hosts
        SortedSet<PriorityHostSkin> hosts = new TreeSet<PriorityHostSkin>();
        
        List<Pe> peList = new ArrayList<Pe>();
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); 

        GoogleHost host1 = new GoogleHost(1, peList, new VmSchedulerMipsBased(peList), 3);
        hosts.add(new PriorityHostSkin(host1, 0));

        GoogleHost host2 = new GoogleHost(2, peList, new VmSchedulerMipsBased(peList), 3);
        hosts.add(new PriorityHostSkin(host2, 0));
        
        // creating a VM with priority 0
        GoogleVm vm500P0 = new GoogleVm(1, 1, 500, 0, 0, 0, 0);
        
        Assert.assertEquals(host1, selectionPolicy.select(hosts, vm500P0).getHost());

        // creating vm and updating host1
        hosts.remove(new PriorityHostSkin(host1, 0));
        host1.vmCreate(vm500P0);
        hosts.add(new PriorityHostSkin(host1, 0));
    	
        // creating a VM with priority 2
        GoogleVm vm700P2 = new GoogleVm(2, 1, 700, 0, 0, 2, 0);
        
        Assert.assertEquals(host2, selectionPolicy.select(hosts, vm700P2).getHost());

        // creating vm and updating host2
        hosts.remove(new PriorityHostSkin(host2, 0));
        host2.vmCreate(vm700P2);
        hosts.add(new PriorityHostSkin(host2, 0));

        // creating a VM with priority 1
        GoogleVm vm700P1 = new GoogleVm(3, 1, 700, 0, 0, 1, 0);
        
		/*
		 * besides host1 is that with more available mips, only host2 is
		 * suitable for vm with priority 1
		 */
        Assert.assertEquals(500, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(300, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        
        Assert.assertEquals(host2, selectionPolicy.select(hosts, vm700P1).getHost());

    }

    @Test
    public void testDoubleValues(){
        hostList.clear();

        // create host1 with capacity 62.501
        List<Pe> peList = new ArrayList<Pe>();
        double mips = 62.501;
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        host1 = new GoogleHost(1, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(new PriorityHostSkin(host1, 0));


        // create host2 with capacity 62.5
        List<Pe> peList2 = new ArrayList<Pe>();
        double mips2 = 62.5;
        peList2.add(new Pe(1, new PeProvisionerSimple(mips2))); // need to store Pe id and MIPS Rating

        host2 = new GoogleHost(2, peList2, new VmSchedulerMipsBased(peList2), 1);
        hostList.add(new PriorityHostSkin(host2, 0));

        // create host3 with capacity 62.49
        List<Pe> peList3 = new ArrayList<Pe>();
        double mips3 = 62.49;
        peList3.add(new Pe(2, new PeProvisionerSimple(mips3))); // need to store Pe id and MIPS Rating

        host3 = new GoogleHost(3, peList3, new VmSchedulerMipsBased(peList3), 1);
        hostList.add(new PriorityHostSkin(host3, 0));

        // test if is possible allocate vm62 (with 62.5 mips required) at host1 (its capacity is 62.501)
        Assert.assertEquals(host1, selectionPolicy.select(hostList, vm62).getHost());
        hostList.remove(new PriorityHostSkin(host1, 0));
        host1.vmCreate(vm62);
        hostList.add(new PriorityHostSkin(host1, 0));
        Assert.assertEquals(0.001, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // test if is possible allocate vm62 (with 62.5 mips required) at host2 (its capacity is 62.5)
        Assert.assertEquals(host2, selectionPolicy.select(hostList, vm62).getHost());
        hostList.remove(new PriorityHostSkin(host2, 0));
        host2.vmCreate(vm62);
        hostList.add(new PriorityHostSkin(host2, 0));
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // test if is not possible allocate vm62 (with 62.5 mips required) at host1 (its capacity is 62.49)
        Assert.assertNull(selectionPolicy.select(hostList, vm62));
        Assert.assertEquals(62.49, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

    }
}
