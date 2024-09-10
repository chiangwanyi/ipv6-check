package com.jwy.ipv6check.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @program: iot
 * @description: 线程池以及线程管理器
 * @author: 蒋万艺
 * @create: 2023-10-10 11:05
 **/
public class ThreadPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);
    private final ThreadPoolExecutor executor;
    private final String threadNamePrefix;
    // 用于跟踪正在执行的任务的集合
    private final Set<String> executingJobs = Collections.newSetFromMap(new ConcurrentHashMap<>());


    /**
     * @param corePoolSize     核心线程数
     * @param maximumPoolSize  最大线程数
     * @param keepAliveTime    非核心线程的空闲存活时间
     * @param unit             keepAliveTime 的时间单位
     * @param threadNamePrefix 线程名前缀
     */
    public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, String threadNamePrefix) {
        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build());
        this.threadNamePrefix = threadNamePrefix;
    }

    public ThreadPool(long keepAliveTime, TimeUnit unit, String threadNamePrefix) {
        int coreCount = Runtime.getRuntime().availableProcessors();
        int corePoolSize = coreCount * 2; // 核心线程数设置为CPU核心数的2倍
        int maxPoolSize = coreCount * 4; // 最大线程数设置为CPU核心数的4倍
        this.executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build());
        this.threadNamePrefix = threadNamePrefix;
    }

    public Map<String, Object> getBasicThreadPoolData() {
        Map<String, Object> threadPoolData = new HashMap<>();
        threadPoolData.put("核心线程数", executor.getCorePoolSize());
        threadPoolData.put("最大线程数", executor.getMaximumPoolSize());
        threadPoolData.put("当前活跃的线程数", executor.getActiveCount());
        threadPoolData.put("到目前为止已经完成的任务数量", executor.getCompletedTaskCount());
        threadPoolData.put("到目前为止提交给线程池的任务总数", executor.getTaskCount());
        threadPoolData.put("线程池是否已关闭", executor.isShutdown());
        threadPoolData.put("线程池是否已终止", executor.isTerminated());
        threadPoolData.put("当前正在执行的任务列表", getExecutingJobs());
        threadPoolData.put("当前等待执行的任务数量", getQueueSize());

        return threadPoolData;
    }

    // 获取当前正在执行的任务列表
    private Set<String> getExecutingJobs() {
        return Collections.unmodifiableSet(executingJobs);
    }

    // 获取等待执行的任务数量
    private long getQueueSize() {
        return executor.getQueue().size();
    }

    public <V> CompletableFuture<V> submit(IJob<V> job) {
        // 在执行之前，将工作添加到正在执行的集合中
        executingJobs.add(job.getJobName());

        CompletableFuture<V> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                LOGGER.info("start job:[{}]", job.getJobName());
                if (job.isRetry()) {
                    int retries = 0;
                    while (retries < job.getRetryCount()) {
                        try {
                            V result = job.execute();
                            future.complete(result);
                            return;
                        } catch (Exception e) {
                            retries++;
                            LOGGER.warn("retry job:[{}], count:{}, error:", job.getJobName(), retries, e);
                            if (retries == job.getRetryCount()) {
                                job.onFailure(e);
                                LOGGER.error("execute job:[{}] fail!, error:", job.getJobName(), e);
                                future.completeExceptionally(e);
                            }
                        }
                    }
                } else {
                    V result = job.execute();
                    future.complete(result);
                }
            } catch (Exception e) {
                job.onFailure(e);
                LOGGER.error("thread pool error: ", e);
                future.completeExceptionally(e);
            }
        });

        // 在future完成时（无论是正常还是异常完成），从集合中移除工作
        future.whenComplete((result, ex) -> executingJobs.remove(job.getJobName()));
        return future;
    }

    public <V> void submit(IJob<V> job, Consumer<V> callback) {
        submit(job).thenAccept(callback);
    }

    // 模拟ThreadFactoryBuilder，真实场景中请使用Guava等库
    private static class ThreadFactoryBuilder {
        private String nameFormat;

        public ThreadFactoryBuilder setNameFormat(String nameFormat) {
            this.nameFormat = nameFormat;
            return this;
        }

        public ThreadFactory build() {
            return r -> new Thread(r, String.format(nameFormat, Thread.currentThread().getId()));
        }
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }
}
