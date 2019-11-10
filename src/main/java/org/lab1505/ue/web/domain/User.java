package org.lab1505.ue.web.domain;

public class User {
    private String username;
    private String password;

    public String getId() {
        return username;
    }

    public void setId(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
