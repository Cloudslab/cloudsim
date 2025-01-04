package org.cloudbus.cloudsim.geolocation.geoip2;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MinMaxPriorityQueue;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cloudbus.cloudsim.geolocation.BaseGeolocationService;
import org.cloudbus.cloudsim.geolocation.IGeolocationService;
import org.cloudbus.cloudsim.geolocation.IPMetadata;
import org.cloudbus.cloudsim.EX.util.CustomLog;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Implements an offline {@link IGeolocationService} from the data downloaded
 * from <a href="http://www.maxmind.com">GeoLite2</a> and <a
 * href="http://www-iepm.slac.stanford.edu/pinger/">PingER</a>.
 * 
 * <br>
 * <br>
 * Sample PingER data can be extracted with the following request: <a href=
 * "http://www-wanmon.slac.stanford.edu/cgi-wrap/pingtable.pl?format=tsv&file=average_rtt&by=by-node&size=1000&tick=monthly&year=2014&month=09&from=WORLD&to=WORLD&ex=none&dataset=hep&percentage=75%25&filter=on"
 * >PingER request URL</a>.
 * 
 * 
 * @author nikolay.grozev
 * 
 */
public class GeoIP2PingERService extends BaseGeolocationService implements IGeolocationService, Closeable {


    /** In order to minimise the number of created instances, we keep a cache. */
    private final Cache<String, double[]> coordinatesCache = CacheBuilder.newBuilder().concurrencyLevel(1)
            .initialCapacity(INITIAL_CACHE_SIZE).maximumSize(CACHE_SIZE).build();

    /** In order to minimise the number of created instances, we keep a cache. */
    private final Cache<String, Double> ipDistanceCache = CacheBuilder.newBuilder().concurrencyLevel(1)
            .initialCapacity(INITIAL_CACHE_SIZE).maximumSize(CACHE_SIZE).build();

    // @TODO Extract these TSV/CSV constants elsewhere as they can be reused ...
    /** The separator in the tsv file. */
    private static final char TSV_SEP = '\t';
    /** The separator in the csv file. */
    private static final char CSV_SEP = ',';
    /** The quote symbol in the csv and tsv file. */
    private static final char QUOTE_SYMBOL = '\"';
    /**
     * A latency threshold, below which the latency is considered to be 0, which
     * is unacceptably low.
     */
    private static final double LATENCY_EPSILON = 0.001;
    /** Number of approximations to use when estimating a latency. */
    private static final int NUM_APPROX_FOR_LATENCY_ESTIMATION = 3;
    /** A pattern for a string representing a decimal double number. */
    private static final String DOUBLE_GROUP_PATTERN = "(\\-?\\d+(\\.\\d+)?)";
    /** A regular expression for strings of the format (latitude longitude). */
    private static final Pattern COORD_PATTERN = Pattern.compile("\\(\\s*" + DOUBLE_GROUP_PATTERN + "\\s+"
            + DOUBLE_GROUP_PATTERN + "\\s*\\)");

    private final DatabaseReader reader;

