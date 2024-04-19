package org.cloudbus.cloudsim.web.workload;

import org.cloudbus.cloudsim.geolocation.IGeolocationService;
import org.cloudbus.cloudsim.geolocation.IPGenerator;
import org.cloudbus.cloudsim.web.WebSession;

import java.util.List;
import java.util.Map;

/**
 * A workload generator, which sets the IPs of the generated sessions in
 * accordance with a specified {@link IPGenerator}. It wraps another instance of
 * {@link IWorkloadGenerator}, which creates the sessions, and only adds the
 * IPs.
 * 
 * <br>
 * <br>
 * 
 * This class is useful when you have a workload generator that does not create
 * IPs (the wrapped generator) and you want to add random IPs to the generated
 * sessions.
 * 
 * @author nikolay.grozev
 * 
 */
public class RandomIPWorkloadGenerator implements IWorkloadGenerator {

    private final IWorkloadGenerator wrappedGenerator;
    private final IPGenerator ipGen;
    private final IGeolocationService geoService;

    private static final int ATTEMPTS = 10;

    /**
     * Constr.
     * 
     * @param wrappedGenerator
     *            - the wrapped generator to use. Must not be null.
     * @param ipGen
     *            - the IP generator to use when assigning IPs to the newly
     *            created session. Must not be null.
     * @param geoService
     *            - a geolocation service. Must not be null.
     */
    public RandomIPWorkloadGenerator(final IWorkloadGenerator wrappedGenerator, final IPGenerator ipGen,
                                     final IGeolocationService geoService) {
        super();
        this.wrappedGenerator = wrappedGenerator;
        this.ipGen = ipGen;
        this.geoService = geoService;
    }

    @Override
    public Map<Double, List<WebSession>> generateSessions(double startTime, double periodLen) {
        // Call the wrapped workload generator
        Map<Double, List<WebSession>> result = wrappedGenerator.generateSessions(startTime, periodLen);

        // Generate and set random IPs to all web sessions.
        for (Map.Entry<Double, List<WebSession>> e : result.entrySet()) {
            for (WebSession sess : e.getValue()) {
                sess.setSourceIP(ipGen.pollRandomIP(geoService, ATTEMPTS));
            }
        }

        return result;
    }

}
