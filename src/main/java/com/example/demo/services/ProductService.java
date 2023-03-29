package com.example.demo.services;

import com.example.demo.models.Product;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Value("${resource.server.url}")
    private String resourceServerUri;

    @Autowired
    RestTemplate restTemplate;


    public List<Product> getProducts() {
        String url = resourceServerUri + "/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);

        if (products == null) {
            return List.of();
        }
        return Arrays.asList(products);
    }

    public int addProduct(String accessToken) {

        String url = resourceServerUri + "/api/addProducts";
        logger.info("Rendering post api:" + url );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        JSONObject object = new JSONObject();
        object.put("name", "test");
        object.put("id", UUID.randomUUID());
        logger.info("Rendering accessToken:" + accessToken );
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request =
                new HttpEntity<String>(object.toString(), headers);

        ResponseEntity<String> restApi = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        return restApi.getStatusCode().value();
    }

}
