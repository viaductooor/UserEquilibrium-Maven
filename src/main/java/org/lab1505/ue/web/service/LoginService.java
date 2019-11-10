package org.lab1505.ue.web.service;

import org.lab1505.ue.web.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginService {
    @Autowired
    private UserDao userDao;

    public boolean login(String username, String password) {
        if (userDao.containUser(username)) {
            return password.equals(userDao.getPassword(username));
        }
        return false;
    }
}
