package org.cloudbus.cloudsim.web.workload.freq;

/**
 * A frequency function that defines periodic behaviour - for example the
 * workload repeats everyday/week etc. During each period, the frequency values
 * are defined as values of a statistical/stochastic distribution.
 * 
 * <br>
 * <br>
 * 
 * The main properties that define the behaviour of the function are:
 * <ul>
 * <li>periodLength - the length of a period.</li>
 * <li>nullPoint - a point which defines a starting point of an interval. The
 * considered intervals/periods are in the form [nullPoint+i*periodLength,
 * nullPoint+(i+1)*periodLength], where i is an integer.</li>
 * <li>valuedSet - value generator for the values within the interval
 * [0,periodLength].</li>
 * </ul>
 * 
 * @author nikolay.grozev
 * 
 */
public class PeriodicStochasticFrequencyFunction extends FrequencyFunction {

    private final double periodLength;
    private final double nullPoint;
    private final CompositeValuedSet valuedSet;

    public PeriodicStochasticFrequencyFunction(double unit, double periodLength, double nullPoint,
            CompositeValuedSet valuedSet) {
        super(unit);
        this.periodLength = periodLength;
        this.nullPoint = nullPoint;
        this.valuedSet = valuedSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.incubator.web.workload.freq.FrequencyFunction#
     * getFrequency(double)
     */
    @Override
    public int getFrequency(double time) {
        double periodOffset = 0;
        if (time >= nullPoint) {
            periodOffset = time - nullPoint;
            int timesPeriod = (int) (periodOffset / periodLength);
            periodOffset -= timesPeriod * periodLength;
        } else {
            periodOffset = nullPoint - time;
            int timesPeriod = (int) (periodOffset / periodLength);
            periodOffset -= timesPeriod * periodLength;
            periodOffset = periodLength - periodOffset;
        }
        return Math.max(0, (int) valuedSet.getValue(periodOffset));
    }
}
