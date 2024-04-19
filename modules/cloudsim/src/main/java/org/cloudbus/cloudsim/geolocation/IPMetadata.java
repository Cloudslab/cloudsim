package org.cloudbus.cloudsim.geolocation;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * Represents common metadata extracted from an IP address - e.g. geolocation,
 * etc.
 * 
 * @author nikolay.grozev
 * 
 */
public class IPMetadata {

    private final String continentName;
    private final String continentCode;
    private final String countryName;
    private final String countryIsoCode;
    private final String cityName;
    private final String postalCode;
    private final Double latitude;
    private final Double longitude;

    public IPMetadata(final String continentName, final String continentCode, final String countryName,
            final String countryIsoCode, final String cityName, final String postalCode, final Double latitude,
            final Double longitude) {
        super();
        this.continentName = continentName;
        this.continentCode = continentCode;
        this.countryName = countryName;
        this.countryIsoCode = countryIsoCode;
        this.cityName = cityName;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getContinentName() {
        return continentName;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public String getCityName() {
        return cityName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        ToStringHelper helper = MoreObjects.toStringHelper(IPMetadata.class);
        helper.add("Continent", continentName);
        helper.add("Continent Code", continentCode);
        helper.add("Country", countryName);
        helper.add("Country ISO", countryIsoCode);
        helper.add("City", cityName);
        helper.add("Post.Code", postalCode);
        helper.add("Coords",
                latitude == null || longitude == null ? null : String.format("(%.2f,%.2f)", latitude, longitude));
        helper.omitNullValues();
        return helper.toString();
    }

}
