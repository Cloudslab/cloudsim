package org.cloudbus.cloudsim.vmplus;

import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.vmplus.disk.HddCloudlet;
import org.cloudbus.cloudsim.vmplus.disk.HddResCloudlet;
import org.cloudbus.cloudsim.vmplus.disk.HddVm;
import org.cloudbus.cloudsim.vmplus.vm.MonitoredVMex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A broker which measures the utilisation of its VMs. This broker only measures
 * the utilisation of vms of type {@link MonitoredVMex}. VMs of other types are
 * processed as usual, and their utilisation is not measured.
 * 
 * <br>
 * <br>
 * 
 * Additionally the broker performs autoscaling periodically in accordance with
 * the specified {@link IAutoscalingPolicy}.
 * 
 * @author nikolay.grozev
 * 
 */
public class MonitoringBorkerEX extends DatacenterBrokerEX {

    // FIXME find a better way to get an unused tag instead of hardcoding ...
    protected static final int BROKER_MEASURE_UTIL_NOW = BROKER_DESTROY_ITSELF_NOW + 20;
    protected static final int BROKER_RECORD_UTIL_NOW = BROKER_MEASURE_UTIL_NOW + 1;
    protected static final int BROKER_AUTOSCALE_NOW = BROKER_RECORD_UTIL_NOW + 1;

    /** The time of the first measurement. */
    private final double offset = Math.min(0.01, CloudSim.getMinTimeBetweenEvents());
    /** The period between subsequent VM utilisation measurements. */
    private final double monitoringPeriod;
    /** The period between subsequent VM autoscaling events. */
    private final double autoScalePeriod;

    public List<IAutoscalingPolicy> getAutoscalingPolicies() {
        return autoscalingPolicies;
    }

    private final List<IAutoscalingPolicy> autoscalingPolicies = new ArrayList<>();

    /**
     * A map, whose entries are in the format [time, Map[vm-id, Array[cpu-util,
     * ram-util, io-util]]] .
     */
    private final LinkedHashMap<Double, Map<Integer, double[]>> recordedUtilisations = new LinkedHashMap<>();
    private double utilisationRecorddDelta = -1;

    /**
     * Constr.
     * 
     * @param name
     *            - the name of the broker.
     * @param lifeLength
     *            - for how long we need to keep this broker alive. If -1, then
     *            the broker is kept alive/running untill all cloudlets
     *            complete.
     * @param monitoringPeriod
     *            - the period between subsequent measurements.
     * @param autoScalePeriod
     *            - the period between subsequent autoscalings.
     * @throws Exception
     *             - from the superclass.
     */
    public MonitoringBorkerEX(final String name, final double lifeLength, final double monitoringPeriod,
            final double autoScalePeriod) throws Exception {
        super(name, lifeLength);
        this.monitoringPeriod = monitoringPeriod <= 0 ? -1 : Math.max(monitoringPeriod,
                CloudSim.getMinTimeBetweenEvents());
        this.autoScalePeriod = autoScalePeriod <= 0 ? -1 : Math.max(monitoringPeriod, autoScalePeriod);
    }

    /**
     * Adds a new autoscaling policy, which is executed upon each new VM
     * utilisation measurment.
     * 
     * @param policy
     *            - the new policy to add. Must not be null.
     */
    public void addAutoScalingPolicy(final IAutoscalingPolicy policy) {
        autoscalingPolicies.add(policy);
    }

    /**
     * Sets times, for which the utilisations of all VMs will be recorded. for
     * each of these times, the utilisations of all VMs will be recorded. Used
     * mostly for testing purposes.
     * 
     * @param utilRecordTimes
     *            - times for which the VM utilisations should be recorded. Used
     *            for testing purposes. The times must be positive.
     */
    public void recordUtilisation(final List<Double> utilRecordTimes) {
        for (final Double delay : utilRecordTimes) {
            if (isStarted()) {
                send(getId(), delay, BROKER_RECORD_UTIL_NOW, Boolean.FALSE);
            } else {
                presetEvent(getId(), BROKER_RECORD_UTIL_NOW, Boolean.FALSE, delay);
            }
        }
    }

