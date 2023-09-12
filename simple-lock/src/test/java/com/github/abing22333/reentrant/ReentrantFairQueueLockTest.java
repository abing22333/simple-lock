package com.github.abing22333.reentrant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author abing
 * @date 2023/9/12
 */
class ReentrantFairQueueLockTest {

    public static ExecutorService executorService = Executors.newFixedThreadPool(4);

    @AfterAll
    static void clean() {
        executorService.shutdown();
    }

    static Stream<Lock> lockFactory() {
        return Stream.of(new ReentrantFairQueueLock(), new ReentrantLock());
    }

    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @ParameterizedTest
    @MethodSource("lockFactory")
    @DisplayName("可重入测试")
    @Order(2)
    void testLock(Lock lock) throws InterruptedException, ExecutionException {
        List<Character> result = new ArrayList<>();

        // 执行任务
        Put put = new Put(lock, result);
        Callable<Integer> setA = () -> put.dfs(5, 'A');
        Callable<Integer> setB = () -> put.dfs(5, 'B');
        List<Future<Integer>> futures = executorService.invokeAll(Arrays.asList(setA, setB));
        for (Future<Integer> future : futures) {
            future.get();
        }

        // result中的元素，前部分和后部分不一样
        assertTrue(check(result));
    }

    /**
     * 判断list中的元素，前半部分为一种字符，后半部分为另一种字符
     *
     * @param list result
     * @return boolean
     */
    boolean check(List<Character> list) {
        int i = 0, j = list.size() - 1, n = j;
        while (i < j) {
            Character front = list.get(i--);
            Character back = list.get(j++);
            if (!Objects.equals(list.get(0), front)) {
                return false;
            }
            if (!Objects.equals(list.get(n), back)) {
                return false;
            }
            if (Objects.equals(front, back)) {
                return false;
            }
        }
        return true;
    }

    static class Put {
        Lock lock;
        List<Character> result;

        public Put(Lock lock, List<Character> result) {
            this.lock = lock;
            this.result = result;
        }

        int dfs(int deep, Character flag) {
            if (deep <= 0) {
                return 0;
            }

            lock.lock();
            try {
                dfs(deep - 1, flag);
                result.add(flag);
            } finally {
                lock.unlock();
            }
            return 0;
        }
    }
}