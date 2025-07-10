package org.progingo.fault.retry;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import org.progingo.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔 - 重试策略
 *
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 固定时间重试机制
     *
     * @param callable
     * @return
     * @throws ExecutionException
     * @throws RetryException
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class) //重试条件：使用 retryIfExceptionOfType 方法指定当出现 Exception 异常时重试
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS)) //重试等待策略：使用 withWaitStrategy 方法指定策略，选择 fixedWait 固定时间间隔策略
                .withStopStrategy(StopStrategies.stopAfterAttempt(10)) //重试停止策略：使用 withStopStrategy 方法指定策略，选择 stopAfterAttempt 超过最大重试次数停止
                .withRetryListener( //重试工作：使用 withRetryListener 监听重试，每次重试时，除了再次执行任务外，还能够打印当前的重试次数。
                        new RetryListener() {
                            @Override
                            public <V> void onRetry(Attempt<V> attempt) {
                                log.info("重试次数 {}", attempt.getAttemptNumber());
                            }
                        })
                .build();
        return retryer.call(callable);
    }

}