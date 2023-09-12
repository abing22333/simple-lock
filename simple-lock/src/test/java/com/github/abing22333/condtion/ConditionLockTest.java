package com.github.abing22333.condtion;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author abing
 * @date 2023/9/12
 */
class ConditionLockTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    static Integer time = 20;


    @AfterAll
    static void clean() {
        executorService.shutdown();
    }


    static Stream<Lock> lockFactory() {
        return Stream.of(new ConditionLock(), new ReentrantLock());
    }

    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @ParameterizedTest
    @MethodSource("lockFactory")
    @DisplayName("测试condition")
    public void test(Lock lock) throws InterruptedException, ExecutionException {
        LinkedList<Character> linkedList = new LinkedList<>();

        // 执行任务
        PutaPutb putaPutb = new PutaPutb(lock, linkedList);
        List<Future<Object>> futures = executorService.invokeAll(Arrays.asList(putaPutb::putB, putaPutb::putA));
        for (Future<Object> future : futures) {
            future.get();
        }

        //  判断list中的元素顺序
        assertTrue(check(linkedList));
    }

    /**
     * 检测列表中的元素是否以‘A,B’的顺序交替出现
     *
     * @param list 目标列表
     * @return true：顺序正常
     */
    boolean check(List<Character> list) {
        boolean isA = true;
        for (Character character : list) {
            if (character != (isA ? 'A' : 'B')) {
                return false;
            }
            isA = !isA;
        }
        return true;
    }

    static class PutaPutb {
        Lock lock;
        Condition a;
        Condition b;
        LinkedList<Character> linkedList;

        public PutaPutb(Lock lock, LinkedList<Character> linkedList) {
            this.lock = Objects.requireNonNull(lock);
            this.a = lock.newCondition();
            this.b = lock.newCondition();
            this.linkedList = linkedList;
        }

        int putA() {
            for (int i = 0; i < time; i++) {

                lock.lock();
                try {
                    while (lastIsA()) {
                        a.await();
                    }
                    linkedList.addLast('A');
                    b.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
            return 0;
        }

        int putB() {
            for (int i = 0; i < time; i++) {

                lock.lock();
                try {
                    while (!lastIsA()) {
                        b.await();
                    }
                    linkedList.addLast('B');
                    a.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
            return 0;
        }

        /**
         * 判断最新的内容是不是A
         *
         * @return true 最新的内容是A
         */
        boolean lastIsA() {
            if (linkedList.isEmpty()) {
                return false;
            }
            return linkedList.getLast().equals('A');
        }
    }
}