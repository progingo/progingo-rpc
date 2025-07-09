package org.progingo.tcp;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.protocol.ProtocolMessage;
import org.progingo.protocol.ProtocolMessageDecoder;
import org.progingo.protocol.ProtocolMessageEncoder;
import org.progingo.protocol.ProtocolMessageTypeEnum;
import org.progingo.registry.LocalRegistry;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * TCP请求处理器
 * 收到请求(Buffer类型)后调用解码器得到我们自定义的消息结构ProtocolMessage
 * 获取到请求体，同样通过反射调用方法
 * 将结果响应封装成自定义的消息结构ProtocolMessage
 * 通过编码器将结果编写成Buffer类型
 */
public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {
        System.out.println(new Date() + "=================================");
        System.out.println("TCP请求处理器:调用了TCP请求处理器");
        // 处理连接
        netSocket.handler(buffer -> {
            // 接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            System.out.println("TCP请求处理器:封装响应:" + rpcResponse);
            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
            System.out.println(new Date() + "=============================处理结束");
        });
    }
}