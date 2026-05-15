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
public class BlogWebsiteService {

    @Autowired
    @Qualifier("postRestTemplate")
    private RestTemplate postRestTemplate;

    @Autowired
    @Qualifier("commentRestTemplate")
    private RestTemplate commentRestTemplate;

    @Autowired
    @Qualifier("categoryRestTemplate")
    private RestTemplate categoryRestTemplate;

    public Map<String, Object> loadHomePageData() {
        Map<String, Object> model = baseModel();
        model.put("posts", fetchList(postRestTemplate, "http://localhost:8082/api/v1/posts/published"));
        model.put("categories", fetchList(categoryRestTemplate, "http://localhost:8084/api/v1/categories/top-level"));
        return model;
    }

    public Map<String, Object> loadPostPageData(String slug) {
        Map<String, Object> model = baseModel();
        Map<String, Object> post = fetchObject(postRestTemplate, "http://localhost:8082/api/v1/posts/slug/" + slug);
        model.put("post", post);

        Object postId = post.get("postId");
        if (postId != null) {
            model.put("comments", fetchList(commentRestTemplate, "http://localhost:8083/api/v1/comments/post/" + postId));
        } else {
            model.put("comments", List.of());
        }

        return model;
    }

    public Map<String, Object> loadSearchResults(String keyword) {
        Map<String, Object> model = baseModel();
        model.put("keyword", keyword);
        model.put("results", fetchList(postRestTemplate, "http://localhost:8082/api/v1/posts/search?keyword=" + keyword));
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

    private Map<String, Object> fetchObject(RestTemplate restTemplate, String url) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (RestClientException ex) {
            return Map.of(
                "error", "Service unavailable",
                "message", "The requested content is temporarily unavailable."
            );
        }
    }
}
