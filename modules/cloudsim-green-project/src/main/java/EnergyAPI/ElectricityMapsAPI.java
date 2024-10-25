package EnergyAPI;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Returns: {
 *   "zone": "DE",
 *   "carbonIntensity": 302,
 *   "datetime": "2018-04-25T18:07:00.350Z",
 *   "updatedAt": "2018-04-25T18:07:01.000Z",
 *   "emissionFactorType": "lifecycle",
 *   "isEstimated": true,
 *   "estimationMethod": "TIME_SLICER_AVERAGE"
 * }
 */
public class ElectricityMapsAPI {
    private static final String BASE_URL = "https://api.electricitymap.org/v3/carbon-intensity/latest";
    private final String apiKey;

    public ElectricityMapsAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getCarbonIntensity(String region) throws IOException {
        String url = BASE_URL + "?zone=" + region;

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
