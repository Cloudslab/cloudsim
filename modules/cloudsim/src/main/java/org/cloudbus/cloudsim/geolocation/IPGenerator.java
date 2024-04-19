package org.cloudbus.cloudsim.geolocation;

import java.util.Set;

/**
 * Generates random IPs from specified countries.
 * 
 * @author nikolay.grozev
 * 
 */
public interface IPGenerator {

    /**
     * Returns the ISO codes of the countries for which this generator generates
     * IPs.
     * 
     * @return the ISO codes of the countries for which this generator generates
     *         IPs.
     */
    Set<String> getCountryCodes();

    /**
     * Creates a random IP from the specified countries. This may be either IPv4
     * or IPv6. The returned values is in the standard dot notation. If no IP
     * could be generated, null is returned.
     * 
     * @return a random IP from the specified countries or null, if no value
     *         could be generated.
     */
    String pollRandomIP();

    /**
     * Creates a random IP from the specified countries. Sometimes the origins
     * of IPs of an {@link IPGenerator} and an {@link IGeolocationService} may
     * be different - e.g. different data bases. In such cases generating an IP
     * from this generator, may not have the same metadata in a given service.
     * This method repeatedly generates IPs until an IP which is in the desired
     * location according to the service is found.
     * 
     * 
     * @param service
     *            - the service, with which the IP will be checked. Must not be
     *            null.
     * @param attempts
     *            - maximum unsuccessful attempts to poll IPs. If -1 IPs are
     *            polled until success.
     * @return a random IP from the specified countries, according to the
     *         specified service, or null, if no value could be generated.
     */
    String pollRandomIP(IGeolocationService service, int attempts);

}