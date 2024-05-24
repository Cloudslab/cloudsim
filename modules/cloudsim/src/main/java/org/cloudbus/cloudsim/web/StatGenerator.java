package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.DataItem;
import org.uncommons.maths.number.NumberGenerator;

import java.util.Map;

/**
 * A statistical generator for Server cloudlets.
 * 
 * @author nikolay.grozev
 * 
 */
public class StatGenerator extends BaseStatGenerator<WebCloudlet> {

    /**
     * Creates a new instance.
     * 
     * @param randomGenerators
     *            - the statistical random number generators as explained in the
     *            javadoc of the super class.
     * @param data
     *            - the data used by the generator, or null if no data is used.
     */
    public StatGenerator(final Map<String, ? extends NumberGenerator<? extends Number>> randomGenerators,
            final DataItem data) {
        super(randomGenerators, data);
    }

    /**
     * Creates a new instance.
     * 
     * @param randomGenerators
     *            - the statistical random number generators as explained in the
     *            javadoc of the super class.
     * @param startTime
     *            - see the javadoc of the super class.
     * @param startTime
     *            - see the javadoc of the super class.
     * @param data
     *            - the data used by the generator, or null if no data is used.
     */
    public StatGenerator(final Map<String, ? extends NumberGenerator<? extends Number>> seqGenerators,
            final double startTime, final double endTime, final DataItem data) {
        super(seqGenerators, startTime, endTime, data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.incubator.web.BaseStatGenerator#create(double)
     */
    @Override
    protected WebCloudlet create(final double idealStartTime) {
        Double cpuLen = generateNumericValue(CLOUDLET_LENGTH);
        Double ram = generateNumericValue(CLOUDLET_RAM);
        Double ioLen = generateNumericValue(CLOUDLET_IO);
        Boolean modifiesData = generateBooleanValue(CLOUDLET_MODIFIES_DATA);

        if (cpuLen == null || ram == null || ioLen == null || modifiesData == null) {
            return null;
        } else {
            return new WebCloudlet(idealStartTime, cpuLen.longValue(), ioLen.longValue(), ram * 10, -1, modifiesData,
                    getData());
        }
    }

}
