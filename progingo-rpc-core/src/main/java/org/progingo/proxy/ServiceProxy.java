package org.progingo.proxy;

import cn.hutool.core.collection.CollUtil;

import org.progingo.config.RpcApplication;
import org.progingo.config.RpcConfig;
import org.progingo.constant.RpcConstant;
import org.progingo.fault.retry.RetryStrategy;
import org.progingo.fault.retry.RetryStrategyFactory;
import org.progingo.fault.tolerant.TolerantStrategy;
import org.progingo.fault.tolerant.TolerantStrategyFactory;
import org.progingo.loadbalancer.LoadBalancer;
import org.progingo.loadbalancer.LoadBalancerFactory;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.model.ServiceMetaInfo;
import org.progingo.registry.Registry;
import org.progingo.registry.RegistryFactory;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;
import org.progingo.tcp.VertxTcpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 负载均衡
        System.out.println("服务代理:使用负载均衡");
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("服务代理:负载均衡器选择的服务信息=" + selectedServiceMetaInfo);
        // 使用重试机制
        RpcResponse rpcResponse;
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            System.out.println("服务代理:选用重试机制为:" + retryStrategy);
            // 发送 TCP 请求
            System.out.println("服务代理:通过TCP客户端发送请求");
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e){
            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }

        return rpcResponse.getData();

    }
}
