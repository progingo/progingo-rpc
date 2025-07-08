package org.progingo.proxy;

import org.progingo.config.RpcApplication;

import java.lang.reflect.Proxy;

public class ServiceProxyFactory {

    /**
     * 根据服务类获取代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        System.out.println("代理工厂:进入代理工厂");
        if (RpcApplication.getRpcConfig().isMock()) {
            System.out.println("代理工厂:获取到Mock代理");
            return getMockProxy(serviceClass);
        }
        System.out.println("代理工厂:获取到真实代理");
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }

    /**
     * 根据服务类获取 Mock 代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy());
    }

}
