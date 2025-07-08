package org.progingo.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.progingo.config.RpcApplication;
import org.progingo.config.RpcConfig;
import org.progingo.constant.RpcConstant;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.model.ServiceMetaInfo;
import org.progingo.registry.Registry;
import org.progingo.registry.RegistryFactory;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;

import java.io.IOException;
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

        try {
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
            System.out.println("服务代理:使用服务地址=" + selectedServiceMetaInfo.getServiceAddress());
            // 序列化
            System.out.println("服务代理:开始序列化");
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            System.out.println("服务代理:调用URL");
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
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
