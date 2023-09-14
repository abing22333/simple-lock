package com.github.abing22333.benchmark.usecase;

import com.github.abing22333.benchmark.core.Info;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * 访问共享资源的测试用例
 *
 * @author abing22333
 */
public class UseCaseLock implements Callable<Integer> {
    private final Lock lock;
    public final Info info;
    private volatile int value = 0;


    public UseCaseLock(Info info, Lock lock) {
        this.lock = lock;
        this.info = info;
    }

    @Override
    public Integer call() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < info.getLoop(); i++) {
            lock.lock();
            try {
                // 拿到锁后休眠
                if (info.getSleepTime() > 0) {
                    Thread.sleep(info.getSleepTime());
                }
                value++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        long end = System.currentTimeMillis();
        // 设置消耗时间
        info.setSpeedTime((int) (end - start - info.getSleepTime()) + info.getSpeedTime());
        return value;
    }
}