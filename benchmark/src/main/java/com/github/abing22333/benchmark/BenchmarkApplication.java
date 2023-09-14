package com.github.abing22333.benchmark;


import com.github.abing22333.benchmark.core.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author abing22333
 */
public class BenchmarkApplication {

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS * 2);
    public static final InputStream inputStream = BenchmarkApplication.class.getClassLoader().getResourceAsStream("InfoFactory.yaml");

    public static void main(String[] args) throws IOException {
        System.out.println("\nsimple-lock基准测试开始，请稍等（如果等待时间过长，请减少睡眠时间、任务数量和任务执行次数）。\n");

        try {
            // 从yml中加载配置
            InfoFactory infoFactory = new Yaml().loadAs(inputStream, InfoFactory.class);

            // 创建 info
            List<InfoFactory.SequenceGroup> sequenceGroups = infoFactory.create();

            // 执行测试计划并输出结果
            for (InfoFactory.SequenceGroup sequenceGroup : sequenceGroups) {
                execute(sequenceGroup);
                System.out.println(new ResultDisplay(sequenceGroup).display());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        System.out.println("simple-lock基准测试结束");
    }

    static void execute(InfoFactory.SequenceGroup sequenceGroup) throws InterruptedException, ExecutionException {

        for (Info info : sequenceGroup.getInfos()) {
            Callable<Integer> callable = info.getCallable();
            Integer runnableSize = info.getTaskSize();

            List<Callable<Integer>> callables = IntStream.range(0, runnableSize)
                    .mapToObj(e -> callable)
                    .collect(Collectors.toList());

            List<Future<Integer>> futures = executorService.invokeAll(callables);
            for (Future<Integer> future : futures) {
                future.get();
            }
        }
    }
}
