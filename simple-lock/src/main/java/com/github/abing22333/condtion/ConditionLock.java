package com.github.abing22333.condtion;


import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * 一个实现条件变量的锁。
 *
 * @author abing
 * @date 2023/9/3
 */
public class ConditionLock implements Lock {

    Queue<Thread> queue = new LinkedList<>();

    /**
     * 当前持有锁的线程
     */
    Thread holder;

    /**
     * 我们的lock、unlock方法也需要加锁
     */
    private AtomicInteger guard = new AtomicInteger(0);

    /**
     * 标识锁被持有的情况
     */
    private int flag = 0;


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
        // 对queue的操作需要保证原子性
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
    public Condition newCondition() {

        return new SimpleCondition(this);
    }

    class SimpleCondition implements Condition {


        final Lock lock;
        Queue<Thread> conditionQueue = new LinkedList<>();

        SimpleCondition(Lock lock) {
            this.lock = lock;
        }

        @Override
        public void await() throws InterruptedException {
            if (Thread.currentThread() != holder) {
                throw new IllegalMonitorStateException();
            }
            // 将当前线程加入等待队列
            conditionQueue.add(Thread.currentThread());
            // 当前线程释放锁
            lock.unlock();
            // 当前队列阻塞
            LockSupport.park();
        }


        @Override
        public void signal() {

            if (Thread.currentThread() != holder) {
                throw new IllegalMonitorStateException();
            }

            Thread poll = conditionQueue.poll();
            if (poll != null) {
                queue.add(poll);
            }

        }

        @Override
        public void signalAll() {
            if (Thread.currentThread() != holder) {
                throw new IllegalMonitorStateException();
            }

            queue.addAll(conditionQueue);

            conditionQueue.removeIf(e -> true);
        }

        @Override
        public void awaitUninterruptibly() {

        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return false;
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
}
