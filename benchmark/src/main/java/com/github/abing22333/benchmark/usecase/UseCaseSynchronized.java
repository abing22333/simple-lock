package com.github.abing22333.benchmark.usecase;

import com.github.abing22333.benchmark.core.Info;

import java.util.concurrent.Callable;

/**
 * 访问共享资源的测试用例
 *
 * @author abing22333
 */
public class UseCaseSynchronized implements Callable<Integer>{
    public  final Info info;
    private final Object lock;
    private volatile int value = 0;



    public UseCaseSynchronized(Info info, Object lock) {
        this.info = info;
        this.lock = lock;
    }

    @Override
    public Integer call() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < info.getLoop(); i++) {
            synchronized (lock) {
                try {
                    // 拿到锁后休眠
                    if (info.getSleepTime() > 0) {
                        Thread.sleep(info.getSleepTime());
                    }
                    value++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        long end = System.currentTimeMillis();
        // 设置消耗时间
        info.setSpeedTime((int) (end - start - info.getSleepTime()) + info.getSpeedTime());
        return value;
    }
}