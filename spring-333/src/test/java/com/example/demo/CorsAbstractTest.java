package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.example.demo.DemoAppConstants.ALLOWED_ORIGIN;
import static com.example.demo.DemoAppConstants.AUTHENTICATED_ENDPOINT;
import static com.example.demo.DemoAppConstants.DISALLOWED_ORIGIN;
import static org.assertj.core.api.Assertions.assertThat;


public abstract class CorsAbstractTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void t01_standardUnauthenticatedRequest_shouldReturn401WithoutVary() {
        var response = getUnauthenticatedRestTemplate().getForEntity(AUTHENTICATED_ENDPOINT, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
        //assertThat(response.getHeaders()).doesNotContainKey("Vary");  // holds in SB 3.3.3
    }

    @Test
    void t02_corsSimpleAllowedUnauthenticatedRequest_shouldReturn401() {
        var response = sendSimpleCorsRequest(getUnauthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, ALLOWED_ORIGIN);

        expect401WithAccessControlAllowOrigin(response);
    }

    @Test
    void t03_corsSimpleDisallowedUnauthenticatedRequest_shouldReturn403() {
        var response = sendSimpleCorsRequest(getUnauthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, DISALLOWED_ORIGIN);

        expect403WithInvalidCorsResponse(response);
    }

    @Test
    void t04_standardAuthenticatedRequest_shouldReturn200WithoutVary() {
        var response = getAuthenticatedRestTemplate().getForEntity(AUTHENTICATED_ENDPOINT, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");
        //assertThat(response.getHeaders()).doesNotContainKey("Vary");  // holds in SB 3.3.3
    }

    @Test
    void t05_corsSimpleAllowedAuthenticatedRequest_shouldReturn200WithCorsHeaders() {
        var response = sendSimpleCorsRequest(getAuthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, ALLOWED_ORIGIN);

        expect200WithAccessControlAllowOriginAndBody(response);
    }

    @Test
    void t06_corsSimpleDisallowedAuthenticatedRequest_shouldReturn403WithInvalidCors() {
        var response = sendSimpleCorsRequest(getUnauthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, DISALLOWED_ORIGIN);

        expect403WithInvalidCorsResponse(response);
    }

    @Test
    void t07_corsPreflightAllowedUnauthenticatedRequest_shouldReturn200() {
        var response = sendPreflightCorsRequest(getUnauthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, ALLOWED_ORIGIN);

        expect200WithAccessControlAllowOriginAndNoBody(response);
    }

    @Test
    void t08_corsPreflightDisallowedUnauthenticatedRequest_shouldReturn403() {
        var response = sendPreflightCorsRequest(getUnauthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, DISALLOWED_ORIGIN);

        expect403WithInvalidCorsResponse(response);
    }

    @Test
    void t09_corsPreflightAllowedAuthenticatedRequest_shouldReturn200WithCorsHeaders() {
        var response = sendPreflightCorsRequest(getAuthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, ALLOWED_ORIGIN);

        expect200WithAccessControlAllowOriginAndNoBody(response);
    }

    @Test
    void t10_corsPreflightDisallowedAuthenticatedRequest_shouldReturn403WithInvalidCors() {
        var response = sendPreflightCorsRequest(getAuthenticatedRestTemplate(), AUTHENTICATED_ENDPOINT, DISALLOWED_ORIGIN);

        expect403WithInvalidCorsResponse(response);
    }

    private void expect200WithAccessControlAllowOriginAndBody(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");
        assertThat(response.getHeaders()).containsKeys("Vary", "Access-Control-Allow-Origin");
        assertThat(response.getHeaders().get("Vary")).containsExactlyInAnyOrder("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers");
        assertThat(response.getHeaders().get("Access-Control-Allow-Origin")).containsExactly(ALLOWED_ORIGIN);
    }

    private void expect200WithAccessControlAllowOriginAndNoBody(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        assertThat(response.getHeaders()).containsKeys("Vary", "Access-Control-Allow-Origin");
        assertThat(response.getHeaders().get("Vary")).containsExactlyInAnyOrder("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers");
        assertThat(response.getHeaders().get("Access-Control-Allow-Origin")).containsExactly(ALLOWED_ORIGIN);
    }

    private void expect403WithInvalidCorsResponse(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Invalid CORS request");
        assertThat(response.getHeaders()).containsKey("Vary");
        assertThat(response.getHeaders().get("Vary")).containsExactlyInAnyOrder("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers");
        assertThat(response.getHeaders()).doesNotContainKey("Access-Control-Allow-Origin");
    }

    private void expect401WithAccessControlAllowOrigin(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
        assertThat(response.getHeaders()).containsKeys("Vary", "Access-Control-Allow-Origin");
        assertThat(response.getHeaders().get("Vary")).containsExactlyInAnyOrder("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers");
        assertThat(response.getHeaders().get("Access-Control-Allow-Origin")).containsExactly(ALLOWED_ORIGIN);
    }

    private TestRestTemplate getUnauthenticatedRestTemplate() {
        return restTemplate;
    }

    private TestRestTemplate getAuthenticatedRestTemplate() {
        return restTemplate.withBasicAuth("user", "password");
    }

    private ResponseEntity<String> sendSimpleCorsRequest(TestRestTemplate restTemplate, String path, String origin) {
        var headers = new HttpHeaders();
        headers.setOrigin(origin);

        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<String> sendPreflightCorsRequest(TestRestTemplate restTemplate, String path, String origin) {
        var headers = new HttpHeaders();
        headers.setOrigin(origin);
        headers.add("Access-Control-Request-Method", "GET");

        var optionsRequest = new HttpEntity<>(headers);
        return restTemplate.exchange(path, HttpMethod.OPTIONS, optionsRequest, String.class);
    }
}
