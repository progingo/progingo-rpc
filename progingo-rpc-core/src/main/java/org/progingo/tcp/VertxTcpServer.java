package org.progingo.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.progingo.server.HttpServer;

public class VertxTcpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        // 创建 TCP 服务器
        NetServer server = vertx.createNetServer();

        // 处理请求
        server.connectHandler(new TcpServerHandler());

        // 启动 TCP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP服务启动端口:" + port);
            } else {
                System.err.println("TCP服务启动失败: " + result.cause());
            }
        });
    }
}