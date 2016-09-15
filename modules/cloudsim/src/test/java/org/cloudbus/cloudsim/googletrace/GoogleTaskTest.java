package org.cloudbus.cloudsim.googletrace;

/**
 * Created by Alessandro Fook and João Victor Mafra on 15/09/16.
 */


import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;

public class GoogleTaskTest {

    private static GoogleTask task1, task2, task3, task4, task5, task6, task7;
    private static final double SUBMIT_TIME = 30.546;
    private static final int PRIORITY = 1;

    @Test
    public void testCompareToEquals(){
        task1 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY - 1);
        task2 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY - 1);
        Assert.assertEquals(0, task1.compareTo(task2));
        Assert.assertEquals(0, task2.compareTo(task1));
    }

    @Test
    public void testCompareToId(){
        task1 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        task2 = new GoogleTask(2, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        Assert.assertEquals(-1, task1.compareTo(task2));
        Assert.assertEquals(1, task2.compareTo(task1));
    }

    @Test
    public void testCompareToPrority(){
        task1 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY - 1);
        task2 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        Assert.assertEquals(-1, task1.compareTo(task2));
        Assert.assertEquals(1, task2.compareTo(task1));
    }

    @Test
    public void testCompareToSubmit(){
        task1 = new GoogleTask(1, SUBMIT_TIME - 1, 1, 1, 1, PRIORITY);
        task2 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        Assert.assertEquals(-1, task1.compareTo(task2));
        Assert.assertEquals(1, task2.compareTo(task1));
    }

    @Test
    public void testCompareToSubmit2(){
        task1 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        task2 = new GoogleTask(1, SUBMIT_TIME + 0.0000001, 1, 1, 1, PRIORITY);
        Assert.assertEquals(-1, task1.compareTo(task2));
        Assert.assertEquals(1, task2.compareTo(task1));
    }

    @Test
    public void testCompareToSubmit3(){
        task1 = new GoogleTask(1, SUBMIT_TIME - 0.0000001, 1, 1, 1, PRIORITY);
        task2 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        Assert.assertEquals(-1, task1.compareTo(task2));
        Assert.assertEquals(1, task2.compareTo(task1));
    }

    @Test
    public void testCompareToSubmit4(){
        task1 = new GoogleTask(1, SUBMIT_TIME - 0.0000001, 1, 1, 1, PRIORITY);
        task2 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY - 1);
        Assert.assertEquals(-1, task1.compareTo(task2));
        Assert.assertEquals(1, task2.compareTo(task1));
    }

    @Test
    public void testTreeSetOrder(){
        SortedSet<GoogleTask> sortedTasks = new TreeSet<>();

        task1 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        task2 = new GoogleTask(3, SUBMIT_TIME - 1, 1, 1, 1, PRIORITY + 1);
        task3 = new GoogleTask(2, SUBMIT_TIME + 1, 1, 1, 1, PRIORITY - 1);

        sortedTasks.add(task1);
        sortedTasks.add(task2);
        sortedTasks.add(task3);

        Assert.assertEquals(3, sortedTasks.size());
        Assert.assertEquals(task2, sortedTasks.first());
        Assert.assertEquals(task3, sortedTasks.last());

        // not possible insert task4
        task4 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY);
        sortedTasks.add(task4);
        Assert.assertEquals(3, sortedTasks.size());

        // inserting task4 at first position
        task4 = new GoogleTask(1, SUBMIT_TIME - 1, 1, 1, 1, PRIORITY);
        sortedTasks.add(task4);
        Assert.assertEquals(4, sortedTasks.size());
        Assert.assertEquals(task4, sortedTasks.first());
        Assert.assertEquals(task3, sortedTasks.last());


        // inserting task5 at middle
        task5 = new GoogleTask(1, SUBMIT_TIME, 1, 1, 1, PRIORITY + 1);
        sortedTasks.add(task5);
        Assert.assertEquals(5, sortedTasks.size());
        Assert.assertEquals(task4, sortedTasks.first());
        Assert.assertEquals(task3, sortedTasks.last());


        // inserting task6 at last
        task6 = new GoogleTask(1, SUBMIT_TIME + 1, 1, 1, 1, PRIORITY);
        sortedTasks.add(task6);
        Assert.assertEquals(6, sortedTasks.size());
        Assert.assertEquals(task4, sortedTasks.first());
        Assert.assertEquals(task6, sortedTasks.last());

        // inserting task7 at last by Id
        task7 = new GoogleTask(3, SUBMIT_TIME + 1, 1, 1, 1, PRIORITY);
        sortedTasks.add(task7);
        Assert.assertEquals(7, sortedTasks.size());
        Assert.assertEquals(task4, sortedTasks.first());
        Assert.assertEquals(task7, sortedTasks.last());

        // inserting task8 at middle
        GoogleTask task8 = new GoogleTask(2, SUBMIT_TIME + 1, 1, 1, 1, PRIORITY);
        sortedTasks.add(task8);
        Assert.assertEquals(8, sortedTasks.size());
        Assert.assertEquals(task4, sortedTasks.first());
        Assert.assertEquals(task7, sortedTasks.last());

    }

    @Test
    public void testEquals(){
        // test runTime
        task1 = new GoogleTask(1, SUBMIT_TIME, 3, 1, 1, PRIORITY);
        task2 = new GoogleTask(1, SUBMIT_TIME, 2, 1, 1, PRIORITY);
        task3 = new GoogleTask(1, SUBMIT_TIME, 2, 1, 1, PRIORITY);

        Assert.assertFalse(task1.equals(task2));
        Assert.assertFalse(task1.equals(task3));
        Assert.assertTrue(task2.equals(task3));

    }
}
