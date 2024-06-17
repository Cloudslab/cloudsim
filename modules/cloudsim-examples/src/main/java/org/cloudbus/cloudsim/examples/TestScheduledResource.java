package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.Log;
import java.util.Calendar;

public class TestScheduledResource {
    static ScheduledResource res;
    static SrcEntity src;
    static DstEntity dst;
    static class SrcEntity extends SimEntity {
        public SrcEntity(String name) {
            super(name);
        }
        @Override
        public void processEvent(SimEvent ev) {
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), " processEvent()");
            res.enqueue(SimEvent.SEND, dst.getId(), 100.0, CloudActionTags.BLANK, null);
            res.enqueueDelay(1000.0, SimEvent.SEND, dst.getId(), 100.0, CloudActionTags.BLANK, null);
        }
    }
    static class DstEntity extends SimEntity {
        public DstEntity(String name) {
            super(name);
        }
        @Override
        public void processEvent(SimEvent ev) {
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), " processEvent()");
        }
    }
    public static void main(String[] args) {
        int num_user = 1;
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = true;
        CloudSim.init(num_user, calendar, trace_flag);
        res = new ScheduledResource("res", 1.0);
        src = new SrcEntity("src");
        dst = new DstEntity("dst");

        CloudSim.send(0, src.getId(), 0.0, CloudActionTags.BLANK, null);
        CloudSim.send(0, src.getId(), 10.0, CloudActionTags.BLANK, null);
        CloudSim.send(0, src.getId(), 20.0, CloudActionTags.BLANK, null);

        // simulate
        CloudSim.startSimulation();
        CloudSim.stopSimulation();
    }
}
