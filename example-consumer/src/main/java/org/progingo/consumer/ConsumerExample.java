package org.progingo.consumer;

import org.progingo.bootstrap.ConsumerBootstrap;
import org.progingo.model.User;
import org.progingo.proxy.ServiceProxyFactory;
import org.progingo.service.UserService;

/**
 * 服务消费者示例
 *
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // 服务提供者初始化
        ConsumerBootstrap.init();
        // 获取代理
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