package org.progingo.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import org.progingo.config.RpcApplication;
import org.progingo.config.RpcConfig;
import org.progingo.constant.RpcConstant;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.model.ServiceMetaInfo;
import org.progingo.protocol.*;
import org.progingo.registry.Registry;
import org.progingo.registry.RegistryFactory;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;
import org.progingo.tcp.VertxTcpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        System.out.println("服务代理:构造出的请求:" + rpcRequest);


        // 从注册中心获取服务提供者请求地址
        System.out.println("服务代理:从注册中心获取服务");
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        System.out.println("服务代理:获取到注册中心配置=" + registry);

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务地址");
        }
        // 暂时先取第一个
        ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);

        // 发送 TCP 请求
        System.out.println("服务代理:通过TCP客户端发送请求");
        RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
        return rpcResponse.getData();

    }
}
