package com.inkwell.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminWebsiteService {

    @Autowired
    @Qualifier("authRestTemplate")
    private RestTemplate authRestTemplate;

    @Autowired
    @Qualifier("postRestTemplate")
    private RestTemplate postRestTemplate;

    @Autowired
    @Qualifier("categoryRestTemplate")
    private RestTemplate categoryRestTemplate;

    public Map<String, Object> loadUsersData() {
        Map<String, Object> model = baseModel();

        // The current auth-service only exposes a dashboard string for admin routes,
        // so we keep the controller ready and degrade gracefully until a list-users
        // endpoint is added.
        model.put("users", List.of());
        model.put("authSummary", fetchText(authRestTemplate, "http://localhost:8081/admin/dashboard"));
        return model;
    }

    public Map<String, Object> loadAllPostsData() {
        Map<String, Object> model = baseModel();
        model.put("posts", fetchList(postRestTemplate, "http://localhost:8082/api/v1/posts"));
        return model;
    }

    public Map<String, Object> loadCategoriesData() {
        Map<String, Object> model = baseModel();
        model.put("categories", fetchList(categoryRestTemplate, "http://localhost:8084/api/v1/categories"));
        model.put("tags", fetchList(categoryRestTemplate, "http://localhost:8084/api/v1/tags"));
        return model;
    }

    private Map<String, Object> baseModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("serviceUnavailable", false);
        model.put("serviceMessage", null);
        return model;
    }

    private List<Map<String, Object>> fetchList(RestTemplate restTemplate, String url) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (RestClientException ex) {
            return List.of();
        }
    }

    private String fetchText(RestTemplate restTemplate, String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException ex) {
            return "Auth service is temporarily unavailable.";
        }
    }
}
