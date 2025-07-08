package org.progingo.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.progingo.config.RpcApplication;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("服务代理:进入ServiceProxy的invoke()方法");
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        System.out.println("服务代理:选择使用构造器:" + serializer);
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        System.out.println("服务代理:构造出的请求:" + rpcRequest);

        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            // todo 这里地址被硬编码了（需要使用注册中心和服务发现机制解决）
            System.out.println("服务代理:调用URL");
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                System.out.println("服务代理:进行反序列化");
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                System.out.println("服务代理:反序列化的结果:" + rpcResponse);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
