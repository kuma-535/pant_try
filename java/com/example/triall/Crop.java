package com.example.triall;

public class Crop {
    private int id;
    private String name;

    public Crop(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // New constructor for default item
    public Crop(String name) {
        this.name = name;
        this.id = -1; // or any default value
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name; // This will be displayed in the spinner
    }
}
