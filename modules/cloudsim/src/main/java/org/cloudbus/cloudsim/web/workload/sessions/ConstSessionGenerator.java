package org.cloudbus.cloudsim.web.workload.sessions;

import org.cloudbus.cloudsim.EX.disk.DataItem;
import org.cloudbus.cloudsim.web.*;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates equal sessions that consist of equal cloudlets and use only one
 * data item.
 * 
 * @author nikolay.grozev
 * 
 */
public class ConstSessionGenerator implements ISessionGenerator {
    private final long asCloudletLength;
    private final int asRam;

    private final long dbCloudletLength;
    private final int dbRam;
    private final long dbCloudletIOLength;

    private final double duration;
    private int numberOfCloudlets = 0;

    private final String[] metadata;

    private final DataItem data;
    private final boolean modifiesData;

    /**
     * Constructor.
     * 
     * @param asCloudletLength
     *            - the length of the generated app server cloudlets.
     * @param asRam
     *            - the ram of the generated app server cloudlets.
     * @param dbCloudletLength
     *            - the length of the generated db server cloudlets.
     * @param dbRam
     *            - the ram of the generated db server cloudlets.
     * @param dbCloudletIOLength
     *            - the IO ram of the generated app server cloudlets.
     * @param duration
     *            - the duration of the generated sessions.
     * @param numberOfCloudlets
     *            - number of cloudlets in each sessions. Negative means
     *            infinity.
     * @param data
     *            - the data used by the database cloudlets.
     * @param metadata
     *            - the metadata of the generated session.
     */
    public ConstSessionGenerator(final long asCloudletLength, final int asRam, final long dbCloudletLength,
            final int dbRam, final long dbCloudletIOLength, final double duration, final int numberOfCloudlets,
            final boolean modifiesData, final DataItem data, final String... metadata) {
        super();
        this.asCloudletLength = asCloudletLength;
        this.asRam = asRam;
        this.dbCloudletLength = dbCloudletLength;
        this.dbRam = dbRam;
        this.dbCloudletIOLength = dbCloudletIOLength;
        this.duration = duration;
        this.numberOfCloudlets = numberOfCloudlets;
        this.modifiesData = modifiesData;
        this.data = data;
        this.metadata = metadata;
    }

    /**
     * Constructor.
     * 
     * @param asCloudletLength
     *            - the length of the generated app server cloudlets.
     * @param asRam
     *            - the ram of the generated app server cloudlets.
     * @param dbCloudletLength
     *            - the length of the generated db server cloudlets.
     * @param dbRam
     *            - the ram of the generated db server cloudlets.
     * @param dbCloudletIOLength
     *            - the IO ram of the generated app server cloudlets.
     * @param data
     *            - the data used by the database cloudlets.
     * @param metadata
     *            - the metadata of the generated session.
     */
    public ConstSessionGenerator(final long asCloudletLength, final int asRam, final long dbCloudletLength,
            final int dbRam, final long dbCloudletIOLength, final boolean modifiesData, final DataItem data,
            final String... metadata) {
        this(asCloudletLength, asRam, dbCloudletLength, dbRam, dbCloudletIOLength, -1, -1, modifiesData, data, metadata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.incubator.web.workload.sessions.ISessionGenerator
     * #generateSessionAt(double)
     */
    @Override
    public WebSession generateSessionAt(final double time) {
        double startTime = duration > 0 ? time : -1;
        double endTime = duration > 0 ? time + duration : -1;

        Map<String, NumberGenerator<Double>> asGenerators = new HashMap<>();
        asGenerators.put(StatGenerator.CLOUDLET_LENGTH, new ConstantGenerator<>((double) asCloudletLength));
        asGenerators.put(StatGenerator.CLOUDLET_RAM, new ConstantGenerator<>((double) asRam));
        IGenerator<WebCloudlet> appServerCloudLets = new StatGenerator(asGenerators, startTime, endTime, null);

        Map<String, NumberGenerator<? extends Number>> dbGenerators = new HashMap<>();
        dbGenerators.put(StatGenerator.CLOUDLET_LENGTH, new ConstantGenerator<>((double) dbCloudletLength));
        dbGenerators.put(StatGenerator.CLOUDLET_RAM, new ConstantGenerator<>((double) dbRam));
        dbGenerators.put(StatGenerator.CLOUDLET_IO, new ConstantGenerator<>((double) dbCloudletIOLength));
        dbGenerators.put(StatGenerator.CLOUDLET_MODIFIES_DATA, new ConstantGenerator<>(modifiesData ? 1 : 0));

        CompositeGenerator<? extends WebCloudlet> dbServerCloudLets = new CompositeGenerator<>(new StatGenerator(
                dbGenerators, startTime, endTime, data));

        return new WebSession(appServerCloudLets, dbServerCloudLets, -1, numberOfCloudlets, time + duration, metadata);
    }

}