    private final Map<String, double[]> nodesTable = new HashMap<>();
    private final Map<Pair<String, String>, Double> latencyTable = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param geoIP2DB
     *            - a stream to a valid mmdb data. Must not be null
     * @param pingErRTT
     *            - a stream to a TSV file extracted from the PingER service, containing
     *            data about RTTs between hosts distributed worldwide. Must not be null
     * @param pingerMonitoringSites
     *            - a stream to a CSV file extracted from the PingER service, containing the
     *            metadata of all hosts. Must not be null
     */
    public GeoIP2PingERService(final InputStream geoIP2DB, final InputStream pingErRTT, final InputStream pingerMonitoringSites) {
        Preconditions.checkNotNull(geoIP2DB);
        Preconditions.checkNotNull(pingErRTT);
        Preconditions.checkNotNull(pingerMonitoringSites);
        
        CustomLog.printf(Level.FINER, "Creating a GeoLocation service from streams");
        try {
            reader = new DatabaseReader.Builder(geoIP2DB).build();

            parsePingER(pingErRTT, pingerMonitoringSites);
        } catch (IOException e) {
            String msg = "Invalid file: " + geoIP2DB + " Error details:" + e.getMessage();
            CustomLog.logError(Level.SEVERE, msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }
    
    /**
     * Constructor.
     * 
     * @param geoIP2DB
     *            - a valid file in the mmdb format.
     * @param pingErRTTFile
     *            - a TSV file extracted from the PingER service, containing
     *            data about RTTs between hosts distributed worldwide.
     * @param pingerMonitoringSitesFile
     *            - a CSV file extracted from the PingER service, containing the
     *            metadata of all hosts.
     */
    public GeoIP2PingERService(final File geoIP2DB, final File pingErRTTFile, final File pingerMonitoringSitesFile) {
        this(ResourceUtil.toStream(geoIP2DB), ResourceUtil.toStream(pingErRTTFile), ResourceUtil.toStream(pingerMonitoringSitesFile));
    }

    public GeoIP2PingERService() {
        this(ResourceUtil.classLoad(ResourceUtil.DEFAULT_GEO_LITE2_CITY_MMDB),
                ResourceUtil.classLoad(ResourceUtil.DEFAULT_PING_TABLE_PING_ER_TSV),
                ResourceUtil.classLoad(ResourceUtil.DEFAULT_MONITORING_SITES_PING_ER_CSV));
    }
    
    private void parsePingER(final InputStream pingErRTT, final InputStream pingerMonitoringSites) {
        try (BufferedReader pingsReader = new BufferedReader(new InputStreamReader(pingErRTT));
                BufferedReader nodeDefsReader = new BufferedReader(new InputStreamReader(pingerMonitoringSites))) {
            parseNodesDefitions(nodeDefsReader);
            parseInterNodePings(pingsReader);
        } catch (Exception e) {
            String msg = " A file could not be found or read properly. Message: " + e.getMessage();
            CustomLog.logError(Level.SEVERE, msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    private void parseInterNodePings(final BufferedReader pings) throws Exception {
        latencyTable.clear();
        Set<String> unknownNodes = new LinkedHashSet<>();
        try (CSVReader csv = new CSVReader(pings)) {
            // Skip header line
            String[] lineElems = csv.readNext();
            int lineCount = 0;
            while ((lineElems = csv.readNext()) != null) {
                List<Double> measurements = new ArrayList<>();
                String monitoringNode = null;
                String remoteNode = null;

                for (int i = 3; i < lineElems.length; i++) {
                    String element = lineElems[i].trim();
                    if (element.isEmpty() || element.matches(DOUBLE_GROUP_PATTERN)) {
                        double rtt = Double.parseDouble(element);
                        // We consider the latency to be half the RTT
                        measurements.add(rtt / 2);
                    } else {
                        monitoringNode = element;
                        remoteNode = lineElems[i + 3].trim();
                        break;
                    }
                }

                Double latency = averageLatency(measurements);
                if (!this.nodesTable.containsKey(monitoringNode)) {
                    unknownNodes.add(monitoringNode);
                } else if (!this.nodesTable.containsKey(remoteNode)) {
                    unknownNodes.add(remoteNode);
                } else if (latency != null) {
                    latencyTable.put(ImmutablePair.of(monitoringNode, remoteNode), latency);
                }

                if (++lineCount % 1000 == 0) {
                    CustomLog.printf(Level.FINER, "%d ping measurments definitions parsed", lineCount);
                }
            }
        }
        CustomLog.printf(Level.FINER, "Total %d ping measurments definitions parsed", latencyTable.size());
        CustomLog.print(Level.FINER, "The definitions of the following nodes are missing." + unknownNodes);
    }

    private void parseNodesDefitions(final BufferedReader defs) throws Exception {
        nodesTable.clear();
        try (CSVReader csv = new CSVReader(defs)) {
            // Skip header line
            String[] lineElems = csv.readNext();
            int lineCount = 0;
            while ((lineElems = csv.readNext()) != null) {
                String node = lineElems[0].trim();
                String location = lineElems[2].trim();

                Matcher matcher = COORD_PATTERN.matcher(location);
                if (matcher.find()) {
                    double lat = Double.parseDouble(matcher.group(1));
                    double lon = Double.parseDouble(matcher.group(3));
                    nodesTable.put(node, new double[] { lat, lon });
                } else {
                    nodesTable.clear();
                    throw new IllegalArgumentException("Could not extract the geo location from \"" + location + "\"");
                }
                if (++lineCount % 100 == 0) {
                    CustomLog.printf(Level.FINER, "%d node definitions parsed", lineCount);
                }
            }

            CustomLog.printf(Level.FINER, "Total %d node definitions parsed", nodesTable.size());
        }
    }

    private static Double averageLatency(final List<Double> measurements) {
        double sum = 0;
        int count = 0;
        for (Double d : measurements) {
            if (Math.abs(d - 0) > LATENCY_EPSILON) {
                sum += d;
                count++;
            }
        }

        return count != 0 ? sum / count : null;
    }

    @Override
    public final double[] getCoordinates(final String ip) {
        double[] result = coordinatesCache.getIfPresent(ip);
        if (result == null) { // If not in the cache
            Location location;
            try {
                location = reader.city(InetAddress.getByName(ip)).getLocation();
                result = new double[] { location.getLatitude(), location.getLongitude() };
            } catch (UnknownHostException e) {
                String msg = "Invalid IP: " + ip;
                CustomLog.logError(Level.SEVERE, msg, e);
                throw new IllegalArgumentException("Invalid IP", e);
            } catch (IOException e) {
                String msg = "Could not locate IP: " + ip + ", " + "because of I/O error:"
                        + e.getMessage();
                CustomLog.logError(Level.SEVERE, msg, e);
                throw new IllegalStateException(e);
            } catch (GeoIp2Exception e) {
                String msg = "Could not locate IP: " + ip + ", because " + e.getMessage();
                CustomLog.logError(Level.FINER, msg, e);
                result = new double[] { Double.NaN, Double.NaN };
            }
            coordinatesCache.put(ip, result);
        }
        return result;
    }

    @Override
    public IPMetadata getMetaData(final String ip) {
        CityResponse city;
        try {
            city = reader.city(InetAddress.getByName(ip));
            return new IPMetadata(city.getContinent().getName(), city.getContinent().getCode(), city.getCountry()
                    .getName(), city.getCountry().getIsoCode(), city.getCity().getName(), city.getPostal().getCode(),
                    city.getLocation().getLatitude(), city.getLocation().getLongitude());
        } catch (UnknownHostException e) {
            String msg = "Invalid IP: " + ip;
            CustomLog.logError(Level.INFO, msg, e);
            throw new IllegalArgumentException("Invalid IP", e);
        } catch (IOException | GeoIp2Exception e) {
            String msg = "Could not locate IP: " + ip + ", because: " + e.getMessage();
            CustomLog.logError(Level.FINER, msg, e);
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public final double latency(final String ip1, final String ip2) {
        String key = ip1 + ip2;
        Double cached = ipDistanceCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        // The coordinates of the requested IPs
        final double[] reqCoord1 = getCoordinates(ip1);
        final double[] reqCoord2 = getCoordinates(ip2);

        double result = latency(reqCoord1, reqCoord2);
        CustomLog.print(Level.FINEST, String.format("Latency betweeen %s and %s is %.2f", ip1, ip2, result));

        ipDistanceCache.put(key, result);
        return result;
    }

    @Override
    public double latency(final double[] reqCoord1, final double[] reqCoord2) {
		// Set up the heap...
        @SuppressWarnings("rawtypes")
        MinMaxPriorityQueue.Builder builderRaw = MinMaxPriorityQueue.maximumSize(NUM_APPROX_FOR_LATENCY_ESTIMATION);
        @SuppressWarnings({ "unchecked" })
        MinMaxPriorityQueue.Builder<PingERLatencyEntry> builder = builderRaw;
        builder.expectedSize(NUM_APPROX_FOR_LATENCY_ESTIMATION);

        // We keep all latencies within a priority queue with a fixed size N. At
        // the end we compute the average of the best N elements, which are kept
        // in the queue.
        MinMaxPriorityQueue<PingERLatencyEntry> heap = builder.create();


        // Loop through the latencies and put them in the priority queue.
        for (Map.Entry<Pair<String, String>, Double> el : latencyTable.entrySet()) {
            // The coordinates and names of the two nodes of the latency entry.
            String node1 = el.getKey().getLeft();
            String node2 = el.getKey().getRight();
            double[] nodeCoord1 = nodesTable.get(node1);
            double[] nodeCoord2 = nodesTable.get(node2);
            double latency = el.getValue();

            // If the nodes are missing from the table of nodes'
            // definitions - skip
            if (nodeCoord1 == null || nodeCoord2 == null) {
                continue;
            }

            // Compute the sum of the difference between the nodes
            // and requested locations
            double distance1 = distance(reqCoord1, nodeCoord1);
            double distance2 = distance(reqCoord2, nodeCoord2);
            double distanceSum = distance1 + distance2;

            // Now do it inversely ...
            double distance1Inverse = distance(reqCoord1, nodeCoord2);
            double distance2Inverse = distance(reqCoord2, nodeCoord1);
            double distanceSumInverse = distance1Inverse + distance2Inverse;

            // Update the heap/queue...
            PingERLatencyEntry qEntry = null;
            if (distanceSum < distanceSumInverse) {
                qEntry = new PingERLatencyEntry(node1, nodeCoord1, node2, nodeCoord2, distanceSum, latency);
            } else {
                qEntry = new PingERLatencyEntry(node1, nodeCoord2, node2, nodeCoord1, distanceSumInverse, latency);
            }
            updateHeap(heap, qEntry);
        }

        double result = weigthedAverage(heap);
		return result;
	}

    public double weigthedAverage(final MinMaxPriorityQueue<PingERLatencyEntry> heap) {
        double sumLatencies = 0;
        double weigthedCount = 0;
        double bestDistance = heap.peekFirst().accumDistance;
        while (!heap.isEmpty()) {
            PingERLatencyEntry e = heap.pollFirst();
            double eWeigthedCount = bestDistance / e.accumDistance;
            weigthedCount += eWeigthedCount;
            sumLatencies += e.latency * eWeigthedCount;
            CustomLog.print(Level.FINEST, String.format(
                    "Used nodes %s, %s; Accum Distance %.2f, Latency %.2f, Weigth %.2f ", e.node1, e.node2,
                    e.accumDistance / 1000, e.latency, eWeigthedCount));
        }
        return sumLatencies / weigthedCount;
    }

    /**
     * Performance optimisation! Use this variable instead of creating a new
     * ArrayList in every call to updateHeap! <strong>NOTE:<\strong> do not
     * modify from other methods!
     */
    private final List<PingERLatencyEntry> elemsWithMatchingNodes = new ArrayList<>();

    /**
     * Adds the new entry to the heap, only if there is no other entry with less
     * distance to the requested point and having a common node. If in the heap
     * there is an element with a common node and greater distance, it is
     * replaced with the new entry.
     * 
     * <br>
     * <br>
     * 
     * The main idea of this method is to maintain "diversity", in terms of the
     * used nodes in the heap.
     * 
     * @param heap
     *            - the heap.
     * @param qEntry
     *            - the new entry.
     */
    public void updateHeap(final MinMaxPriorityQueue<PingERLatencyEntry> heap, final PingERLatencyEntry qEntry) {
        elemsWithMatchingNodes.clear();
        for (PingERLatencyEntry e : heap) {
            if (qEntry.node1.equals(e.node1) || qEntry.node1.equals(e.node2) || qEntry.node2.equals(e.node1)
                    || qEntry.node2.equals(e.node2)) {
                elemsWithMatchingNodes.add(e);
            }
        }

        if (elemsWithMatchingNodes.isEmpty()) {
            heap.offer(qEntry);
        } else {
            boolean replaceMatches = true;
            for (PingERLatencyEntry m : elemsWithMatchingNodes) {
                if (m.accumDistance < qEntry.accumDistance) {
                    replaceMatches = false;
                    break;
                }
            }

            if (replaceMatches) {
                heap.removeAll(elemsWithMatchingNodes);
                heap.offer(qEntry);
            }
        }
    }

    private static class PingERLatencyEntry implements Comparable<PingERLatencyEntry> {

        public PingERLatencyEntry(final String node1, final double[] coord1, final String node2, final double[] coord2,
                final double distance, final double latency) {
            super();
            this.node1 = node1;
            this.coord1 = coord1;
            this.node2 = node2;
            this.coord2 = coord2;
            this.accumDistance = distance;
            this.latency = latency;
        }

        final String node1;
        final double[] coord1;
        final String node2;
        final double[] coord2;
        final double accumDistance;
        final double latency;

        @Override
        public int compareTo(PingERLatencyEntry o) {
            return Double.compare(accumDistance, o.accumDistance);
        }

        @Override
        public String toString() {
            return String.format(
                    "Node1: %s, (%.2f, %.2f), Node2: %s, (%.2f, %.2f), Accum Distance: %.2f, Latency: %.2f", node1,
                    coord1[0], coord1[1], node2, coord2[0], coord2[1], accumDistance, latency);
        }
    }

    public static void main(String[] args) throws IOException {
        String ip = "124.168.86.122";
        try (GeoIP2PingERService service = new GeoIP2PingERService()) {
            System.out.println(service.getTxtAddress(ip));
        }
    }

}
