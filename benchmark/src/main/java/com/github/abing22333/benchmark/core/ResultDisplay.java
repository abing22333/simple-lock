package com.github.abing22333.benchmark.core;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;

/**
 * 结果展示
 *
 * @author abing
 * @date 2023/9/14
 */
public class ResultDisplay {
    InfoFactory.SequenceGroup sequenceGroup;


    private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

    public ResultDisplay(InfoFactory.SequenceGroup sequenceGroup) {
        this.sequenceGroup = sequenceGroup;
    }


    public String display() {

        List<SortSequence> sortSequence = getSortSequence();

        // 分组
        Map<Integer, Map<Integer, Map<Integer, List<Info>>>> infoMap = sequenceGroup.infos.stream().collect(
                groupingBy(sortSequence.get(0).getGroupFunction(),
                        groupingBy(sortSequence.get(1).getGroupFunction(),
                                groupingBy(sortSequence.get(2).getGroupFunction()))));

        StringBuilder sb = new StringBuilder();

        // 标题
        sb.append(sequenceGroup.title).append(" (cpu可用核数: ").append(CPU_CORE).append(")");

        for (Map.Entry<Integer, Map<Integer, Map<Integer, List<Info>>>> entry1 : infoMap.entrySet()) {
            for (Map.Entry<Integer, Map<Integer, List<Info>>> entry2 : entry1.getValue().entrySet()) {
                // 表头
                sb.append(System.lineSeparator()).append(System.lineSeparator());
                sb.append(String.format("%10s%10s%10s |", sortSequence.get(0).getName(), sortSequence.get(1).getName(), sortSequence.get(2).getName()));
                sequenceGroup.getLocks().forEach(lockEnum -> sb.append(String.format("%16s", lockEnum.getName())));

                // 内容
                for (Map.Entry<Integer, List<Info>> entry3 : entry2.getValue().entrySet()) {
                    sb.append(System.lineSeparator());
                    sb.append(String.format("%10d%10d%10d |", entry1.getKey(), entry2.getKey(), entry3.getKey()));
                    entry3.getValue().forEach(info -> sb.append(String.format("%15.4fs", info.getSpeedTime() / 1000.0)));
                }
            }
        }
        return sb.append(System.lineSeparator()).toString();
    }

    List<SortSequence> getSortSequence() {

        List<Function<Info, Integer>> groupFunctions = Arrays.asList(Info::getLoop, Info::getSleepTime, Info::getTaskSize);
        List<String> tableName = Arrays.asList("loop", "sleepTime", "taskSize");
        // sequence[0]=loop, sequence[1]=sleepTime, sequence[2]=taskSize,
        List<InfoFactory.ArithmeticSequence> sequneceList = sequenceGroup.getSequneceList();

        List<SortSequence> sortSequences = new ArrayList<>();
        for (int i = 0; i < sequneceList.size(); i++) {
            SortSequence sortSequence = new SortSequence(sequneceList.get(i), groupFunctions.get(i), tableName.get(i));
            sortSequences.add(sortSequence);
        }

        sortSequences.sort(SortSequence::compareTo);
        return sortSequences;
    }

    static class SortSequence implements Comparable<SortSequence> {
        InfoFactory.ArithmeticSequence sequence;

        Function<Info, Integer> groupFunction;

        String name;

        public InfoFactory.ArithmeticSequence getSequence() {
            return sequence;
        }

        public void setSequence(InfoFactory.ArithmeticSequence sequence) {
            this.sequence = sequence;
        }

        public Function<Info, Integer> getGroupFunction() {
            return groupFunction;
        }

        public void setGroupFunction(Function<Info, Integer> groupFunction) {
            this.groupFunction = groupFunction;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SortSequence(InfoFactory.ArithmeticSequence sequence, Function<Info, Integer> groupFunction, String name) {
            this.sequence = sequence;
            this.groupFunction = groupFunction;
            this.name = name;
        }


        @Override
        public int compareTo(SortSequence target) {
            if (this.getSequence().getN().equals(target.getSequence().getN())) {
                return this.getName().compareTo(target.getName());
            }
            return this.getSequence().getN().compareTo(target.getSequence().getN());
        }

        @Override
        public String toString() {
            return "SortSequence{" +
                   "sequence=" + sequence +
                   ", groupFunction=" + groupFunction +
                   ", name='" + name + '\'' +
                   '}';
        }
    }
}
