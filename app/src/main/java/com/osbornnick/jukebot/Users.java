package com.osbornnick.jukebot;

import com.google.firebase.Timestamp;

public class Users {
    public Timestamp dateCreated;
    public String token;
    public String username;

    public Users(Timestamp dateCreated, String token, String username) {
        this.dateCreated = dateCreated;
        this.token = token;
        this.username = username;
    }

    public Users(){

    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
