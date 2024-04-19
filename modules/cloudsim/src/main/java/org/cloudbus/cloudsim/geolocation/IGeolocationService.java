package org.cloudbus.cloudsim.geolocation;

import java.io.Closeable;

/**
 * Implements functionalities for extracting geographical location of servers
 * based on their IPs and computing different measures based on that.
 * Implementing classes can either use offline databases or online services.
 * 
 * <br>
 * <br>
 * 
 * Input IP addresses can be either IPv4 or IPv6. IPv4 addresses should be in
 * the standard dotted form (e.g. 1.2.3.4). IPv6 addresses should be in the
 * canonical form described in RFC 5952 - e.g. 2001:db8::1:0:0:1.
 * 
 * @author nikolay.grozev
 * 
 */
public interface IGeolocationService extends Closeable {

    /**
     * Returns estimation of the geographical the latitude and the longitude of
     * the provided IP address.
     * 
     * @param ip
     *            - the ip to estimate the geolocation for. Must not be null.
     *            Must be a valid ip, in the sense of the previously described
     *            formats.
     * @return an array with two elements in the form [Lattitude, Longtitude].
     *         If any of them could not be estimated, the corresponding value in
     *         the resulting arrays is NaN - e.g. [NaN, NaN].
     */
    double[] getCoordinates(final String ip);

    /**
     * Computes the distance in meters between between the points with
     * coordinates [lat1, lon1] and [lat2, lon2]. The returned value is in
     * meters.
     * 
     * @param lat1
     *            - the latitude of the first point. Must be valid latitude.
     * @param lon1
     *            - the longitude of the first point. Must be valid longitude.
     * @param lat2
     *            - the latitude of the second point. Must be valid latitude.
     * @param lon2
     *            - the longtitude of the second point. Must be valid longitude.
     * @return the distance in meters between the two points in meters.
     */
    double distance(double lat1, double lon1, double lat2, double lon2);

    /**
     * Computes the distance in meters between between the points with
     * coordinates [lat1, lon1] and [lat2, lon2]. The returned value is in
     * meters.
     * 
     * @param coord1
     *            - the latitude and longitude of the first point. Must be valid
     *            coordinates.
     * @param coord2
     *            - the latitude and longitude of the second point. Must be
     *            valid coordinates.
     * @return the distance in meters between the two points in meters.
     */
    double distance(double[] coord1, double[] coord2);

    /**
     * Returns the extracted geographical information about the IP address.
     * 
     * @param ip
     *            - the ip to estimate the geolocation for. Must not be null.
     *            Must be a valid ip, in the sense of the previously described
     *            formats.
     * @return the extracted geographical information about the IP address. Any
     *         of properties can be null in case the data could not be
     *         extracted. Null can be returned if no metadata is extracted.
     */
    IPMetadata getMetaData(final String ip);

    /**
     * Returns an estimation of the latency between the two IPs, measured in ms.
     * 
     * @param ip1
     *            - must not be null. Must be a valid ip, in the sense of the
     *            previously described formats.
     * @param ip2
     *            - must not be null. Must be a valid ip, in the sense of the
     *            previously described formats.
     * @return an estimation of the latency between the two IPs, measured in ms.
     */
    double latency(String ip1, String ip2);

    /**
     * Returns an estimation of the latency between the two IPs, measured in ms.
     * 
     * @param reqCoord1
     *            - the latitude and longitude of the first point. Must be valid
     *            coordinates.
     * @param reqCoord2
     *            - the latitude and longitude of the second point. Must be
     *            valid coordinates.
     * @return an estimation of the latency between the two IPs, measured in ms.
     */
	double latency(double[] reqCoord1, double[] reqCoord2);
    
    /**
     * Returns a textual representation of the location of the ip - useful for
     * debug purposes.
     * 
     * @param ip
     *            - the ip to estimate the geolocation for. Must not be null.
     *            Must be a valid ip, in the sense of the previously described
     *            formats.
     * @return a textual representation of the location of the ip - useful for
     *         debug purposes.
     */
    String getTxtAddress(final String ip);

    /**
     * Optional method for debug purposes. Returns a url to a map, displaying
     * the position of the coordinates.
     * 
     * @param coordinates
     *            - the coordinates to display.
     * @return - a url to map, displaying the position of the coordinates or
     *         null, if not implemented.
     */
    String getLocationMapUrl(Double... coordinates);

}
