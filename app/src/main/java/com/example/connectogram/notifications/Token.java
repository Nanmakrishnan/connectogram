package com.example.connectogram.notifications;

public class Token {
    public Token(String token) {
        this.token = token;
    }

    String token;

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
