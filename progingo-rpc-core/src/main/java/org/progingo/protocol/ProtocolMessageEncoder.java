package org.progingo.protocol;

import io.vertx.core.buffer.Buffer;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;

import java.io.IOException;

public class ProtocolMessageEncoder {

    /**
     * 编码器
     * 将协议消息信息编入Buffer
     *
     * @param protocolMessage
     * @return
     * @throws IOException
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        System.out.println("TCP编码器:开始编码");
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            System.out.println("TCP编码器:消息为空");
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();
        // 依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        // 获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        System.out.println("TCP编码器:获取到当前使用的序列化器为=" + serializer);
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        // 写入 body 长度和数据
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        System.out.println("TCP编码器:编码完成");
        return buffer;
    }
}