package com.yourpackage; // 请替换为实际的包名

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 线程池队列管理类
 * 单例模式，管理一组线程池
 */
@Component
public class ThreadPoolQueueManager {

    @Value("${syn.fof.thread.pool.queue.size:10}")
    private int threadPoolQueueSize;

    @Value("${syn.fof.thread.count:10}")
    private int threadCount;

    private BlockingQueue<ExecutorService> threadPoolQueue;

    private static ThreadPoolQueueManager instance;

    @PostConstruct
    public void init() {
        threadPoolQueue = new ArrayBlockingQueue<>(threadPoolQueueSize);
        // 初始化填充线程池队列
        for (int i = 0; i < threadPoolQueueSize; i++) {
            threadPoolQueue.offer(Executors.newFixedThreadPool(threadCount));
        }
        instance = this;
    }

    /**
     * 获取单例实例
     */
    public static ThreadPoolQueueManager getInstance() {
        return instance;
    }

    /**
     * 获取线程池，如果没有可用线程池则返回null
     */
    public ExecutorService getThreadPool() {
        return threadPoolQueue.poll();
    }

    /**
     * 归还线程池到队列中
     * 在归还前会清空线程池中未执行的任务
     */
    public void returnThreadPool(ExecutorService executorService) {
        if (executorService != null && !executorService.isShutdown()) {
            if (executorService instanceof java.util.concurrent.ThreadPoolExecutor) {
                java.util.concurrent.ThreadPoolExecutor threadPoolExecutor = (java.util.concurrent.ThreadPoolExecutor) executorService;
                // 清空队列中未执行的任务
                threadPoolExecutor.getQueue().clear();
            }
            threadPoolQueue.offer(executorService);
        }
    }

    /**
     * 关闭所有线程池（应用程序关闭时调用）
     */
    public void shutdownAll() {
        ExecutorService executorService;
        while ((executorService = threadPoolQueue.poll()) != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}