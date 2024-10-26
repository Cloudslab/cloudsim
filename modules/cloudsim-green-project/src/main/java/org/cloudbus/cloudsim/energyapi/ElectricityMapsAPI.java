package org.cloudbus.cloudsim.energyapi;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ElectricityMapsAPI {
    private static final String BASE_URL = "https://api.electricitymap.org/v3/carbon-intensity/latest";
    private static final String POWER_BREAKDOWN_URL = "https://api.electricitymap.org/v3/power-breakdown/latest";
    private final String apiKey;

    public ElectricityMapsAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Retrieves the carbon intensity data for a specific region.
     * {
     *   "zone": "DE",
     *   "carbonIntensity": 302,
     *   "datetime": "2018-04-25T18:07:00.350Z",
     *   "updatedAt": "2018-04-25T18:07:01.000Z",
     *   "emissionFactorType": "lifecycle",
     *   "isEstimated": true,
     *   "estimationMethod": "TIME_SLICER_AVERAGE"
     * }
     */
    public String getCarbonIntensity(String region) throws IOException {
        String url = BASE_URL + "?zone=" + region;
        return fetchData(url);
    }

    /**
     * Retrieves the power breakdown data for a specific region.
     * {
     *   "zone": "FR",
     *   "datetime": "2022-04-20T09:00:00.000Z",
     *   "updatedAt": "2022-04-20T06:40:32.246Z",
     *   "createdAt": "2022-04-14T17:30:23.620Z",
     *   "powerConsumptionBreakdown": {
     *     "nuclear": 31479,
     *     "geothermal": 0,
     *     "biomass": 753,
     *     "coal": 227,
     *     "wind": 8122,
     *     "solar": 4481,
     *     "hydro": 7106,
     *     "gas": 6146,
     *     "oil": 341,
     *     "unknown": 2,
     *     "hydro discharge": 1013,
     *     "battery discharge": 0
     *   },
     *   "powerProductionBreakdown": {
     *     "nuclear": 31438,
     *     "geothermal": null,
     *     "biomass": 740,
     *     "coal": 219,
     *     "wind": 8034,
     *     "solar": 4456,
     *     "hydro": 7099,
     *     "gas": 6057,
     *     "oil": 341,
     *     "unknown": null,
     *     "hydro discharge": 1012,
     *     "battery discharge": null
     *   },
     *   "powerImportBreakdown": {
     *     "GB": 548
     *   },
     *   "powerExportBreakdown": {
     *     "GB": 0
     *   },
     *   "fossilFreePercentage": 89,
     *   "renewablePercentage": 36,
     *   "powerConsumptionTotal": 59670,
     *   "powerProductionTotal": 59396,
     *   "powerImportTotal": 548,
     *   "powerExportTotal": 0,
     *   "isEstimated": true,
     *   "estimationMethod": "TIME_SLICER_AVERAGE"
     * }
     */
    public String getPowerBreakdown(String region) throws IOException {
        String url = POWER_BREAKDOWN_URL + "?zone=" + region;
        return fetchData(url);
    }

    private String fetchData(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("auth-token", apiKey);

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("Unexpected response status: " + statusCode);
                }
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}
