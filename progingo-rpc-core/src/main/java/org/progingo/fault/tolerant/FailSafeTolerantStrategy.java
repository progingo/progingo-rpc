package org.progingo.fault.tolerant;

import lombok.extern.slf4j.Slf4j;
import org.progingo.model.RpcResponse;

import java.util.Map;

/**
 * 静默处理异常 - 容错策略
 *
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("静默处理异常", e);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setMessage(e.getMessage());
        rpcResponse.setException(e);
        return rpcResponse;
    }
}