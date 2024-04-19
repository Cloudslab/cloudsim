package org.cloudbus.cloudsim.geolocation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Objects;

/**
 * Implements common functionalities of the geolocation services, regardless of
 * how the data is accessed - through an online API or offline.
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class BaseGeolocationService implements IGeolocationService {

    protected static final int CACHE_SIZE = 1_000_000;
    protected static final int INITIAL_CACHE_SIZE = 100_000;
    /** In order to minimise the number of created instances, we keep a cache. */
    private final Cache<GeoDistanceCacheKey, Double> distanceCache = CacheBuilder.newBuilder().concurrencyLevel(1)
            .initialCapacity(INITIAL_CACHE_SIZE).maximumSize(CACHE_SIZE).build();
    /**
     * We shall consider coordinates differing only after the ROUND_DIGITS
     * significant digit to be equal.
     */
    private static final int SIGNIFICANT_COORD_DIGITS = 1;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.geoweb.geolocation.IGeolocationService#distance(double,
     * double, double, double)
     * 
     * Based on http://www.movable-type.co.uk/scripts/latlong-vincenty.html
     */
    @Override
    public final double distance(double lat1, double lon1, double lat2, double lon2) {
        // First check in the cache...
        GeoDistanceCacheKey key = GeoDistanceCacheKey.of(lat1, lon1, lat2, lon2, SIGNIFICANT_COORD_DIGITS);
        Double cachedDistance = distanceCache.getIfPresent(key);
        if (cachedDistance != null) {
            // CustomLog.printf("[CACHED] Distance between [%.2f, %.2f] and [%.2f, %.2f] is %.3f",
            // lat1, lon1, lat2, lon2, cachedDistance);
            return cachedDistance;
        }

        // It is not in the cache... run Vincenty's formula ...
        double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563; // WGS-84
        // ellipsoid
        // params
        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        double lambda = L, lambdaP, iterLimit = 100;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return 0; // co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM))
                cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (6)
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return Double.NaN; // formula failed to converge

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B
                * sinSigma
                * (cos2SigmaM + B
                        / 4
                        * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                                * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double dist = b * A * (sigma - deltaSigma);

        // CustomLog.printf("Distance between [%.2f, %.2f] and [%.2f, %.2f] is %.3f",
        // lat1, lon1, lat2, lon2, dist);

        // Update the cache..
        distanceCache.put(key, dist);

        return dist;
    }

    @Override
    public double distance(double[] coord1, double[] coord2) {
        return distance(coord1[0], coord1[1], coord2[0], coord2[1]);
    }

    @Override
    public double latency(final String ip1, final String ip2) {
        double distanceInMeters = distance(getCoordinates(ip1), getCoordinates(ip2));
        int speedOfLigthMetPerSec = 300_000_000;

        /*
         * Based on "Latency and the Quest for Interactivity" by Stuart Chester
         * and graph 1(a) from
         * "Modelling the Internet Delay Space Based on Geographical Locations"
         * by S. Kaune et al.
         * 
         * Very inaccurate when considering IPs in developing countries as
         * discussed by Kaune et al.
         */

        int approxSpeedOfInetBackbone = speedOfLigthMetPerSec / 3;
        return 1000 * distanceInMeters / approxSpeedOfInetBackbone;
    }

    @Override
    public String getTxtAddress(String ip) {
        return Objects.toString(getMetaData(ip));
    }

    @Override
    public String getLocationMapUrl(Double... coordinates) {
        return String.format("https://maps.google.com/?q=%f,%f", coordinates[0], coordinates[1]);
    }
}
