package com.github.abing22333.lock;

import com.github.abing22333.condtion.ConditionLock;
import com.github.abing22333.reentrant.ReentrantFairQueueLock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


/**
 * 测试自定义锁的功能
 *
 * @author abing
 * @date 2023/9/10
 */
class LockTest {

    public static ExecutorService executorService = Executors.newFixedThreadPool(4);
    int num = 500;

    @AfterAll
    static void clean() {
        executorService.shutdown();
    }

    static Stream<Lock> lockFactory() {
        return Stream.of(
                new FairQueueLock()
                , new NoFairSpinLock()
                , new FairSpinLock()
                , new ConditionLock()
                , new ReentrantFairQueueLock()
                , new ReentrantLock());
    }
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @Test
    @DisplayName("没有用的锁")
    @Order(1)
    void testNoLock() throws InterruptedException {
        Lock nolock = new UselessLock();

        Set<Integer> result = execute(nolock);
        assertNotEquals(result.size(), num);
    }

    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @ParameterizedTest
    @MethodSource("lockFactory")
    @DisplayName("lock,unlock方法的正常处理")
    @Order(2)
    void testLock(Lock lock) throws InterruptedException {
        Set<Integer> result = execute(lock);
        assertEquals(result.size(), num);
    }

    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @ParameterizedTest
    @MethodSource("lockFactory")
    @DisplayName("unlock方法的异常处理")
    @Order(3)
    void testUnlock(Lock lock) {

        assertThrows(IllegalMonitorStateException.class, () -> {
            execute(lock);
            Thread.sleep(50);
            // 非持有锁的线程，调用unlock方法，会抛IllegalMonitorStateException
            lock.unlock();
        });
    }

    /**
     * 创建 锁的测试场景，执行并收集结果
     *
     * @param lock lock
     * @return 收集的结果
     * @throws InterruptedException InterruptedException
     */
    Set<Integer> execute(Lock lock) throws InterruptedException {
        Set<Integer> result = new HashSet<>(num);

        // 创建callable
        AccessSharedResource callable = new AccessSharedResource(lock, num);
        List<AccessSharedResource> callables = IntStream.range(0, 4)
                .mapToObj(i -> callable)
                .collect(Collectors.toList());

        // 多线程执行任务
        List<Future<Set<Integer>>> futures = executorService.invokeAll(callables);

        // 统计每个任务的结果
        futures.stream()
                .map(LockTest::apply)
                .forEach(result::addAll);

        return result;
    }


    /**
     * 访问共享资源的测试用例
     */
    static class AccessSharedResource implements Callable<Set<Integer>> {
        /**
         * 共享资源
         */
        private volatile int num;

        private final Lock lock;

        public AccessSharedResource(Lock lock, int num) {
            this.lock = lock;
            this.num = num;
        }

        @Override
        public Set<Integer> call() {

            Set<Integer> list = new HashSet<>(num);

            while (true) {
                lock.lock();
                try {
                    if (num <= 0) {
                        break;
                    }
                    // 加剧争抢锁的频率
                    Thread.sleep(1);
                    list.add(num--);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
            return list;
        }
    }

    /**
     * 封装Future.get()的异常
     *
     * @param future future
     * @return Set<Integer>
     */
    private static Set<Integer> apply(Future<Set<Integer>> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 没有作用的锁
     */
    private static class UselessLock implements Lock {
        @Override
        public void lock() {

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

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }
}