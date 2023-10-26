package org.taonity.helpbot.discord.event;

import java.util.concurrent.*;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MdcAwareThreadPoolExecutor extends ThreadPoolExecutor {

    public MdcAwareThreadPoolExecutor() {
        super(10, 50, 0, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), Thread::new, new AbortPolicy());
    }

    public MdcAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        super(
                corePoolSize,
                maximumPoolSize,
                0,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                Thread::new,
                new AbortPolicy());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        MDC.clear();
        ThreadContext.clearAll();
    }
}
