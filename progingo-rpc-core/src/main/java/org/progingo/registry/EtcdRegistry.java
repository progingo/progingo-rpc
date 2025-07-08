package org.progingo.registry;

import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import org.progingo.config.RegistryConfig;
import org.progingo.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {

    private Client client;

    private KV kvClient;
    
    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        System.out.println("注册中心:初始化");
        System.out.println("注册中心:配置地址=" + registryConfig.getAddress() + ",超时时间" + registryConfig.getTimeout());
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        System.out.println("注册中心:服务注册");
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个 30 秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        // 设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        System.out.println("注册中心:key=" + registerKey + ",value=" + JSONUtil.toJsonStr(serviceMetaInfo) + ",租约时间=" + leaseId);

        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
    }

    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        System.out.println("注册中心:服务注销");
        System.out.println("注册中心:注销key=" + ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey());
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }

    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        System.out.println("注册中心:服务发现");
        // 前缀搜索，结尾一定要加 '/'
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        System.out.println("注册中心:发现key前缀=" + searchPrefix);

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();
            System.out.println("注册中心:发现服务数量:" + keyValues.size());
            // 解析服务信息
            return keyValues.stream()
                    .map(keyValue -> {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    public void destroy() {
        System.out.println("注册中心:销毁");
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }
}