package org.cloudbus.cloudsim.googletrace.util;

import org.cloudbus.cloudsim.googletrace.GoogleHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook and João Victor Mafra on 20/09/16.
 */
public class PriorityHostComparatorTest {
    private GoogleHost host1;
    private GoogleHost host2;
    private PriorityHostComparator comparator0, comparator1, comparator2;


    @Before
    public void setUp() {
        comparator0 = new PriorityHostComparator(0);

        host1 = Mockito.mock(GoogleHost.class);
        host2 = Mockito.mock(GoogleHost.class);

    }

    @Test
    public void testHost1BiggerThanHost2(){
        Mockito.when(host1.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host2.getAvailableMipsByPriority(0)).thenReturn(45.4);

        Assert.assertEquals(comparator0.compare(host1,host2), -1);
        Assert.assertEquals(comparator0.compare(host2,host1), 1);
    }

    @Test
    public void testHost1CapacityEqualsHost2Capacity(){
        Mockito.when(host1.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host2.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host1.getId()).thenReturn(1);
        Mockito.when(host2.getId()).thenReturn(2);

        Assert.assertEquals(comparator0.compare(host1,host2), -1);
        Assert.assertEquals(comparator0.compare(host2,host1), 1);

    }

    @Test
    public void testHost1CapacityEqualsHost2CapacityAndEqualsIds(){
        Mockito.when(host1.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host2.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host1.getId()).thenReturn(1);
        Mockito.when(host2.getId()).thenReturn(1);

        Assert.assertEquals(comparator0.compare(host1,host2), 0);
        Assert.assertEquals(comparator0.compare(host2,host1), 0);

    }


}
