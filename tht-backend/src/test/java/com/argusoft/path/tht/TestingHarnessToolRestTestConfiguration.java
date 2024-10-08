package com.argusoft.path.tht;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.http.HttpStatus.OK;


public class TestingHarnessToolRestTestConfiguration extends TestingHarnessToolTestConfiguration {

    protected Map tokenMap;
    @Value("${app.authBasicToken}")
    String authBasicToken;
    @Autowired
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;


    public void init() {
        super.init();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    public void login(
            String username,
            String password,
            WebTestClient webTestClient
    ) {

        String formData = "username=" + username + "&password=" + password + "&grant_type=password";

        this.tokenMap = webTestClient
                .post()
                .uri(uriBuilder
                        -> uriBuilder
                        .path("/oauth/token")
                        .build())
                .bodyValue(formData)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + authBasicToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(OK)
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
    }

}
