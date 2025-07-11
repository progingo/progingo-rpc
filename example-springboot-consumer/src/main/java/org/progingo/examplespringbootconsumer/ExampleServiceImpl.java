package org.progingo.examplespringbootconsumer;

import org.progingo.model.User;
import org.progingo.progingorpc.springboot.starter.annotation.RpcReference;
import org.progingo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {

    @RpcReference
    private UserService userService;

    public void test() {
        User user = new User();
        user.setName("progingo");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }

}