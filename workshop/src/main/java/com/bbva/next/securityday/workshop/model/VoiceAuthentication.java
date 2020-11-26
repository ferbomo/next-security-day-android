package com.bbva.next.securityday.workshop.model;

import com.google.gson.annotations.SerializedName;

public class VoiceAuthentication {

    @SerializedName("confidenceNumber")
    private double confidenceNumber;

    public double getConfidenceNumber() {
        return confidenceNumber;
    }
}
