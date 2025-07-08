package org.progingo.consumer;

import org.progingo.model.User;
import org.progingo.proxy.ServiceProxyFactory;
import org.progingo.service.UserService;

/**
 * 简易服务消费者示例
 *
 */
public class ConsumerExample {

    public static void main(String[] args) {
/*        //测试能不能获取到配置文件
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);*/
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
        //检测获取到的是代理对象还是Mock代理
        System.out.println(userService.getNumber());


    }
}