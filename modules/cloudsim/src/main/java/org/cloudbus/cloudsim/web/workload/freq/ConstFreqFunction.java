package org.cloudbus.cloudsim.web.workload.freq;

/**
 * A function that returns a constant for every time instance.
 * 
 * @author nikolay.grozev
 * 
 */
public class ConstFreqFunction extends FrequencyFunction {

    private final int value;

    /**
     * Constructor.
     * 
     * @param unit
     *            - the time unit for which a frequency is specified.
     * @param value
     *            - the frequency for that time unit.
     */
    public ConstFreqFunction(double unit, int value) {
        super(unit);
        this.value = value;
    }

    @Override
    public int getFrequency(double time) {
        return value;
    }

}
