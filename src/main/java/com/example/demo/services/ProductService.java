package com.example.demo.services;

import com.example.demo.models.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {
    private final String resourceServerUri;

    public ProductService(@Value("${resource.server.url}") String resourceServerUri) {
        this.resourceServerUri = resourceServerUri;
    }

    public List<Product> getProducts() {
        String url = resourceServerUri + "/products";
        RestTemplate restTemplate = new RestTemplate();
        Product[] products = restTemplate.getForObject(url, Product[].class);

        if (products == null) {
            return List.of();
        }
        return Arrays.asList(products);
    }

}
