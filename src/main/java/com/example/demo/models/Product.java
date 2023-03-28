package com.example.demo.models;

public class Product {
    private String name;
    private int price;
    private String description;
    private String manufacturer;

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String toString() {
        return "Product: " + name + ", " + price + ", " + description + ", " + manufacturer;
    }
}
