package org.cloudbus.cloudsim.geolocation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Serves as a cache entry key in a hash table backed cache. For example it can
 * be used cache entries of type [lat1, lon1, lat2, lon2] -> distance. This is
 * an immutable class. Overrides equals and hashcode appropriately.
 * 
 * @author nikolay.grozev
 * 
 */
public class GeoDistanceCacheKey {

    private static final int CACHE_SIZE = 10_000;
    private static final int INITIAL_CACHE_SIZE = 1_000;
    /** In order to minimise the number of created instances, we keep a cache. */
    private static final Cache<Integer, GeoDistanceCacheKey> CACHE = CacheBuilder.newBuilder().concurrencyLevel(1)
            .initialCapacity(INITIAL_CACHE_SIZE).maximumSize(CACHE_SIZE).build();

    private final double lat1;
    private final double lon1;
    private final double lat2;
    private final double lon2;

    private final int hashCode;

    private GeoDistanceCacheKey(double lat1, double lon1, double lat2, double lon2) {
        super();
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;

        // This is an immutable class therefore we can pre-computer the hash
        // now.
        hashCode = computeHash(lat1, lon1, lat2, lon2);
    }

    public double getLat1() {
        return lat1;
    }

    public double getLon1() {
        return lon1;
    }

    public double getLat2() {
        return lat2;
    }

    public double getLon2() {
        return lon2;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoDistanceCacheKey)) {
            return false;
        } else if (this == obj) {
            return true;
        } else {
            GeoDistanceCacheKey other = (GeoDistanceCacheKey) obj;
            return hashCode == other.hashCode
                    && areEqualCoords(lat1, lon1, lat2, lon2, other.lat1, other.lon1, other.lat2, other.lon2);
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private static int computeHash(double lat1, double lon1, double lat2, double lon2) {
        int exp = 1000;
        return (int) (lat1 * exp) ^ (int) (lon1 * exp) + (int) (lat2 * exp) ^ (int) (lon2 * exp);
    }

    private static boolean areEqualCoords(double lat1_1, double lon1_1, double lat1_2, double lon1_2, double lat2_1,
            double lon2_1, double lat2_2, double lon2_2) {
        boolean match = lat1_1 == lat2_1 && lon1_1 == lon2_1 && lat1_2 == lat2_2 && lon1_2 == lon2_2;
        boolean matchInverse = !match && lat1_1 == lat2_2 && lon1_1 == lon2_2 && lat1_2 == lat2_1 && lon1_2 == lon2_1;
        return match || matchInverse;
    }

    /**
     * Factory method for instances.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static GeoDistanceCacheKey of(double lat1, double lon1, double lat2, double lon2) {
        return of(lat1, lon1, lat2, lon2, -1);
    }

    /**
     * Factory method for instances. Rounds the coordinates.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param digits
     *            - number of digits after the decimal sign to consider. Must be
     *            positive. If negative or zero - no rounding is performed.
     * @return
     */
    public static GeoDistanceCacheKey of(double lat1, double lon1, double lat2, double lon2, int digits) {
        double newLat1 = digits > 0 ? round(lat1, digits) : lat1;
        double newLon1 = digits > 0 ? round(lon1, digits) : lon1;
        double newLat2 = digits > 0 ? round(lat2, digits) : lat2;
        double newLon2 = digits > 0 ? round(lon2, digits) : lon2;

        int hash = computeHash(newLat1, newLon1, newLat2, newLon2);
        GeoDistanceCacheKey cached = CACHE.getIfPresent(hash);
        if (cached == null || !areEqualCoords(lat1, lon1, lat1, lon1, lat2, cached.lon1, cached.lat2, cached.lon2)) {
            cached = new GeoDistanceCacheKey(newLat1, newLon1, newLat2, newLon2);
            CACHE.put(hash, cached);
        }

        return cached;
    }

    private static double round(double num, int digits) {
        if (digits > 0) {
            double exp = Math.pow(10, digits);
            return Math.round(num * exp) / exp;
        } else if (digits == 0) {
            return (int) num;
        } else {
            return num;
        }
    }
}
