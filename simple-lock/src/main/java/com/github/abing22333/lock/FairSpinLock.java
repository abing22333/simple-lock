package com.github.abing22333.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于自旋的公平锁
 *
 * @author abing
 * @date 2023/9/3
 */
public class FairSpinLock implements Lock {
    private AtomicInteger ticket = new AtomicInteger(0);
    private AtomicInteger turn = new AtomicInteger(0);

    /**
     * 当前持有锁的线程
     */
    Thread holder;

    @Override
    public void lock() {
        // 返回旧值，并且让该值自增一
        int myTurn = ticket.getAndIncrement();

        while (turn.get() != myTurn) {
        }

        holder = Thread.currentThread();
    }

    @Override
    public void unlock() {
        if (Thread.currentThread() != holder) {
            throw new IllegalMonitorStateException();
        }

        turn.getAndIncrement();
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
