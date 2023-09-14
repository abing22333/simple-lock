package com.github.abing22333.benchmark.core;

import com.github.abing22333.benchmark.usecase.UseCaseLock;
import com.github.abing22333.benchmark.usecase.UseCaseSynchronized;
import com.github.abing22333.condtion.ConditionLock;
import com.github.abing22333.lock.FairQueueLock;
import com.github.abing22333.lock.FairSpinLock;
import com.github.abing22333.lock.NoFairSpinLock;
import com.github.abing22333.reentrant.ReentrantFairQueueLock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author abing
 * @date 2023/9/14
 */
public class InfoFactory {

    List<SequenceGroup> sequenceGroups;


    /**
     * 创建infos
     *
     * @return list
     */
    public List<SequenceGroup> create() {

        for (SequenceGroup sequenceGroup : sequenceGroups) {
            List<Info> infos = new ArrayList<>();
            ArithmeticSequence loopSeq = sequenceGroup.getLoop();
            ArithmeticSequence sleepTimeSeq = sequenceGroup.getSleepTime();
            ArithmeticSequence taskSizeSeq = sequenceGroup.getTaskSize();

            for (int i = 0, loop = loopSeq.getA1(); i < loopSeq.getN(); i++, loop += loopSeq.getD()) {

                for (int j = 0, sleepTime = sleepTimeSeq.getA1(); j < sleepTimeSeq.getN(); j++, sleepTime += sleepTimeSeq.getD()) {

                    for (int k = 0, taskSize = taskSizeSeq.getA1(); k < taskSizeSeq.getN(); k++, taskSize += taskSizeSeq.getD()) {

                        for (LockEnum lockEnum : sequenceGroup.getLocks()) {
                            Info info = new Info();
                            info.setLoop(loop);
                            info.setSleepTime(sleepTime);
                            info.setTaskSize(taskSize);
                            info.setLockName(lockEnum.getName());
                            Lock lock = lockEnum.getLockFactory().get();
                            Callable<Integer> callable = lockEnum.getUseCaseFactory().apply(info, lock);
                            info.setCallable(callable);
                            infos.add(info);
                        }
                    }
                }
            }
            sequenceGroup.setInfos(infos);
        }

        return getSequenceGroups();
    }

    public List<SequenceGroup> getSequenceGroups() {
        return sequenceGroups;
    }

    public void setSequenceGroups(List<SequenceGroup> sequenceGroups) {
        this.sequenceGroups = sequenceGroups;
    }

    public static class SequenceGroup {
        ArithmeticSequence loop;
        ArithmeticSequence sleepTime;
        ArithmeticSequence taskSize;

        List<Info> infos;

        String title = "";

        List<LockEnum> locks;

        public List<ArithmeticSequence> getSequneceList(){
            return Arrays.asList(loop, sleepTime, taskSize);
        }

        public ArithmeticSequence getLoop() {
            return loop;
        }

        public void setLoop(ArithmeticSequence loop) {
            this.loop = loop;
        }

        public ArithmeticSequence getSleepTime() {
            return sleepTime;
        }

        public void setSleepTime(ArithmeticSequence sleepTime) {
            this.sleepTime = sleepTime;
        }

        public ArithmeticSequence getTaskSize() {
            return taskSize;
        }

        public void setTaskSize(ArithmeticSequence taskSize) {
            this.taskSize = taskSize;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Info> getInfos() {
            return infos;
        }

        public void setInfos(List<Info> infos) {
            this.infos = infos;
        }

        public List<LockEnum> getLocks() {
            if (locks == null || locks.isEmpty()) {
                return Arrays.asList(LockEnum.values());
            }
            return locks;
        }

        public void setLocks(List<LockEnum> locks) {
            this.locks = locks;
        }

        @Override
        public String toString() {
            return "SequenceGroup{" +
                   "loop=" + loop +
                   ", sleepTime=" + sleepTime +
                   ", taskSize=" + taskSize +
                   ", title='" + title + '\'' +
                   ", locks=" + locks +
                   '}';
        }
    }

    public static class ArithmeticSequence {
        /**
         * 初始值
         */
        private Integer a1;

        /**
         * 公差
         */
        private Integer d = 0;

        /**
         * 项数
         */
        private Integer n = 1;

        public Integer getA1() {
            return a1;
        }


        public Integer getD() {
            return d;
        }

        public Integer getN() {
            return n;
        }

        public void setA1(Integer a1) {
            this.a1 = a1;
        }

        public void setD(Integer d) {
            this.d = d;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        @Override
        public String toString() {
            return "ArithmeticSequence{" +
                   "a1=" + a1 +
                   ", d=" + d +
                   ", n=" + n +
                   '}';
        }


    }

    /**
     * lock, userCase Enum
     *
     * @author abing
     * @date 2023/9/12
     */
    public enum LockEnum {

        /**
         * synchronized
         */
        SYNCHRONIZED("synchronized", ReentrantLock::new, UseCaseSynchronized::new),

        /**
         * java.util.concurrent下的锁
         */
        REENTRANT_LOCK("ReentrantLock", ReentrantLock::new, UseCaseLock::new),

        /**
         * 基于队列休眠的公平锁
         */
        FAIR_QUEUE_LOCK("FairQueueLock", FairQueueLock::new, UseCaseLock::new),

        /**
         * 基于自旋的公平锁
         */
        FAIR_SPIN_LOCK("FairSpinLock", FairSpinLock::new, UseCaseLock::new),

        /**
         * 基于自旋的非公平锁
         */
        NO_FAIR_SPIN_LOCK("NoFairSpinLock", NoFairSpinLock::new, UseCaseLock::new),

        /**
         * 基于队列休眠的可重入的公平的锁
         */
        REENTRANT_FAIR_QUEUE_LOCK("MyReentrantLock", ReentrantFairQueueLock::new, UseCaseLock::new),

        /**
         * 基于队列休眠的公平的带有condition的锁
         */
        CONDITION_LOCK("ConditionLock", ConditionLock::new, UseCaseLock::new),

        ;

        LockEnum(String name, Supplier<Lock> lockFactory, BiFunction<Info, Lock, Callable<Integer>> useCaseFactory) {
            this.name = name;
            this.lockFactory = lockFactory;
            this.useCaseFactory = useCaseFactory;
        }

        /**
         * 锁名称
         */
        private final String name;

        /**
         * lock是测试用例
         */

        private final Supplier<Lock> lockFactory;

        /**
         * 测试用例的factory
         */
        private final BiFunction<Info, Lock, Callable<Integer>> useCaseFactory;

        public String getName() {
            return name;
        }

        public Supplier<Lock> getLockFactory() {
            return lockFactory;
        }

        public BiFunction<Info, Lock, Callable<Integer>> getUseCaseFactory() {
            return useCaseFactory;
        }
    }

}
