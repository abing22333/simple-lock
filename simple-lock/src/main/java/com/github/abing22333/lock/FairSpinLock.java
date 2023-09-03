package com.github.abing22333.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 一个公平的自旋锁。
 *
 * @author abing
 * @date 2023/9/3
 */
public class FairSpinLock implements Lock {
    private AtomicInteger ticket = new AtomicInteger(0);
    private AtomicInteger turn = new AtomicInteger(0);

    @Override
    public void lock() {
        // 返回旧值，并且让该值自增一
        int myTurn = ticket.getAndIncrement();

        while (turn.get() != myTurn) {

        }
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
    public void unlock() {
        turn.getAndIncrement();
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
