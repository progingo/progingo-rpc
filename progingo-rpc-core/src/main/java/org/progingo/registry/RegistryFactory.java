package org.progingo.registry;

import org.progingo.spi.SpiLoader;

/**
 * 注册中心工厂（用于获取注册中心对象）
 *
 */
public class RegistryFactory {

    static {
        System.out.println("注册中心工厂:注册中心工厂加载注册中心");
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Registry getInstance(String key) {
        System.out.println("注册中心工厂:获取注册中心实例,key=" + key);
        return SpiLoader.getInstance(Registry.class, key);
    }

}