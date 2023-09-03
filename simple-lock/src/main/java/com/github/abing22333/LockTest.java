package com.github.abing22333;

import com.github.abing22333.lock.FairQueueLock;
import com.github.abing22333.lock.FairSpinLock;
import com.github.abing22333.lock.NoFairSpinLock;
import com.github.abing22333.ticket.Ticket;
import com.github.abing22333.ticket.TicketConsumer;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * 测试锁是否实现其功能
 *
 * @author abing22333
 */
public class LockTest {

    Integer consumerSize = Runtime.getRuntime().availableProcessors() * 2;
    Long ticketTotal = 1000L;
    ExecutorService executorService = Executors.newFixedThreadPool(consumerSize);

    /**
     * 单位 ms
     */
    Integer ticketSleep = 1;

    public static void main(String[] args) {
        LockTest lockTest = new LockTest();

        lockTest.testLock();
    }

    void testLock() {
        System.out.println("#############################################################################");
        System.out.printf("# LockTest: core: %d, consumerSize: %d, ticketTotal:%d, ticketSleep:%.3fs  %n", Runtime.getRuntime().availableProcessors(), consumerSize, ticketTotal, ticketSleep / 1000.1);
        System.out.println("#############################################################################");
        Ticket ticket = new Ticket(ticketTotal, new ReentrantLock(), ticketSleep);
        List<Callable<Long>> machines = createConsumer(consumerSize, ticket);
        execute(machines, "ReentrantLock");

        Ticket ticket1 = new Ticket(ticketTotal, new FairQueueLock(), ticketSleep);
        List<Callable<Long>> machines1 = createConsumer(consumerSize, ticket1);
        execute(machines1, "FairQueueLock");

        Ticket ticket3 = new Ticket(ticketTotal, new NoFairSpinLock());
        List<Callable<Long>> machines3 = createConsumer(consumerSize, ticket3);
        execute(machines3, "NoFairSpinLock");

        Ticket ticket2 = new Ticket(ticketTotal, new FairSpinLock());
        List<Callable<Long>> machines2 = createConsumer(consumerSize, ticket2);
        execute(machines2, "FairSpinLock");

        executorService.shutdown();
    }


    /**
     * 创建给定数量的消费者
     *
     * @param size   消费者数量
     * @param ticket 票
     * @return 消费者列表
     */
    List<Callable<Long>> createConsumer(long size, Ticket ticket) {
        return LongStream.range(0, size - 1)
                .mapToObj(i -> new TicketConsumer(ticket))
                .collect(Collectors.toList());
    }

    /**
     * 消费者开始买票，售罄后统计售票结果。
     *
     * @param consumer 消费者
     */
    void execute(List<Callable<Long>> consumer, String prefix) {
        int sum = 0;
        try {
            long start = System.currentTimeMillis();
            List<Future<Long>> futures = executorService.invokeAll(consumer);
            for (Future<Long> future : futures) {
                // 统计售票情况
                sum += future.get();
            }
            long end = System.currentTimeMillis();
            double seedTime = (end - start) / 1000.0;
            System.out.printf("%s:  time: %.3fs, sum: %d %n", prefix, seedTime, sum);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}


