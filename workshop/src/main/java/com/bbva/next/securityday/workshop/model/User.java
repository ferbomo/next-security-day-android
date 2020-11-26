package com.bbva.next.securityday.workshop.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private long id;
    @SerializedName("userId")
    private String userId;
    @SerializedName("authenticated")
    private int authenticated;

    public long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAuthenticated() {
        return authenticated;
    }


}
