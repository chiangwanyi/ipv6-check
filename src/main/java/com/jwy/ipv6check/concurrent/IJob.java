package com.jwy.ipv6check.concurrent;

/**
 * @program: iot
 * @description: 任务接口，所有异步任务都需要实现这个接口
 * @author: 蒋万艺
 * @create: 2023-10-12 13:48
 **/
public interface IJob<V> {
    String getJobName();

    boolean isRetry();

    int getRetryCount();

    V execute() throws Exception;

    default void onFailure(Exception e) {
    }
}
