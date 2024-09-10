package com.jwy.ipv6check.init;

import com.jwy.ipv6check.concurrent.ScheduledThreadPool;
import com.jwy.ipv6check.job.Ipv6CheckJobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

@Configuration
@Order(1)
public class InitializerRunner implements ApplicationRunner {
    public static final ScheduledThreadPool SCHEDULED_THREAD_POOL = new ScheduledThreadPool(4, "scheduled");

    @Autowired
    private Ipv6CheckJobHandler ipv6CheckJobHandler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        SCHEDULED_THREAD_POOL.submit(ipv6CheckJobHandler, 0, 5, TimeUnit.MINUTES);
    }
}
