package org.cloudbus.cloudsim.web.workload.freq;

/**
 * We are representing the generated workload as a Poisson distribution over a
 * function of time - Po(f(t)). This class represents the f(t) concept from this
 * definition. It has one main property (the unit) and an abstract method for
 * the frequency.
 * 
 * <br>
 * <br>
 * 
 * For example if we would like to denote that at the 100th sec after the start
 * of the simulation an event occurs with frequency 10 per minute (60sec), then
 * the following calls should return:
 * <ul>
 * <li>getUnit() - 60</li>
 * <li>getFrequency(100) - 10</li>
 * </ul>
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class FrequencyFunction {

    private final double unit;

    /**
     * Constructor.
     * 
     * @param unit
     *            - the unit of the frequency function.
     */
    public FrequencyFunction(double unit) {
        super();
        this.unit = unit;
    }

    /**
     * Returns the unit of the frequency function.
     * 
     * @return - the unit of the frequency function.
     */
    public double getUnit() {
        return unit;
    }

    /**
     * Returns the frequency for the specified moment oafter the start of the
     * simulation.
     * 
     * @param time
     *            - the time after the start of the simulation we are interested
     *            in. Must be valid simulation time.
     * @return - the frequency.
     */
    public abstract int getFrequency(final double time);

}
