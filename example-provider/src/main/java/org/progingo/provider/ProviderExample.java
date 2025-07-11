package org.progingo.provider;

import org.progingo.bootstrap.ProviderBootstrap;
import org.progingo.model.ServiceRegisterInfo;
import org.progingo.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示例
 *
 */
public class ProviderExample {

    public static void main(String[] args) {
        System.out.println("服务提供者:开始启动服务");
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}