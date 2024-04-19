package org.cloudbus.cloudsim.geolocation;

/**
 * Implements common utility functions for handling IPs.
 * 
 * @author nikolay.grozev
 * 
 */
public class IPUtil {

    /**
     * Converts the integer representation of an IPv4 to a canonical String
     * representation.
     * 
     * @param ip
     *            - the IP in integer format.
     * @return the canonical String representation of the provided in
     *         representation.
     */
    public static String convertIPv4(final int ip) {
        int lastByteMask = 0b11111111;
        int i1 = ip >>> 24;
        int i2 = ip >> 16 & lastByteMask;
        int i3 = ip >> 8 & lastByteMask;
        int i4 = ip & lastByteMask;

        return String.format("%d.%d.%d.%d", i1, i2, i3, i4);
    }

}
