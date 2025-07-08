package org.progingo.consumer;

import org.progingo.model.User;
import org.progingo.proxy.ServiceProxyFactory;
import org.progingo.service.UserService;

public class EasyConsumerExample {
    public static void main(String[] args) {

        //静态代理
        //UserService userService = new UserServiceProxy();

        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        
        User user = new User();
        user.setName("progingo");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
