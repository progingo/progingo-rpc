package org.progingo.provider;

import org.progingo.model.User;

public class UserServiceImpl {
    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
