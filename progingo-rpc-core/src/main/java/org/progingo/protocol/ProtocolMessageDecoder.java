package org.progingo.protocol;

import io.vertx.core.buffer.Buffer;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;

import java.io.IOException;

/**
 * 协议消息解码器
 */
public class ProtocolMessageDecoder {

    /**
     * 解码器
     * 将获取到的Buffer编码转换成我们的协议消息信息
     *
     * @param buffer
     * @return
     * @throws IOException
     */

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        System.out.println("TCP解码器:开始解码");
        // 分别从指定位置读出 Buffer
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        // 校验魔数
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));
        // 解决粘包问题，只读指定长度的数据
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        System.out.println("TCP解码器:获取到当前使用的序列化器为=" + serializer);
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }
        switch (messageTypeEnum) {
            case REQUEST:
                System.out.println("TCP解码器:是请求");
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                System.out.println("TCP解码器:解析出请求为=" + request);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                System.out.println("TCP解码器:是响应");
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                System.out.println("TCP解码器:解析出响应为=" + response);
                return new ProtocolMessage<>(header, response);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该消息类型");
        }
    }

}