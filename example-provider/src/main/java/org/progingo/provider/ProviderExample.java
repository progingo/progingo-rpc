package org.progingo.provider;

import org.progingo.config.RegistryConfig;
import org.progingo.config.RpcApplication;
import org.progingo.config.RpcConfig;
import org.progingo.model.ServiceMetaInfo;
import org.progingo.registry.LocalRegistry;
import org.progingo.registry.Registry;
import org.progingo.registry.RegistryFactory;
import org.progingo.service.UserService;
import org.progingo.tcp.VertxTcpServer;

/**
 * 服务提供者示例
 *
 */
public class ProviderExample {

    public static void main(String[] args) {
        System.out.println("服务提供者:开始启动服务");
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();

        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());//自己提供服务的地址
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());//自己提供服务的端口
        try {
            registry.register(serviceMetaInfo);//注册到注册中心，让消费者发现
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 启动 TCP 服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}