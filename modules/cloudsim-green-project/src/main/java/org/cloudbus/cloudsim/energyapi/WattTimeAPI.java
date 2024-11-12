// Written by Kevin Le (kevinle2)

package org.cloudbus.cloudsim.energyapi;

import java.io.IOException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Note: Need to login every 30 min to get a new token
 * I already registered as a user so dont need to worry about that part of the code
 */

public class WattTimeAPI {
    private static final String REGISTER_URL = "https://api2.watttime.org/v2/register";
    private static final String LOGIN_URL = "https://api2.watttime.org/v2/login";
    private static final String DATA_URL = "https://api2.watttime.org/v2/data?ba=CAISO_NORTH";
    private String accessToken;

    public void registerUser(String username, String password, String email, String org) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(REGISTER_URL);

            String jsonPayload = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"org\":\"%s\"}",
                    username, password, email, org
            );

            post.setEntity(new StringEntity(jsonPayload));
            post.setHeader("Content-type", "application/json");

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                System.out.println("Registration Response: " + jsonResponse);
            }
        }
    }

    public void login(String username, String password) throws IOException {
        // Credentials provider for Basic Authentication
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(username, password)
        );

        // HTTP client with the credentials provider
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build()) {

            HttpGet get = new HttpGet(LOGIN_URL);

            try (CloseableHttpResponse response = client.execute(get)) {

                String responseBody = EntityUtils.toString(response.getEntity());
                JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                accessToken = json.get("token").getAsString();
                System.out.println("Login Token: " + accessToken);
            }
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getData() throws IOException {
        HttpGet request = new HttpGet(DATA_URL);
        request.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("Response: " + responseBody);
                    return responseBody;
                } else {
                    System.out.println("Failed: " + response.getStatusLine().getStatusCode());
                    return null;
                }
            }
        }
    }

}
