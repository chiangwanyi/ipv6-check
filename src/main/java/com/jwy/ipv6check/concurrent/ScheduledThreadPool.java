package com.jwy.ipv6check.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @program: Iot_v2
 * @description:
 * @author: 蒋万艺
 * @create: 2023-11-14 09:28
 **/
public class ScheduledThreadPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledThreadPool.class);
    private final ScheduledExecutorService scheduledExecutor;
    private final String threadNamePrefix;
    private final Set<String> executingJobs = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public ScheduledThreadPool(int coreCount, String threadNamePrefix) {
        // 现有的executor初始化...
        this.threadNamePrefix = threadNamePrefix;
        this.scheduledExecutor = Executors.newScheduledThreadPool(coreCount,
                new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build());
    }

    public <V> void submit(IJob<V> job, long initialDelay, long period, TimeUnit unit) {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                executingJobs.add(job.getJobName());
                LOGGER.debug("Scheduled job started: [{}]", job.getJobName());
                V result = job.execute();
                // 根据业务需要处理结果
            } catch (Exception e) {
                LOGGER.error("Scheduled job error: ", e);
                job.onFailure(e);
            } finally {
                executingJobs.remove(job.getJobName());
            }
        }, initialDelay, period, unit);
    }

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

    // 提供关闭scheduledExecutor的方法
    public void shutdownScheduledExecutor() {
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
        }
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }
}
