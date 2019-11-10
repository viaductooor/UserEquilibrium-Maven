package org.lab1505.ue.web.domain;

import org.springframework.stereotype.Component;

@Component
public class DefaultUser {
    private String username = "admin";
    private String password = "123456";

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
