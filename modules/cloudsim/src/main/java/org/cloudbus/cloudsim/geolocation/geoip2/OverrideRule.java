package org.cloudbus.cloudsim.geolocation.geoip2;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

/**
 * An immutable class representing an override rule for GeoIP services.
 * 
 * @author nikolay.grozev
 *
 */
public class OverrideRule {

    private final String ip_prefix;
    private SubnetInfo netInfo;

    private final String location;
    private final double lon;
    private final double lat;
    private final String details;

    /**
     * Ctor.
     * 
     * @param ip_prefix
     *            - must be in the form IPv4/mask. May not be null;
     * @param location
     *            - location of the address. May not be null.
     * @param lat
     *            - the latitude.
     * @param lon
     *            - the longitude.
     * @param details
     *            - miscellaneous details.
     */
    public OverrideRule(final String ip_prefix, final String location, final double lat, final double lon,
            final String details) {
        this.ip_prefix = ip_prefix;
        this.location = location;
        this.lon = lon;
        this.lat = lat;
        this.details = details;
    }

    protected SubnetInfo netInfo() {
        if (netInfo == null) {
            netInfo = new SubnetUtils(ip_prefix).getInfo();
        }
        return netInfo;
    }

    /**
     * Returns if the specified IP address is in the range of this rule.
     * 
     * @param ip
     *            - the ip address. Must not be null. Must be in IPv4 notation.
     * @return if the specified IP address is in the range of this rule.
     */
    public boolean matches(final String ip) {
        Preconditions.checkNotNull(ip);
        return netInfo().isInRange(ip);
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("ip_prefix", ip_prefix).add("location", location).add("lon", lon)
                .add("lat", lat).add("Details", details).toString();
    }
}
