package org.lab1505.ue.web.dao;

import org.lab1505.ue.web.domain.DefaultUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDao {
    @Autowired
    private DefaultUser defaultUser;

    public boolean containUser(String username) {
        return username.equals(defaultUser.getUsername());
    }

    public String getPassword(String username) {
        return defaultUser.getPassword();
    }
}
