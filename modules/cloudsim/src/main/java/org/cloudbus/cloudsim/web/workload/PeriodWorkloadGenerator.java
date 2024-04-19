package org.cloudbus.cloudsim.web.workload;

import org.cloudbus.cloudsim.web.WebSession;
import org.cloudbus.cloudsim.web.workload.sessions.ISessionGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author nikolay.grozev
 * 
 */
public class PeriodWorkloadGenerator implements IWorkloadGenerator {

    private int sessionsNumber;
    private final double period;
    private final ISessionGenerator sessGen;

    public PeriodWorkloadGenerator(final ISessionGenerator sessGen, final double period, final int sessionsNumber) {
        super();
        this.sessGen = sessGen;
        this.period = period;
        this.sessionsNumber = sessionsNumber;
    }

    @Override
    public Map<Double, List<WebSession>> generateSessions(final double startTime, final double periodLen) {
        Map<Double, List<WebSession>> result = new HashMap<>();
        for (int i = 0; i < periodLen / period && sessionsNumber > 0; i++) {
            sessionsNumber--;
            double startAt = startTime + i * period;
            result.put(startAt, List.of(sessGen.generateSessionAt(startAt)));
        }
        return result;
    }

}
