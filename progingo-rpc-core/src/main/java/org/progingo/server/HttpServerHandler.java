package org.progingo.server;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.progingo.config.RpcApplication;
import org.progingo.model.RpcRequest;
import org.progingo.model.RpcResponse;
import org.progingo.registry.LocalRegistry;
import org.progingo.serializer.Serializer;
import org.progingo.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * HTTP 请求处理
 * 反序列化请求为对象，并从请求对象中获取参数。
 * 根据服务名称从本地注册器中获取到对应的服务实现类。
 * 通过反射机制调用方法，得到返回结果。
 * 对返回结果进行封装和序列化，并写入到响应中。
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        System.out.println(new Date() + "=================================");
        System.out.println("HTTP请求处理器:调用了HTTP请求处理器");
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        System.out.println("HTTP请求处理器:使用序列化器:" + serializer);

        // 记录日志
        System.out.println("HTTP请求处理器:收到请求: " + request.method() + " " + request.uri());

        // 异步处理 HTTP 请求
        request.bodyHandler(body -> {
            //反序列化拿到请求
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            // 如果请求为 null，直接返回
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(request, rpcResponse, serializer);
                return;
            }

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
            System.out.println("HTTP请求处理器:封装响应: " + rpcResponse);
            // 响应
            doResponse(request, rpcResponse, serializer);
            System.out.println(new Date() + "=============================处理结束");
        });
    }

    /**
     * 响应
     *
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            // 序列化
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
