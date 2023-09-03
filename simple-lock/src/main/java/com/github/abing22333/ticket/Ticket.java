package com.github.abing22333.ticket;

import java.util.concurrent.locks.Lock;

/**
 * 车票
 *
 * @author abing22333
 */
public class Ticket {
    /**
     * 票的总量
     */
    private volatile long total;

    /**
     * 锁
     */
    private final Lock lock;

    /**
     * 网络延迟，业务处理时间
     */
    private final int sleep;

    public Ticket(long ticket, Lock lock) {
        this.total = ticket;
        this.lock = lock;
        this.sleep = 1;
    }

    public Ticket(long ticket, Lock lock, int sleep) {
        this.total = ticket;
        this.lock = lock;
        this.sleep = sleep;
    }

    public boolean isSellOut() {
        return total <= 0;
    }

    public Long sell() {
        lock.lock();
        try {
            if (isSellOut()) {
                return null;
            }
            // 模拟网络延迟，业务处理时间
            Thread.sleep(sleep);

            long i = total;
            total = total - 1;
            return i;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}