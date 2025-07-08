package org.progingo.provider;

import org.progingo.config.RpcApplication;
import org.progingo.config.RpcConfig;
import org.progingo.registry.LocalRegistry;
import org.progingo.server.HttpServer;
import org.progingo.server.VertxHttpServer;
import org.progingo.service.UserService;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();
        //尝试获取配置类
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        System.out.println("RPC配置：" + rpcConfig);
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
