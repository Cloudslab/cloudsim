package org.cloudbus.cloudsim.web.workload;

import org.cloudbus.cloudsim.web.WebSession;
import org.cloudbus.cloudsim.web.workload.sessions.ISessionGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates sessions over a specified period - this may include subsequent
 * steps in the simulation.
 * 
 * @author nikolay.grozev
 * 
 */
public class PeriodTimeGenerator implements IWorkloadGenerator {

    private final double period;
    private Integer count;
    private double leftOver = 0;
    private final ISessionGenerator sessGen;

    /**
     * Constr.
     * 
     * @param period
     *            - the period to generate sessions.
     * @param count
     *            - the count of sessions to generate or null ir endless.
     * @param sessGen
     *            - the generator of the sessions.
     */
    public PeriodTimeGenerator(final double period, final Integer count, final ISessionGenerator sessGen) {
        super();
        this.period = period;
        this.count = count;
        this.sessGen = sessGen;
    }

    @Override
    public Map<Double, List<WebSession>> generateSessions(double startTime, double periodLen) {
        Map<Double, List<WebSession>> result = new HashMap<>();
        if (count != null && count > 0) {
            double time = 0;
            for (int i = 0; i * period - leftOver < periodLen && count > 0; i++) {
                count--;
                time = startTime + i * period - leftOver;
                result.put(time, List.of(sessGen.generateSessionAt(time)));
            }
            leftOver = periodLen + startTime - time;
        }

        return result;
    }

}
