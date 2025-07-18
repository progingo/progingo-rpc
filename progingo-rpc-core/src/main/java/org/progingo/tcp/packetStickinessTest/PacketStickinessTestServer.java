package org.progingo.tcp.packetStickinessTest;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.progingo.server.HttpServer;

/**
 * 黏包半包测试服务类
 */
public class PacketStickinessTestServer implements HttpServer {
        @Override
        public void doStart(int port) {
            // 创建 Vert.x 实例
            Vertx vertx = Vertx.vertx();

            // 创建 TCP 服务器
            NetServer server = vertx.createNetServer();

            // 处理请求
//        server.connectHandler(new TcpServerHandler());
            server.connectHandler(socket -> {
                socket.handler(buffer -> {
                    String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
                    int messageLength = testMessage.getBytes().length;
                    if (buffer.getBytes().length < messageLength) {
                        System.out.println("半包, length = " + buffer.getBytes().length);
                        System.out.println(new String(buffer.getBytes(0, buffer.getBytes().length)));
                        return;
                    }
                    if (buffer.getBytes().length > messageLength) {
                        System.out.println("粘包, length = " + buffer.getBytes().length);
                        System.out.println(new String(buffer.getBytes(0, buffer.getBytes().length)));
                        return;
                    }
                    String str = new String(buffer.getBytes(0, messageLength));
                    //System.out.println(str);
                    if (testMessage.equals(str)) {
                        System.out.println("good");
                    }
                });
            });

            // 启动 TCP 服务器并监听指定端口
            server.listen(port, result -> {
                if (result.succeeded()) {
                    System.out.println("TCP server started on port " + port);
                    //log.info("TCP server started on port " + port);
                } else {
                    System.out.println("Failed to start TCP server: " + result.cause());
                    //log.info("Failed to start TCP server: " + result.cause());
                }
            });
        }

        public static void main(String[] args) {
            new PacketStickinessTestServer().doStart(8888);
        }
}
