package org.progingo.fault.retry;

import org.junit.Test;
import org.progingo.model.RpcResponse;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {

    RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();

    @Test
    public void doRetry() {
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟一下任务失败抛出异常");
            });
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}