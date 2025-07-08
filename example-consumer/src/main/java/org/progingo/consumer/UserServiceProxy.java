package org.progingo.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.model.User;
import org.progingo.serializer.JdkSerializer;
import org.progingo.serializer.Serializer;
import org.progingo.service.UserService;

import java.io.IOException;

/**
 * 静态代理
 * 静态代理类的作用是给消费者使用，并通过HTTP请求调用服务提供者。在消费者眼里就是调用到了服务提供者提供的实现，所以是代理
 */
@Deprecated
public class UserServiceProxy implements UserService {

    public User getUser(User user) {
        // 指定序列化器
        Serializer serializer = new JdkSerializer();

        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try {
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            byte[] result;
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                result = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}