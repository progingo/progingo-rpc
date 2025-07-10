package org.progingo.fault.retry;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import org.progingo.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 指数退避 - 重试策略
 */
@Slf4j
public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.exponentialWait(1L, 10L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(15))
                .withRetryListener(new RetryListener() {

                    @Override
                    public void onRetry(Attempt attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}