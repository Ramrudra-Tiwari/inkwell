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
public class AuthorWebsiteService {

    @Autowired
    @Qualifier("postRestTemplate")
    private RestTemplate postRestTemplate;

    @Autowired
    @Qualifier("categoryRestTemplate")
    private RestTemplate categoryRestTemplate;
    @Autowired
    @Qualifier("mediaRestTemplate")
    private RestTemplate mediaRestTemplate;

    public Map<String, Object> loadAuthorDashboard(Integer authorId) {
        Map<String, Object> model = baseModel();
        model.put("authorId", authorId);
        model.put("drafts", fetchList(postRestTemplate,
            "http://localhost:8082/api/v1/posts/author/" + authorId + "/status/DRAFT"));
        model.put("publishedPosts", fetchList(postRestTemplate,
            "http://localhost:8082/api/v1/posts/author/" + authorId + "/status/PUBLISHED"));
        return model;
    }

    public Map<String, Object> loadCreatePostData(Integer authorId) {
        Map<String, Object> model = baseModel();
        model.put("authorId", authorId);
        model.put("categories", fetchList(categoryRestTemplate, "http://localhost:8084/api/v1/categories"));
        model.put("tags", fetchList(categoryRestTemplate, "http://localhost:8084/api/v1/tags"));
        return model;
    }

    public Map<String, Object> loadAuthorPosts(Integer authorId) {
        Map<String, Object> model = baseModel();
        model.put("authorId", authorId);
        model.put("posts", fetchList(postRestTemplate, "http://localhost:8082/api/v1/posts/author/" + authorId));
        model.put("media", fetchList(mediaRestTemplate, "http://localhost:8085/api/v1/media/uploader/" + authorId));
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
}
