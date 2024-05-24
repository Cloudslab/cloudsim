package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.EX.disk.DataItem;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.uncommons.maths.number.NumberGenerator;

import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

/**
 * A base class containing the main functionality of a statistical generator of
 * CloutLets. This class generates CloudLets based on the provided statistical
 * distributions for their properties. Random generators are provided in a map,
 * whose keys usually are the constants of the class, as documented below.
 * 
 * @author nikolay.grozev
 * 
 * @param <T>
 *            - the actual type of the generated CloudLets.
 */
public abstract class BaseStatGenerator<T extends Cloudlet> implements IGenerator<T> {

    /** A key for the statistical generator of CPU length of the cloudlet. */
    public static final String CLOUDLET_LENGTH = "CLOUDLET_MIPS";
    /** A key for the statistical generator of RAM length of the cloudlet. */
    public static final String CLOUDLET_RAM = "CLOUDLET_RAM";
    /** A key for the statistical generator of IO length of the cloudlet. */
    public static final String CLOUDLET_IO = "CLOUDLET_IO";
    /**
     * A key for the statistical generator of boolean values, specifying if a
     * cloudlets modfies its data. Boolean values are represented as integers -
     * 0 is considered False, and other values are considered as True.
     * */
    public static final String CLOUDLET_MODIFIES_DATA = "CLOUDLET_MODIFIES_DATA";

    protected Map<String, ? extends NumberGenerator<? extends Number>> seqGenerators;
    private final LinkedList<Double> idealStartUpTimes = new LinkedList<>();
    private final DataItem data;
    private double startTime = -1;
    private double endTime = -1;
    private T peeked;

    /**
     * Creates a new generator with the specified statistical distributions.
     * 
     * @param seqGenerators
     *            - the statistical distributions to be used for the generation
     *            of CloudLets. See the standard keys provided above to see what
     *            is usually expected from this map. Inheriting classes may
     *            define their own key and values to be used in the factory
     *            method. Must not be null.
     * @param data
     *            - the data used by the generator, or null if no data is used.
     */
    public BaseStatGenerator(final Map<String, ? extends NumberGenerator<? extends Number>> seqGenerators,
            final DataItem data) {
        this(seqGenerators, -1, -1, data);
    }

    /**
     * Creates a new generator with the specified statistical distributions.
     * 
     * @param seqGenerators
     *            - the statistical distributions to be used for the generation
     *            of CloudLets. See the standard keys provided above to see what
     *            is usually expected from this map. Inheriting classes may
     *            define their own key and values to be used in the factory
     *            method. Must not be null.
     * @param startTime
     *            - the start time of the generation. If positive, no web
     *            cloudlets with ideal start time before this will be generated.
     * @param startTime
     *            - the end time of the generation. If positive, no web
     *            cloudlets with ideal start time after this will be generated.
     * @param data
     *            - the data used by the generator, or null if no data is used.
     */
    public BaseStatGenerator(final Map<String, ? extends NumberGenerator<? extends Number>> seqGenerators,
            final double startTime, final double endTime, final DataItem data) {
        this.seqGenerators = seqGenerators;
        this.startTime = startTime;
        this.endTime = endTime;
        this.data = data;

        if (data == null && seqGenerators.containsKey(CLOUDLET_IO)) {
            String errMsg = "IO opeartions should not be provided without data";
            CustomLog.print(Level.SEVERE, errMsg);
            throw new IllegalArgumentException(errMsg);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#peek()
     */
    @Override
    public T peek() {
        if (peeked == null && !idealStartUpTimes.isEmpty()) {
            peeked = create(idealStartUpTimes.poll());
        }
        return peeked;
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#poll()
     */
    @Override
    public T poll() {
        T result = peeked;
        if (peeked != null) {
            peeked = null;
        } else if (!idealStartUpTimes.isEmpty()) {
            result = create(idealStartUpTimes.poll());
        }
        return result;
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return peek() == null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see IGenerator#notifyOfTime(double)
     */
    @Override
    public void notifyOfTime(final double time) {
        if ((startTime < 0 || startTime <= time) && (endTime < 0 || endTime >= time)
                && (idealStartUpTimes.isEmpty() || idealStartUpTimes.getLast() < time)) {
            idealStartUpTimes.offer(time);
        }
    }

    /**
     * Returns the start time of this generator. If positive, no web cloudlets
     * with ideal start time before this will be generated.
     * 
     * @return the start time of the generator.
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of this generator. If positive, no web cloudlets
     * with ideal start time after this will be generated.
     * 
     * @return the end time of the generator.
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Returns the data used by the generated cloudlets, or null if the use no
     * data.
     * 
     * @return the data used by the generated cloudlets, or null if the use no
     *         data.
     */
    public DataItem getData() {
        return data;
    }

    /**
     * An abstract factory method for the creation of a new cloudlet. It uses
     * the provided in the constructor statistical distributions to create a new
     * cloudlet.
     * 
     * @param idealStartTime
     *            - the ideal start time of the new CloudLet.
     * @return a new CloudLet.
     */
    protected abstract T create(final double idealStartTime);

    /**
     * Generates a plausible value for the key.
     * 
     * @param key
     *            - the key. Tytpically one of the constants of the class.
     * @return a plausible (with the correspondent statistical properties) value
     *         for the key.
     */
    protected Double generateNumericValue(final String key) {
        Number genValue = !seqGenerators.containsKey(key) ? 0 : seqGenerators.get(key).nextValue();
        if (genValue == null) {
            return null;
        } else {
            return Math.max(0, genValue.doubleValue());
        }
    }

    /**
     * Generates a boolean value for the key.
     * 
     * @param key
     *            - the key. Tytpically one of the constants of the class.
     * @return a plausible (with the correspondent statistical properties) value
     *         for the key. 0 values are considered as False and other values
     *         are considered as True. If no value is present for the key -
     *         False is returned.
     */
    protected Boolean generateBooleanValue(final String key) {
        Number genValue = !seqGenerators.containsKey(key) ? 0 : seqGenerators.get(key).nextValue();
        if (genValue == null) {
            return null;
        } else {
            int result = Math.max(0, genValue.intValue());
            return result != 1;
        }
    }

}
