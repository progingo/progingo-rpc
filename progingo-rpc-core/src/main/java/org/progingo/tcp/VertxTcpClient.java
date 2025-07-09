package org.progingo.tcp;

import io.vertx.core.Vertx;

/**
 * TCP客户端
 */
public class VertxTcpClient {

    public void start() {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("连接TCP服务");
                io.vertx.core.net.NetSocket socket = result.result();
                // 发送数据
                socket.write("Hello, progingo!");
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("收到来自服务端的响应: " + buffer.toString());
                });
            } else {
                System.err.println("无法连接服务端");
            }
        });

    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}