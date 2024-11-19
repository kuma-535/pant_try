package com.example.triall;

public class DiseasePredictionResponse {
    private String disease_message;
    private String error;

    // Getter and Setter methods
    public String getMessage() {
        return disease_message;
    }

    public void setDiseaseMessage(String disease_message) {
        this.disease_message = disease_message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

