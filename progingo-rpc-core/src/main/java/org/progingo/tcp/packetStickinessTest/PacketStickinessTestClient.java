package org.progingo.tcp.packetStickinessTest;

import io.vertx.core.Vertx;

/**
 * 黏包半包测试消费类
 */
public class PacketStickinessTestClient {
    public void start() {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("Connected to TCP server");
                io.vertx.core.net.NetSocket socket = result.result();
                for (int i = 0; i < 1000; i++) {
                    // 发送数据
                    socket.write("Hello, server!Hello, server!Hello, server!Hello, server!");
                }
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("Received response from server: " + buffer.toString());
                });
            } else {
                System.err.println("Failed to connect to TCP server");
            }
        });
    }

    public static void main(String[] args) {
        new PacketStickinessTestClient().start();
    }
}
