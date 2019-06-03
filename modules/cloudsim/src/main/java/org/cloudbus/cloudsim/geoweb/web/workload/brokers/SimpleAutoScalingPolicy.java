package org.cloudbus.cloudsim.geoweb.web.workload.brokers;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.plus.IAutoscalingPolicy;
import org.cloudbus.cloudsim.plus.MonitoringBorkerEX;
import org.cloudbus.cloudsim.plus.disk.HddCloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.plus.disk.HddVm;
import org.cloudbus.cloudsim.plus.util.CustomLog;
import org.cloudbus.cloudsim.plus.vm.VMStatus;
import org.cloudbus.cloudsim.geoweb.web.ILoadBalancer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * 
 * @author nikolay.grozev
 * 
 */
public class SimpleAutoScalingPolicy implements IAutoscalingPolicy {

    private final double scaleUpCPUTrigger;
    private final double scaleDownCPUTrigger;
    private final double coolDownPeriod;

    private long appId;
    private StringBuilder debugSB = new StringBuilder();

    private double lastActionTime = -1;

    public SimpleAutoScalingPolicy(long appId, double scaleUpCPUTrigger, double scaleDownCPUTrigger,
            double coolDownPeriod) {
        super();
        if (scaleUpCPUTrigger < scaleDownCPUTrigger) {
            throw new IllegalArgumentException("Scale-up ratio should be greater than scale-down. Provided values: "
                    + scaleUpCPUTrigger + "; " + scaleDownCPUTrigger);
        }

        this.scaleUpCPUTrigger = scaleUpCPUTrigger;
        this.scaleDownCPUTrigger = scaleDownCPUTrigger;
        this.coolDownPeriod = coolDownPeriod;
        this.appId = appId;
    }

    @Override
    public void scale(MonitoringBorkerEX broker) {
        double currentTime = CloudSim.clock();
        boolean performScaling = lastActionTime < 0 || lastActionTime + coolDownPeriod < currentTime;

        if (performScaling && broker instanceof WebBroker) {
            WebBroker webBroker = (WebBroker) broker;
            debugSB.setLength(0);

            ILoadBalancer loadBalancer = webBroker.getLoadBalancers().get(appId);

            double avgCPU = 0;
            int count = 0;
            HddVm candidateToStop = null;
            for (HddVm vm : loadBalancer.getAppServers()) {
                if (!EnumSet.of(VMStatus.INITIALISING, VMStatus.RUNNING).contains(vm.getStatus())) {
                    continue;
                }
                avgCPU += vm.getCPUUtil();
                count++;
                candidateToStop = vm;
                debugSB.append(vm);
                debugSB.append("[" + vm.getStatus().name() + "] ");
                debugSB.append(String.format("cpu(%.2f) ram(%.2f) cdlts(%d);\t", vm.getCPUUtil(), vm.getRAMUtil(), vm
                        .getCloudletScheduler().getCloudletExecList().size()));
            }
            avgCPU = count == 0 ? 0 : avgCPU / count;

            CustomLog.printf("Simple-Autoscale(%s) avg-cpu(%.2f): %s", broker, avgCPU, debugSB);

            if (avgCPU > scaleUpCPUTrigger) {
                HddVm newASServer = loadBalancer.getAppServers().get(0).clone(new HddCloudletSchedulerTimeShared());
                loadBalancer.registerAppServer(newASServer);
                webBroker.createVmsAfter(Arrays.asList(newASServer), 0);
                lastActionTime = currentTime;

                CustomLog.printf("Simple-Autoscale(%s) Scale-Up: New AS VMs provisioned: %s", webBroker.toString(),
                        newASServer);
            } else if (avgCPU < scaleDownCPUTrigger && count > 1) {
                List<HddVm> toStop = Arrays.asList(candidateToStop);
                webBroker.destroyVMsAfter(toStop, 0);
                loadBalancer.getAppServers().removeAll(toStop);
                lastActionTime = currentTime;

                CustomLog
                        .printf("Simple-Autoscale(%s) Scale-Down: AS VMs terminated: %s, sessions to be killed:",
                                webBroker.toString(), toStop.toString(),
                                webBroker.getSessionsInServer(candidateToStop.getId()));
            }
        }
    }

}
