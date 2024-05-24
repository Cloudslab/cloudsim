package org.cloudbus.cloudsim.web.workload.brokers;

import org.cloudbus.cloudsim.geolocation.IGeolocationService;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.web.ILoadBalancer;
import org.cloudbus.cloudsim.web.WebSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Models the Route53 service.
 * 
 * @author nikolay.grozev
 * 
 */
public class Route53EntryPoint extends BaseEntryPoint implements IEntryPoint {

    // $0.750 per million queries - first 1 Billion queries / month
    // $0.375 per million queries - over 1 Billion queries / month

    public Route53EntryPoint(IGeolocationService geoService, long appId) {
        super(geoService, appId);
    }

    @Override
    public void dispatchSessions(List<WebSession> webSessions) {
        super.dispatchSessions(webSessions);

        // A table of assignments of web sessions to brokers/clouds.
        Map<WebBroker, List<WebSession>> assignments = new HashMap<>();
        for (WebBroker broker : getBrokers()) {
            assignments.put(broker, new ArrayList<>());
        }

        for (WebSession sess : webSessions) {
            WebBroker selectedBroker = null;
            double bestLatency = Double.MAX_VALUE;

            for (WebBroker broker : getBrokers()) {
                ILoadBalancer lb = broker.getLoadBalancers().get(getAppId());
                double latency = getGeoService().latency(sess.getSourceIP(), lb.getIp());
                if (latency < bestLatency) {
                    selectedBroker = broker;
                    bestLatency = latency;
                }
            }

            if (selectedBroker == null) {
                CustomLog.printConcat("[Route53] Session ", sess.getSessionId(), " has been denied service.");
                getCanceledSessions().add(sess);
            } else {
                assignments.get(selectedBroker).add(sess);
                sess.setServerIP(selectedBroker.getLoadBalancers().get(getAppId()).getIp());
            }
        }

        // Submit the sessions to the selected brokers/clouds
        for (Map.Entry<WebBroker, List<WebSession>> entry : assignments.entrySet()) {
            WebBroker broker = entry.getKey();
            List<WebSession> sessions = entry.getValue();
            for (WebSession sess : sessions) {
                CustomLog.printf("[Route53] Session %d will be assigned to %s", sess.getSessionId(), broker.toString());
            }
            broker.submitSessionsDirectly(sessions, getAppId());
        }
    }
}
