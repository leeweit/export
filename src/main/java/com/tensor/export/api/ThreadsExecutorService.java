package com.tensor.export.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wei.li
 * @version 1.0
 * Create Time 2020/3/25 14:47
 */
public class ThreadsExecutorService {

    private static final Logger log = LoggerFactory.getLogger(ThreadsExecutorService.class);

    private static final long KEEP_ALIVE_TIME = 3L;
    private static ArrayBlockingQueue<Runnable> exportQueue = new ArrayBlockingQueue<>(100);
    private static ExecutorService exportExecutorService;
    private static ThreadsExecutorService threadsExecutorService = null;

    private ThreadsExecutorService() {
    }

    public enum Singleton {
        INSTANCE;

        Singleton() {
            if (threadsExecutorService == null) {
                threadsExecutorService = new ThreadsExecutorService();
                exportExecutorService = new ThreadPoolExecutor(1, 1, KEEP_ALIVE_TIME, TimeUnit.SECONDS, exportQueue,
                        new ThreadFactoryN("export"));
            }
        }

        public ThreadsExecutorService getInstance() {
            return threadsExecutorService;
        }
    }

    @PreDestroy
    public void destroy() {
        exportExecutorService.shutdownNow();
    }

    public void execute(Runnable runnable) {
        exportExecutorService.execute(runnable);
    }

    private static class ThreadFactoryN implements ThreadFactory {

        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ThreadFactoryN(String threadName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = threadName + "-thread-pool-" +
                    POOL_NUMBER.getAndIncrement();

            log.info("初始化自定义线程池[{}]成功", namePrefix);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
