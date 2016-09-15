package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.GoogleHost;
import org.cloudbus.cloudsim.googletrace.GoogleVm;
import org.cloudbus.cloudsim.googletrace.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Alessandro Lia Fook Santos e João Victor Mafra
 */

public class WorstFitMipsBasedHostSelectionPolicyTest {

    public final double ACCEPTABLE_DIFFERENCE = 0.1;

    public Host host1;
    public Host host2;
    public Host host3;
    public Host host4;
    public Host host5;
    public Host host6;

    public Vm vm1000;
    public Vm vm500;
    public Vm vm250;
    public Vm vm125;
    public Vm vm62;
    public Vm vm0;
    public Vm vm1200;
    public SortedSet<Host> hostList;
    public WorstFitMipsBasedHostSelectionPolicy selectionPolicy;


    @Before
    public void setUp() {
        // creating object under test
        selectionPolicy = new WorstFitMipsBasedHostSelectionPolicy();

        //creating lists of hosts
        hostList = new TreeSet<>();
        
        // populating host list
        List<Pe> peList = new ArrayList<Pe>();
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        host1 = new GoogleHost(1, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(host1);

        host2 = new GoogleHost(2, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(host2);

        host3 = new GoogleHost(3, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(host3);

        host4 = new GoogleHost(4, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(host4);

        host5 = new GoogleHost(5, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(host5);

        host6 = new GoogleHost(6, peList, new VmSchedulerMipsBased(peList), 1);
        hostList.add(host6);


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
    	SortedSet<Host> hostList2 = new TreeSet<>();
        selectionPolicy.select(hostList, null);
        selectionPolicy.select(hostList2, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHostListEqualsNull() {
        selectionPolicy.select(null, vm1000);
    }

    @Test
    public void testHostListEmpty() {
    	SortedSet<Host> hostList2 = new TreeSet<>();
        Assert.assertNull(selectionPolicy.select(hostList2, vm1000));
    }


    @Test
    public void testVmBiggerThanFirstHost() {
        Assert.assertNull(selectionPolicy.select(hostList, vm1200));
    }

    @Test
    public void testVmMipsEqualsZero() {
        Assert.assertEquals(host1.getId(), (selectionPolicy.select(hostList, vm0)).getId());
        Assert.assertEquals(host1.getId(), hostList.first().getId());
    }

    @Test
    public void testHostIsFull() {
    	SortedSet<Host> hostList2 = new TreeSet<>();
        // adding a single host in the list
        hostList2.add(host1);
        GoogleHost host = (GoogleHost) selectionPolicy.select(hostList2, vm1000);

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

        GoogleHost host = (GoogleHost) selectionPolicy.select(hostList, vm62);

        // test if the selected host is equals the first inserted
        Assert.assertEquals(host1.getId(), host.getId());

        //allocate Vm in the selected host
        hostList.remove(host);
        host.vmCreate(vm62);
        hostList.add(host);

        // test if the last Host in the list is the host1 now
        Assert.assertEquals(host.getId(), (hostList.last()).getId());
        Assert.assertEquals(host1.getId(), (hostList.last()).getId());

        //test if the host1 suffer mips changes
        Assert.assertEquals((hostList.last()).getAvailableMips(), 937.5, ACCEPTABLE_DIFFERENCE);

        // test if host2 is the new selected
        Assert.assertEquals(host2.getId(), (selectionPolicy.select(hostList, vm250)).getId());
    }

    @Test
    public void testAllocatingMultiplesHosts(){

        for (int i = 0; i < 6; i++){
            Host otherHost = selectionPolicy.select(hostList, vm1000);
            hostList.remove(otherHost);
            otherHost.vmCreate(vm1000);
            hostList.add(otherHost);
        }


        // once all hosts are fully occupied test allocation of vm's
        Host otherHost = selectionPolicy.select(hostList, vm1000);
        Assert.assertNull(otherHost);

        otherHost = selectionPolicy.select(hostList, vm500);
        Assert.assertNull(otherHost);

        otherHost = selectionPolicy.select(hostList, vm250);
        Assert.assertNull(otherHost);

        otherHost = selectionPolicy.select(hostList, vm125);
        Assert.assertNull(otherHost);

        otherHost = selectionPolicy.select(hostList, vm62);
        Assert.assertNull(otherHost);

        otherHost = selectionPolicy.select(hostList, vm0);
        Assert.assertEquals(otherHost.getId(), host1.getId());
    }
    
    @Test
    public void testAllocatingVMsWhereFirstHostIsNotSuitable(){
    	
        //creating hosts
        SortedSet<Host> hosts = new TreeSet<Host>();
        
        List<Pe> peList = new ArrayList<Pe>();
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); 

        Host host1 = new GoogleHost(1, peList, new VmSchedulerMipsBased(peList), 3);
        hosts.add(host1);

        Host host2 = new GoogleHost(2, peList, new VmSchedulerMipsBased(peList), 3);
        hosts.add(host2);
        
        // creating a VM with priority 0
        GoogleVm vm500P0 = new GoogleVm(1, 1, 500, 0, 0, 0, 0);
        
        Assert.assertEquals(host1, selectionPolicy.select(hosts, vm500P0));

        // creating vm and updating host1
        hosts.remove(host1);
        host1.vmCreate(vm500P0);
        hosts.add(host1);
    	
        // creating a VM with priority 2
        GoogleVm vm700P2 = new GoogleVm(2, 1, 700, 0, 0, 2, 0);
        
        Assert.assertEquals(host2, selectionPolicy.select(hosts, vm700P2));

        // creating vm and updating host2
        hosts.remove(host2);
        host2.vmCreate(vm700P2);
        hosts.add(host2);

        // creating a VM with priority 1
        GoogleVm vm700P1 = new GoogleVm(3, 1, 700, 0, 0, 1, 0);
        
		/*
		 * besides host1 is that with more available mips, only host2 is
		 * suitable for vm with priority 1
		 */
        Assert.assertEquals(500, host1.getAvailableMips(), 0.0001);
        Assert.assertEquals(300, host2.getAvailableMips(), 0.0001);
        
        Assert.assertEquals(host2, selectionPolicy.select(hosts, vm700P1));
        
        // creating a smaller VM with priority 1
        GoogleVm vm300P1 = new GoogleVm(3, 1, 300, 0, 0, 1, 0);
        
		/*
		 * In this case, host1 has enough resource to allocate the new VM and
		 * will be return by the selector
		 */
        Assert.assertEquals(500, host1.getAvailableMips(), 0.0001);
        Assert.assertEquals(300, host2.getAvailableMips(), 0.0001);
        
        Assert.assertEquals(host1, selectionPolicy.select(hosts, vm300P1));
    }
}
