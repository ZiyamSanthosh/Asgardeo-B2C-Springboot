package com.example.demo.services;

import com.example.demo.models.Product;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class ProductService {
    public List<Product> getProducts() {
        String url = "http://localhost:8081/products";
        RestTemplate restTemplate = new RestTemplate();
        Product[] products = restTemplate.getForObject(url, Product[].class);

        if (products == null) {
            return List.of();
        }
        return Arrays.asList(products);
    }

}
