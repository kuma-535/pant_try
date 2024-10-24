package com.example.triall;

public class Language {
    private String name;
    private String code;

    // Default constructor needed for Retrofit
    public Language() {}

    public Language(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    // Optional: toString method to display the name in the Spinner
    @Override
    public String toString() {
        return name;
    }
}
