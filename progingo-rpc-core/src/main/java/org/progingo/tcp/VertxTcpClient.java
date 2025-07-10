package org.progingo.tcp;

import cn.hutool.core.util.IdUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.progingo.config.RpcApplication;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.model.ServiceMetaInfo;
import org.progingo.protocol.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vertx TCP 请求客户端
 */
public class VertxTcpClient {

    /**
     * 发送请求
     *
     * @param rpcRequest
     * @param serviceMetaInfo
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws InterruptedException, ExecutionException, ExecutionException {
        // 发送 TCP 请求
        System.out.println("TCP请求客户端:");
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
                result -> {
                    System.out.println("TCP请求客户端:尝试连接TCP服务");
                    if (!result.succeeded()) {
                        System.err.println("Failed to connect to TCP server");
                        return;
                    }
                    System.out.println("TCP请求客户端:成功连接TCP服务");
                    NetSocket socket = result.result();
                    // 发送数据
                    // 构造消息
                    System.out.println("TCP请求客户端:正在构造消息");
                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                    ProtocolMessage.Header header = new ProtocolMessage.Header();
                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                    // 生成全局请求 ID
                    header.setRequestId(IdUtil.getSnowflakeNextId());
                    protocolMessage.setHeader(header);
                    protocolMessage.setBody(rpcRequest);

                    // 编码请求
                    try {
                        System.out.println("TCP请求客户端:开始对自定义消息编码" + protocolMessage);
                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                        System.out.println("TCP请求客户端:发送请求" + encodeBuffer);
                        socket.write(encodeBuffer);
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息编码错误");
                    }

                    // 接收响应
                    TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                            buffer -> {
                                try {
                                    System.out.println("TCP请求客户端:收到响应=" + buffer);
                                    ProtocolMessage<RpcResponse> rpcResponseProtocolMessage =
                                            (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                    System.out.println("TCP请求客户端:响应解码成功" + rpcResponseProtocolMessage);
                                    System.out.println("TCP请求客户端:开始异步反序列化");
                                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                } catch (IOException e) {
                                    throw new RuntimeException("协议消息解码错误");
                                }
                            }
                    );
                    socket.handler(bufferHandlerWrapper);
                });
        RpcResponse rpcResponse = responseFuture.get();
        System.out.println("TCP请求客户端:得到异步反序列化结果=" + rpcResponse);
        // 记得关闭连接
        netClient.close();
        return rpcResponse;
    }
}