package org.cloudbus.cloudsim.geoweb.web.workload.brokers;

import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.vmplus.disk.HddResCloudlet;
import org.cloudbus.cloudsim.vmplus.disk.HddVm;
import org.cloudbus.cloudsim.vmplus.util.CustomLog;
import org.cloudbus.cloudsim.vmplus.util.TextUtil;
import org.cloudbus.cloudsim.geoweb.web.ILoadBalancer;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * A web broker that logs the performance indicators of the virtual machines.
 * 
 * @author nikolay.grozev
 * 
 */
public class PerformanceLoggingWebBroker extends WebBroker {

    protected static final int LOG_TAG = UPDATE_SESSION_TAG + 1;

    public static final List<? extends Class<?>> HEADER_TYPES = Arrays.asList(Double.class, Integer.class,
            Double.class, Double.class, Double.class);

    public static final List<String> HEADER_NAMES = Arrays.asList("time", "vmId", "percentCPU", "percentIO",
            "percentRAM");

    public boolean headerPrinted = false;
    private boolean logStarted = false;
    private double lastTimeCloudletReturned = 0;
    private double lastTimeCloudletSubmited = 0;
    private double offset = 0;

    private final double logPeriod;

    private final double idlePeriod;

    public PerformanceLoggingWebBroker(final String name, final double refreshPeriod, final double lifeLength,
            final double logPeriod, final double offset, final double idlePeriod, Integer dataCenterId)
            throws Exception {
        super(name, refreshPeriod, lifeLength, dataCenterId);
        this.logPeriod = logPeriod;
        this.offset = offset;
        this.idlePeriod = idlePeriod;
    }

    @Override
    protected void processCloudletReturn(final SimEvent ev) {
        lastTimeCloudletReturned = CloudSim.clock();
        super.processCloudletReturn(ev);
    }

    @Override
    protected void submitCloudlets() {
        lastTimeCloudletSubmited = CloudSim.clock();
        super.submitCloudlets();
    }

    @Override
    protected void processOtherEvent(final SimEvent ev) {
        switch (ev.getTag()) {
        case LOG_TAG:
            if (CloudSim.clock() < getLifeLength()) {
                logUtilisation();
                send(getId(), logPeriod, LOG_TAG);
            }
            break;
        case TIMER_TAG:
            if (!logStarted) {
                logStarted = true;
                send(getId(), offset, LOG_TAG);
            }
            break;
        }
        super.processOtherEvent(ev);
    }

    public double getLogPeriod() {
        return logPeriod;
    }

    private void logUtilisation() {
        // If no cloudlet has been submitted or finished - then there is nothing
        // new to log
        double currTime = CloudSim.clock();
        if (currTime - lastTimeCloudletReturned < getIdlePeriod()
                && currTime - lastTimeCloudletSubmited < getIdlePeriod()) {
            for (ILoadBalancer balancer : getLoadBalancers().values()) {
                for (HddVm vm : balancer.getAppServers()) {
                    logUtilisation(vm);
                }
                for (HddVm vm : balancer.getDbBalancer().getVMs()) {
                    logUtilisation(vm);
                }
            }
        }
    }

    public double getIdlePeriod() {
        return idlePeriod;
    }

    public void logUtilisation(final HddVm vm) {
        final Double time = CloudSim.clock();
        final Integer vmId = vm.getId();
        final Double percentCPU = 100 * evaluateCPUUtilization(vm);
        final Double percentIO = 100 * evaluateIOUtilization(vm);
        final Double percentRAM = 100 * evaluateRAMUtilization(vm);

        if (!headerPrinted) {
            CustomLog.printLine(TextUtil.getCaptionLine(HEADER_NAMES, HEADER_TYPES, TextUtil.DEFAULT_DELIM).trim());
            headerPrinted = true;
        }

        CustomLog.printLine(TextUtil.getTxtLine(Arrays.asList(time, vmId, percentCPU, percentIO, percentRAM),
                HEADER_NAMES, TextUtil.DEFAULT_DELIM, false).trim());

    }

    public static double evaluateCPUUtilization(final HddVm vm) {
        double sumExecCloudLets = 0;
        for (ResCloudlet cloudlet : vm.getCloudletScheduler().getCloudletExecList()) {
            sumExecCloudLets += cloudlet.getCloudletLength();
        }
        double vmMips = vm.getMips() * vm.getNumberOfPes();
        return Math.min(1, sumExecCloudLets / vmMips);
    }

    public static double evaluateIOUtilization(final HddVm vm) {
        double sumExecCloudLets = 0;
        for (HddResCloudlet cloudlet : vm.getCloudletScheduler().<HddResCloudlet> getCloudletExecList()) {
            sumExecCloudLets += cloudlet.getCloudlet().getCloudletIOLength();
        }
        double vmIOMips = vm.getIoMips();
        return Math.min(1, sumExecCloudLets / vmIOMips);
    }

    public static double evaluateRAMUtilization(final HddVm vm) {
        double sumExecCloudLets = 0;
        for (HddResCloudlet cloudlet : vm.getCloudletScheduler().<HddResCloudlet> getCloudletExecList()) {
            sumExecCloudLets += cloudlet.getCloudlet().getRam();
        }
        double vmRam = vm.getRam();
        return Math.min(1, sumExecCloudLets / vmRam);
    }

}
