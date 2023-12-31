package com.github.abing22333.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于自旋的非公平锁。
 *
 * @author abing
 * @date 2023/9/3
 */
public class NoFairSpinLock implements Lock {
    private AtomicInteger flag = new AtomicInteger(0);

    /**
     * 当前持有锁的线程
     */
    Thread holder;

    public void lock() {
        // 返回旧值，并设置新值
        while (flag.getAndSet(1) == 1) {
        }

        holder = Thread.currentThread();
    }

    public void unlock() {
        if (Thread.currentThread() != holder) {
            throw new IllegalMonitorStateException();
        }

        flag.set(0);
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public Condition newCondition() {
        return null;
    }
}
