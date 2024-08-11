package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.Log;
import java.util.ArrayList;
import java.util.Collections;

/* a CPU, a network link, ... */
public class ScheduledResource extends SimEntity {
    public static class ResourceUse implements Comparable<ResourceUse> {
        public int src;
        public int dst;
        public double amountLeft;
        public CloudActionTags tag;
        public Object data;
        public ResourceUse(int src, int dst, double amountLeft, CloudActionTags tag, Object data) {
            this.src = src;
            this.dst = dst;
            this.amountLeft = amountLeft;
            this.tag = tag;
            this.data = data;
        }
	@Override
	public int compareTo(ResourceUse u) {
            if (amountLeft < u.amountLeft)
                return -1;
            else if (amountLeft > u.amountLeft)
                return 1;
            else
                return 0;
        }
    }
    private double capacity;
    private final ArrayList<ResourceUse> amountsLeft = new ArrayList<>();
    private double lastClock;

    public ScheduledResource(String name, double capacity) {
        super(name);
        this.capacity = capacity;
    }

    public void startEntity() {
        this.lastClock = CloudSim.clock();
    }

    public void updateAmounts() {
        double now = CloudSim.clock();
        if (amountsLeft.size() > 0) {
            ResourceUse ru = amountsLeft.get(0);
            double end_time = lastClock + ru.amountLeft / capacity * amountsLeft.size();
            if (now < end_time) {
                /* we are updating before the first resource consumption finished, update all amountsLeft till now */
                for (ResourceUse ru1 : amountsLeft)
                    ru1.amountLeft -= (now - lastClock) / capacity / amountsLeft.size();
                lastClock = now;
            } else {
                for (ResourceUse ru1 : amountsLeft)
                /* we are updating after the first resource consumption finished, just update till end-time */
                    ru1.amountLeft -= (end_time - lastClock) / capacity / amountsLeft.size();
                lastClock = end_time;
            }
        } else {
            lastClock = now;
        }
    }

    private void scheduleNext() {
        CloudSim.cancelAll(getId(), new PredicateType(CloudActionTags.NETWORK_PKT_FORWARD));
        if (amountsLeft.isEmpty())
            return;
        double delay = amountsLeft.getFirst().amountLeft / capacity * amountsLeft.size();
        schedule(getId(), delay, CloudActionTags.NETWORK_PKT_FORWARD);
    }

    // trigger an event with the specified parameters when amount is consumed
    public void enqueue(ResourceUse ru) {
        updateAmounts();
        amountsLeft.add(ru);
        Collections.sort(amountsLeft);
        scheduleNext();
    }

    // trigger an event with the specified parameters when amount is consumed
    public void enqueue(int src, int dest, double amount, CloudActionTags tag, Object data) {
        ResourceUse ru = new ResourceUse(src, dest, amount, tag, data);
        enqueue(ru);
    }

    public void enqueueDelay(double delay, int src, int dest, double amount, CloudActionTags tag, Object data) {
        ResourceUse ru = new ResourceUse(src, dest, amount, tag, data);
        schedule(getId(), delay, CloudActionTags.BLANK, ru);
    }

    @Override
    public void processEvent(SimEvent ev) {
        CloudSimTags tag = ev.getTag();
        Log.printlnConcat(CloudSim.clock(), ": ", getName(), " processEvent(), tag=", tag);
        if (tag == CloudActionTags.NETWORK_PKT_FORWARD) {
            updateAmounts();
            ResourceUse ru = amountsLeft.get(0);
            amountsLeft.remove(ru);
            CloudSim.send(ru.src, ru.dst, 0.0, ru.tag, ru.data);
            scheduleNext();
        } else if (tag == CloudActionTags.BLANK) {
            ResourceUse ru = (ResourceUse) ev.getData();
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), " processEvent(), ru.amountLeft=", ru.amountLeft);
            enqueue(ru);
        }
    }
}
