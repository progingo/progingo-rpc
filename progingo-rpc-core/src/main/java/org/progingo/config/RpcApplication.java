package org.progingo.config;

import lombok.extern.slf4j.Slf4j;
import org.progingo.constant.RpcConstant;
import org.progingo.registry.Registry;
import org.progingo.registry.RegistryFactory;
import org.progingo.utils.ConfigUtils;

/**
 * RPC 框架应用
 * 相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，支持传入自定义配置
     *
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        System.out.println("RPC框架应用:使用配置=" + newRpcConfig);
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", newRpcConfig.toString());
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init, config = {}", registryConfig);
    }

    /**
     * 初始化
     */
    public static void init() {
        System.out.println("RPC框架应用:初始化配置");
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 配置加载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     *
     * @return
     */
    public static RpcConfig getRpcConfig() {
        System.out.println("RPC框架应用:获取配置");
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}