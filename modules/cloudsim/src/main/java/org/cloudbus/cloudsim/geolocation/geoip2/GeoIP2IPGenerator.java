package org.cloudbus.cloudsim.geolocation.geoip2;

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;
import org.cloudbus.cloudsim.geolocation.BaseIPGenerator;
import org.cloudbus.cloudsim.geolocation.IPGenerator;
import org.cloudbus.cloudsim.geolocation.IPUtil;
import org.cloudbus.cloudsim.EX.util.CustomLog;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * An IP generator that generates IPv4 IPs, based on the IP ranges specified in
 * the CSV format provided by <a href="http://www.maxmind.com">GeoLite2</a>.
 * 
 * @author nikolay.grozev
 * 
 */
public class GeoIP2IPGenerator extends BaseIPGenerator implements IPGenerator {

    // @TODO Extract these CSV constants elsewhere as they can be reused ...
    /** The separator in the csv file. */
    private static final char CSV_SEP = ',';
    /** The quote symbol in the csv and tsv file. */
    private static final char QUOTE_SYMBOL = '\"';

    /** All ranges specified in the file. */
    private final List<IPRange> ranges = new ArrayList<>();
    /**
     * A list with the same size as {@link GeoIP2IPGenerator.IPRange}. Each i-th
     * element in this list contains the sums of the lengths of the ranges in
     * ranges[0:i].
     */
    private final List<Long> accumRangeLengths = new ArrayList<>();
    /** A sum of the lengths of all ranges in the CSV file. */
    private long sumOfRangesLengths = 0;

    /**
     * Constr. Uses the default embedded data file.
     * 
     * @param countryCodes
     *            - the country codes for this generator.
     */
    public GeoIP2IPGenerator(final Set<String> countryCodes) {
        this(countryCodes, ResourceUtil.classLoad(ResourceUtil.DEFAULT_GEO_IP_COUNTRY_CSV));
    }
    
    /**
     * Constr. Uses the default embedded data file.
     * 
     * @param countryCodes
     *            - the country codes for this generator.
     * @param seed
     *            - a seed if we need to get the same behavior again and again.
     */
    public GeoIP2IPGenerator(final Set<String> countryCodes, final long seed) {
        this(countryCodes, ResourceUtil.classLoad(ResourceUtil.DEFAULT_GEO_IP_COUNTRY_CSV), seed);
    }
    
    /**
     * Constr.
     * 
     * @param countryCodes
     *            - the country codes for this generator.
     * @param is
     *            - a valid CSV stream as defined by the GeoLite2 format.
     * @param seed
     *            - a seed if we need to get the same behavior again and again.
     */
    public GeoIP2IPGenerator(final Set<String> countryCodes, final InputStream is, final long seed) {
        super(countryCodes, seed);
        Preconditions.checkNotNull(countryCodes);
        Preconditions.checkArgument(!countryCodes.isEmpty());
        Preconditions.checkNotNull(is);
        
        parseStream(is);
    }

    /**
     * Constr.
     * 
     * @param countryCodes
     *            - the country codes for this generator. Must not be null or
     *            empty.
     * @param is
     *            - a valid CSV stream as defined by the GeoLite2 format. Must
     *            not be null.
     */
    public GeoIP2IPGenerator(final Set<String> countryCodes, final InputStream is) {
        super(countryCodes);
        Preconditions.checkNotNull(countryCodes);
        Preconditions.checkArgument(!countryCodes.isEmpty());
        Preconditions.checkNotNull(is);
        
        parseStream(is);
    }
    
    /**
     * Constr.
     * 
     * @param countryCodes
     *            - the country codes for this generator.
     * @param f
     *            - a valid CSV file as defined by the GeoLite2 format. Must not
     *            be null. Must be valid.
     * @param seed
     *            - a seed if we need to get the same behavior again and again.
     */
    public GeoIP2IPGenerator(final Set<String> countryCodes, final File f, final long seed) {
        super(countryCodes, seed);
        
        Preconditions.checkNotNull(countryCodes);
        Preconditions.checkArgument(!countryCodes.isEmpty());
        Preconditions.checkNotNull(f);
        Preconditions.checkArgument(f.exists());
        
        CustomLog.printf(Level.FINER, "Creating an IP generator from %s for countries %s",
                f.getAbsolutePath(), Arrays.toString(getCountryCodes().toArray()));
        
        parseStream(ResourceUtil.toStream(f));
    }

    /**
     * Constr.
     * 
     * @param countryCodes
     *            - the country codes for this generator.
     * @param f
     *            - a valid CSV file as defined by the GeoLite2 format. Must not
     *            be null. Must be valid.
     */
    public GeoIP2IPGenerator(final Set<String> countryCodes, final File f) {
        super(countryCodes);
        
        Preconditions.checkNotNull(countryCodes);
        Preconditions.checkArgument(!countryCodes.isEmpty());
        Preconditions.checkNotNull(f);
        Preconditions.checkArgument(f.exists());
        CustomLog.printf(Level.FINER, "Creating an IP generator from %s for countries %s",
                f.getAbsolutePath(), Arrays.toString(getCountryCodes().toArray()));
        
        parseStream(ResourceUtil.toStream(f));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.geoweb.geolocation.IPGenerator#pollRandomIP()
     */
    @Override
    public String pollRandomIP() {
        long serachAccum = (long) (getRandom().nextDouble() * sumOfRangesLengths);
        int idx = Collections.binarySearch(accumRangeLengths, serachAccum);
        idx = idx >= 0 ? idx : -idx - 1;
        idx = idx >= ranges.size() ? ranges.size() - 1 : idx;

        IPRange range = ranges.get(idx);

        int ip = range.from + getRandom().nextInt(range.to - range.from);
        return IPUtil.convertIPv4(ip);
    }

    private void parseStream(final InputStream input) {
        long accum = 0;
        ranges.clear();
        sumOfRangesLengths = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                CSVReader csv = new CSVReader(reader)) {
            // Read the file line by line
            int lineCount = 0;
            String[] lineElems = csv.readNext();
            while ((lineElems = csv.readNext()) != null) {
                String countryCode = lineElems[4];
                if (getCountryCodes().contains(countryCode)) {
                    int from = (int) Long.parseLong(lineElems[2]);
                    int to = (int) Long.parseLong(lineElems[3]);
                    accum += to - from;
                    ranges.add(new IPRange(from, to));
                    accumRangeLengths.add(accum);
                }
                if (++lineCount % 10000 == 0) {
                    CustomLog.printf(Level.FINER, "%d lines processed from", lineCount);
                }
            }
            CustomLog.printf(Level.FINER, "IP generator for countries %s has %d IP ranges",
                    Arrays.toString(getCountryCodes().toArray()), ranges.size());

            sumOfRangesLengths = accum;
        } catch (Exception e) {
            ranges.clear();
            CustomLog.logError(Level.SEVERE, "Parsing Error", e);
//            throw new IllegalArgumentException("Parsing Error", e);
        }
    }

    /**
     * Represents an IP range
     * 
     * @author nikolay.grozev
     * 
     */
    private static class IPRange {
        private final int from;
        private final int to;

        public IPRange(int from, int to) {
            super();
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return String.format("[%d,%d]", from, to);
        }
    }
}
