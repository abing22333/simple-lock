package com.github.abing22333.benchmark.core;

import java.util.concurrent.Callable;

/**
 * 性能测试信息
 *
 * @author abing
 * @date 2023/9/12
 */
public class Info {

    /**
     * 锁名称
     */
    private String lockName;

    /**
     * 每个线程争抢锁的次数
     */
    private Integer loop;

    /**
     * 获取锁后的休眠时间
     */
    private Integer sleepTime;

    /**
     * 任务数量
     */
    private Integer taskSize;

    /**
     * 测试任务
     */
    private Callable<Integer> callable;

    /**
     * 消耗时间
     */
    private Integer speedTime;


    public Info() {
        this.speedTime = 0;
    }

    /**
     * 浅拷贝
     *
     * @param source 目标源
     * @return info
     */
    public static Info copy(Info source) {
        Info info = new Info();
        info.setSpeedTime(source.getSpeedTime());
        info.setLockName(source.getLockName());
        info.setLoop(source.getLoop());
        info.setSleepTime(source.getSleepTime());
        info.setTaskSize(source.getTaskSize());
        info.setCallable(source.getCallable());
        return info;
    }

    public Integer getLoop() {
        return loop;
    }

    public void setLoop(Integer loop) {
        this.loop = loop;
    }

    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }

    public Integer getTaskSize() {
        return taskSize;
    }

    public void setTaskSize(Integer taskSize) {
        this.taskSize = taskSize;
    }

    public Integer getSpeedTime() {
        return speedTime;
    }

    public void setSpeedTime(Integer speedTime) {
        this.speedTime = speedTime;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public Callable<Integer> getCallable() {
        return callable;
    }

    public void setCallable(Callable<Integer> callable) {
        this.callable = callable;
    }



    @Override
    public String toString() {
        return "Info{" +
               "lockName='" + lockName + '\'' +
               ", loop=" + loop +
               ", sleepTime=" + sleepTime +
               " , taskSize=" + taskSize +
               ", speedTime=" + (speedTime / 1000.0) +
               "s}";
    }
}