    /**
     * Sets the period between VM utilisation measurements records. Used mostly
     * for testing purposes.
     * 
     * @param period
     *            - the period between subsequent VM utilisation measurements
     *            are recorded. Must be positive.
     */
    public void recordUtilisationPeriodically(final double period) {
        this.utilisationRecorddDelta = Math.max(period, CloudSim.getMinTimeBetweenEvents());
        if (isStarted()) {
            send(getId(), period, BROKER_RECORD_UTIL_NOW, Boolean.TRUE);
        } else {
            presetEvent(getId(), BROKER_RECORD_UTIL_NOW, Boolean.TRUE, period);
        }
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (!super.isStarted() && monitoringPeriod > 0) {
            send(getId(), offset, BROKER_MEASURE_UTIL_NOW);
        }
        if (!super.isStarted() && autoScalePeriod > 0) {
            send(getId(), offset, BROKER_AUTOSCALE_NOW);
        }
        super.processEvent(ev);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
        case BROKER_MEASURE_UTIL_NOW:
            if (CloudSim.clock() <= getLifeLength()) {
                measureUtil();
                send(getId(), monitoringPeriod, BROKER_MEASURE_UTIL_NOW);
            }
            break;
        case BROKER_AUTOSCALE_NOW:
            if (CloudSim.clock() <= getLifeLength()) {
                autoscale();
                send(getId(), autoScalePeriod, BROKER_AUTOSCALE_NOW);
            }
            break;
        case BROKER_RECORD_UTIL_NOW:
            if (CloudSim.clock() <= getLifeLength()) {
                recordUtil();
                if (utilisationRecorddDelta > 0 && (ev.getData() instanceof Boolean) && ((Boolean) ev.getData())) {
                    send(getId(), utilisationRecorddDelta, BROKER_RECORD_UTIL_NOW, Boolean.TRUE);
                }
            }
            break;
        default:
            super.processOtherEvent(ev);
            break;
        }
    }

    private void autoscale() {
        for (IAutoscalingPolicy policy : autoscalingPolicies) {
            policy.scale(this);
        }
    }

    private void recordUtil() {
        double currTime = CloudSim.clock();
        Map<Integer, double[]> vmsUtil = new LinkedHashMap<>();
        for (Vm vm : getVmList()) {
            if (vm instanceof MonitoredVMex) {
                vmsUtil.put(vm.getId(), ((MonitoredVMex) vm).getAveragedUtil());
            }
        }
        recordedUtilisations.put(currTime, vmsUtil);
    }

    /**
     * Returns the recorded utilisations. The resulting map's entries are in the
     * format [time, Map[vm-id, Array[cpu-util, ram-util, io-util]]] .
     * 
     * @return the recorded utilisations. The resulting map's entries are in the
     *         format [time, Map[vm-id, Array[cpu-util, ram-util, io-util]]] .
     */
    public LinkedHashMap<Double, Map<Integer, double[]>> getRecordedUtilisations() {
        return recordedUtilisations;
    }

    protected void measureUtil() {
        for (Vm vm : getVmList()) {
            if (vm instanceof MonitoredVMex) {
                updateUtil(((MonitoredVMex) vm));
            }
        }
    }

    protected void updateUtil(final MonitoredVMex vm) {
        if (monitoringPeriod <= 0 || vm.getCloudletScheduler().getCloudletExecList().isEmpty()) {
            vm.updatePerformance(0, 0, 0);
        } else {
            double sumCPUCloudLets = 0;
            double sumIOCloudLets = 0;
            double sumRAMCloudLets = 0;

            double vmMips = vm.getMips() * vm.getNumberOfPes();
            double vmIOMips = 0;
            double vmRam = vm.getRam();
            for (ResCloudlet cloudlet : vm.getCloudletScheduler().getCloudletExecList()) {
                sumCPUCloudLets += cloudlet.getRemainingCloudletLength();
                if (vm instanceof HddVm) {
                    if (cloudlet instanceof HddResCloudlet) {
                        sumIOCloudLets += ((HddResCloudlet) cloudlet).getRemainingCloudletIOLength();
                        sumRAMCloudLets += ((HddCloudlet) cloudlet.getCloudlet()).getRam();
                    }
                    vmIOMips = ((HddVm) vm).getIoMips();
                }
            }

            double expectedWorkloadCPUDuration = (sumCPUCloudLets / vmMips);
            double expectedWorkloadIODuration = vmIOMips == 0 ? 0 : (sumIOCloudLets / vmIOMips);

            vm.updatePerformance(Math.min(1, expectedWorkloadCPUDuration / monitoringPeriod),
                    Math.min(1, vmRam == 0 ? 0 : sumRAMCloudLets / vmRam),
                    Math.min(1, expectedWorkloadIODuration / monitoringPeriod));
        }
    }
}
