package com.example.triall;

public class Variety {
    private int id;
    private String name;

    public Variety(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // New constructor for default item
    //public Variety(String name) {
        //this.name = name;
        //this.id = -1; // or any default value
    //}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name; // Display the name in the spinner
    }
}
