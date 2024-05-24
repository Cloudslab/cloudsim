package org.cloudbus.cloudsim.geolocation.geoip2;

import com.google.common.base.Preconditions;
import org.cloudbus.cloudsim.EX.util.CustomLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Utility class for loading resources.
 * 
 * @author nikolay.grozev
 *
 */
public final class ResourceUtil {

    private ResourceUtil() { }
    
    /** Default resource. */
    public static final String DEFAULT_GEO_IP_COUNTRY_CSV = "/GeoIPCountryWhois.csv";
    /** Default resource. */
    public static final String DEFAULT_GEO_LITE2_CITY_MMDB = "/GeoLite2-City.mmdb";
    /** Default resource. */
    public static final String TEST_GEO_LITE2_CITY_MMDB = "/Test_GeoLite2-City.mmdb";
    /** Default resource. */
    public static final String DEFAULT_PING_TABLE_PING_ER_TSV = "/PingTablePingER.tsv";
    /** Default resource. */
    public static final String DEFAULT_MONITORING_SITES_PING_ER_CSV = "/MonitoringSitesPingER.csv";
    
    /**
     * Opens a stream to the file. Checked exceptions are converted to unchecked.
     * @param file - the file. Must not be null. Must be valid.
     * @return a stream to the file
     */
    public static InputStream toStream(final File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            String msg = "Invalid file: " + file + " Error details:" + e.getMessage();
            CustomLog.logError(Level.SEVERE, msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }
    
    /**
     * Loads the resource with the main class loader.
     * @param resource - the name of the resource to load. Must not be null.
     * @return a stream to the resource, or null if it does not exist.
     */
    public static InputStream classLoad(final String resource) {
        Preconditions.checkNotNull(resource);
//        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        return ResourceUtil.class.getResourceAsStream(resource);
    }
}
