package org.progingo.config;

import lombok.Data;
import lombok.ToString;
import org.progingo.serializer.SerializerKeys;

/**
 * RPC 框架配置
 */
@Data
@ToString
public class RpcConfig {
    /**
     * 名称
     */
    private String name = "progingo-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
    /**
     * 模拟调用
     */
    private boolean mock = false;
    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
