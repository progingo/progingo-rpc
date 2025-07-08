package org.progingo.serializer;

import org.progingo.spi.SpiLoader;


/**
 * 序列化器工厂（用于获取序列化器对象）
 *
 */
public class SerializerFactory {

    static {
        //让SPI加载器去根据配置文件加载序列化器
        System.out.println("序列化工厂:序列化器工厂加载序列化器");
        SpiLoader.load(Serializer.class);
    }

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        System.out.println("序列化工厂:获取序列化实例,key=" + key);
        return SpiLoader.getInstance(Serializer.class, key);
    }

}