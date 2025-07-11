package org.progingo.examplespringbootprovider;

import org.progingo.model.User;
import org.progingo.progingorpc.springboot.starter.annotation.RpcService;
import org.progingo.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {

    public User getUser(User user) {
        System.out.println("服务提供者收到用户名：" + user.getName());
        user.setName(user.getName() + "(代理)");
        return user;
    }
}
