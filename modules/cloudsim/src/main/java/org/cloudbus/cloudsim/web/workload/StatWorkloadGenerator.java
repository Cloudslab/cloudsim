package org.cloudbus.cloudsim.web.workload;

import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.web.WebSession;
import org.cloudbus.cloudsim.web.workload.freq.FrequencyFunction;
import org.cloudbus.cloudsim.web.workload.sessions.ISessionGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.PoissonGenerator;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

import java.util.*;

/**
 * Represents a timed factory for sessions of a given type. Defines the workload
 * consisting of sessions of a given type directed to a data center.
 * Mathematically can be represented as Po(f(t)).
 * 
 * @author nikolay.grozev
 * 
 */
public class StatWorkloadGenerator implements IWorkloadGenerator {

    private final FrequencyFunction freqFun;
    private final ISessionGenerator sessGen;
    private final Random rng;

    /**
     * Constructor.
     * 
     * @param seed
     *            - the seed for the used Poisson distribution or null if no
     *            seed is used. Must not be null.
     * @param freqFun
     *            - the frequency function for the Poisson distribution. Must
     *            not be null.
     * @param sessGen
     *            - generator for sessions.
     */
    public StatWorkloadGenerator(final byte[] seed, final FrequencyFunction freqFun, final ISessionGenerator sessGen) {
        this(null, seed, freqFun, sessGen);
    }

    /**
     * Constructor.
     * 
     * @param seedGen
     *            - the seed generator for the used Poisson distribution or null
     *            the default seed genertor seed is used. Must not be null.
     * @param freqFun
     *            - the frequency function for the Poisson distribution. Must
     *            not be null.
     * @param sessGen
     *            - generator for sessions.
     */
    public StatWorkloadGenerator(SeedGenerator seedGen, final FrequencyFunction freqFun, final ISessionGenerator sessGen) {
        this(seedGen, null, freqFun, sessGen);
    }

    /**
     * Constructor.
     * 
     * @param freqFun
     *            - the frequency function for the Poisson distribution.
     * @param sessGen
     *            - generator for sessions.
     */
    public StatWorkloadGenerator(final FrequencyFunction freqFun, final ISessionGenerator sessGen) {
        this(null, null, freqFun, sessGen);
    }

    private StatWorkloadGenerator(SeedGenerator seedGen, final byte[] seed, final FrequencyFunction freqFun,
            final ISessionGenerator sessGen) {
        super();
        this.freqFun = freqFun;
        this.sessGen = sessGen;

        Random newRNG = null;
        if (seed == null) {
            try {
                newRNG = seedGen == null ? new MersenneTwisterRNG() : new MersenneTwisterRNG(seedGen);
            } catch (SeedException e) {
                newRNG = new MersenneTwisterRNG();
            }

        } else {
            newRNG = new MersenneTwisterRNG(seed);
        }
        rng = newRNG;
    }

    /**
     * Generates sessions for the period [startTime, startTime + periodLen].
     * 
     * @param startTime
     *            - the start time of the generated sessions.
     * @param periodLen
     *            - the length of the period.
     * @return a map between session start times and sessions.
     */
    @Override
    public Map<Double, List<WebSession>> generateSessions(final double startTime, final double periodLen) {
        double unit = freqFun.getUnit();
        double freq = freqFun.getFrequency(startTime);

        Map<Double, List<WebSession>> timesToSessions = new LinkedHashMap<>();
        if (freq > 0) {
            // The frequency within this period
            double freqInLen = freq * (periodLen / unit);
            NumberGenerator<Integer> poiss = new PoissonGenerator(freqInLen, rng);
            int numberOfSessions = poiss.nextValue();
            CustomLog.printf("Generate Session at time %s with number %d ",startTime, numberOfSessions);

            // Distribute uniformly the created sessions
            double timeStep = periodLen / numberOfSessions;
            for (int i = 0; i < numberOfSessions; i++) {
                double sessionTime = startTime + i * timeStep;
                if (!timesToSessions.containsKey(sessionTime)) {
                    timesToSessions.put(sessionTime, new ArrayList<>());
                }

                timesToSessions.get(sessionTime).add(sessGen.generateSessionAt(sessionTime));
            }
        }

        return timesToSessions;
    }

}
