package com.aims.subsystem.vietqr.client;

import com.aims.subsystem.vietqr.config.VietQRProperties;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * HTTP client wrapper helper to handle POST requests to VietQR endpoints.
 */
@Component
public class VietQRHttpClient {

    private static final Logger log = LoggerFactory.getLogger(VietQRHttpClient.class);

    private final HttpClient httpClient;
    private final Duration httpTimeout;

    public VietQRHttpClient(VietQRProperties properties) {
        Duration timeout = Duration.ofSeconds(properties.getHttpTimeoutSeconds());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.httpTimeout = timeout;
    }

    public String post(String url, String authHeader, String jsonBody) throws IOException, InterruptedException {
        log.info("Calling VietQR HTTP endpoint: {}", url);
        HttpRequest.BodyPublisher bodyPublisher = jsonBody == null || jsonBody.isBlank()
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(httpTimeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", authHeader)
                .POST(bodyPublisher)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("VietQR HTTP response: status={}", response.statusCode());
        log.debug("VietQR HTTP response body: {}", response.body());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("VietQR HTTP request failed with status " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }
}
