package com.github.abing22333.lock;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于队列休眠的公平锁
 *
 * @author abing
 * @date 2023/9/3
 */
public class FairQueueLock implements Lock {

    Queue<Thread> queue = new LinkedList<>();

    /**
     * 我们的lock、unlock方法也需要加锁
     */
    private AtomicInteger guard = new AtomicInteger(0);
    private int flag = 0;

    /**
     * 当前持有锁的线程
     */
    Thread holder;

    @Override
    public void lock() {
        // lock方法中的代码需要保证原子性
        while (guard.getAndSet(1) == 1) {
        }

        if (flag == 0) {
            flag = 1;
            holder = Thread.currentThread();
            guard.set(0);
        } else {
            queue.add(Thread.currentThread());
            guard.set(0);
            LockSupport.park();
        }
    }


    @Override
    public void unlock() {
        // unlock方法中的代码需要保证原子性
        while (guard.getAndSet(1) == 1) {
        }

        if (Thread.currentThread() != holder) {
            throw new IllegalMonitorStateException();
        }


        if (queue.isEmpty()) {
            flag = 0;
            holder = null;
        } else {
            holder = queue.poll();
            LockSupport.unpark(holder);
        }
        guard.set(0);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }


    @Override
    public Condition newCondition() {
        return null;
    }
}
