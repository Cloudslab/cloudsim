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
    public SortedSet<Host> hostList2;
    public WorstFitMipsBasedHostSelectionPolicy selectionPolicy;


    @Before
    public void setUp() {

        selectionPolicy = new WorstFitMipsBasedHostSelectionPolicy();
        hostList = new TreeSet<>();
        hostList2 = new TreeSet<>();

        List<Pe> peList = new ArrayList<Pe>();
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        host1 = new GoogleHost(1, peList, new VmSchedulerMipsBased(peList));
        hostList.add(host1);

        host2 = new GoogleHost(2, peList, new VmSchedulerMipsBased(peList));
        hostList.add(host2);

        host3 = new GoogleHost(3, peList, new VmSchedulerMipsBased(peList));
        hostList.add(host3);

        host4 = new GoogleHost(4, peList, new VmSchedulerMipsBased(peList));
        hostList.add(host4);

        host5 = new GoogleHost(5, peList, new VmSchedulerMipsBased(peList));
        hostList.add(host5);

        host6 = new GoogleHost(6, peList, new VmSchedulerMipsBased(peList));
        hostList.add(host6);

        vm1000 = new GoogleVm(1, 1, 1000, 0, 0, 0);
        vm500 = new GoogleVm(2, 1, 500, 0, 0, 0);
        vm250 = new GoogleVm(3, 1, 250, 0, 0, 0);
        vm125 = new GoogleVm(4, 1, 125, 0, 0, 0);
        vm62 = new GoogleVm(5, 1, 62.5, 0, 0, 0);
        vm0 = new GoogleVm(6, 1, 0, 0, 0, 0);
        vm1200 = new GoogleVm(7, 1, 1200, 0, 0, 0);
    }

    @Test
            (expected = IllegalArgumentException.class)
    public void TestVmEqualsNull() {

        selectionPolicy.select(hostList, null);
        selectionPolicy.select(hostList2, null);
    }

    @Test
            (expected = IllegalArgumentException.class)
    public void TestHostListEqualsNull() {
        selectionPolicy.select(null, vm1000);
    }

    @Test
    public void TestHostListEmpty() {
        Assert.assertNull(selectionPolicy.select(hostList2, vm1000));
    }


    @Test
    public void TestVmBiggerThanFirstHost() {
        Assert.assertNull(selectionPolicy.select(hostList, vm1200));
    }

    @Test
    public void TestVmMipsEqualsZero() {
        Assert.assertEquals(host1.getId(), (selectionPolicy.select(hostList, vm0)).getId());
        Assert.assertEquals(host1.getId(), hostList.first().getId());
    }

    @Test
    public void TestHostIsFull() {

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
    public void TestAllocatingModifyingFirstHost() {

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
    public void TestAllocatingMultiplesHosts(){

        for (int i = 0; i < 6; i++){
            Host otherHost = selectionPolicy.select(hostList, vm1000);
            hostList.remove(otherHost);
            otherHost.vmCreate(vm1000);
            hostList.add(otherHost);
        }

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
}
