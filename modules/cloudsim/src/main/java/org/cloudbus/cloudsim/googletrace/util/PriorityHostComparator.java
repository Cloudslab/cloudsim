package org.cloudbus.cloudsim.googletrace.util;
import org.cloudbus.cloudsim.googletrace.GoogleHost;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook and João Victor Mafra on 20/09/16.
 */
public class PriorityHostComparator implements Comparator<GoogleHost>{

    private int priority;


    public PriorityHostComparator(int priority){
        setPriority(priority);
    }


    @Override
    public int compare(GoogleHost o1, GoogleHost o2) {
        int result = (-1)
                * (new Double(o1.getAvailableMipsByPriority(getPriority()))
                .compareTo(new Double(o2.getAvailableMipsByPriority(getPriority()))));

        if (result == 0)
            return new Integer(o1.getId()).compareTo(new Integer(o2.getId()));

        return result;
    }


    private void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
