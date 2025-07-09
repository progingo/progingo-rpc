package org.progingo.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import org.progingo.config.RpcApplication;
import org.progingo.config.RpcConfig;
import org.progingo.constant.RpcConstant;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.model.ServiceMetaInfo;
import org.progingo.protocol.*;
import org.progingo.registry.Registry;
import org.progingo.registry.RegistryFactory;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        try {
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
            // 暂时先取第一个
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            System.out.println("服务代理:使用服务地址=" + selectedServiceMetaInfo.getServiceAddress());
            // 序列化
            System.out.println("服务代理:开始序列化");
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送TCP请求
            Vertx vertx = Vertx.vertx();
            NetClient netClient = vertx.createNetClient();
            CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
            netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(),
                    result -> {
                        if (result.succeeded()) {
                            System.out.println("服务代理:成功连接TCP服务");
                            io.vertx.core.net.NetSocket socket = result.result();
                            // 发送数据
                            // 构造消息
                            System.out.println("服务代理:正在构造消息");
                            ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                            ProtocolMessage.Header header = new ProtocolMessage.Header();
                            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                            header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                            header.setRequestId(IdUtil.getSnowflakeNextId());
                            protocolMessage.setHeader(header);
                            protocolMessage.setBody(rpcRequest);
                            // 编码请求
                            try {
                                System.out.println("服务代理:开始对自定义消息编码" + protocolMessage);
                                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                                System.out.println("服务代理:发送请求" + encodeBuffer);
                                socket.write(encodeBuffer);
                            } catch (IOException e) {
                                throw new RuntimeException("协议消息编码错误");
                            }
                            // 接收响应
                            socket.handler(buffer -> {
                                try {
                                    System.out.println("服务代理:收到响应=" + buffer);
                                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                    System.out.println("服务代理:响应解码成功" + rpcResponseProtocolMessage);
                                    System.out.println("服务代理:开始异步反序列化");
                                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                } catch (IOException e) {
                                    throw new RuntimeException("协议消息解码错误");
                                }
                            });
                        } else {
                            System.err.println("Failed to connect to TCP server");
                        }
                    });

            RpcResponse rpcResponse = responseFuture.get();
            System.out.println("服务代理:得到异步反序列化结果=" + rpcResponse);
            // 记得关闭连接
            netClient.close();
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
