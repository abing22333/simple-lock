package com.github.abing22333.ticket;

import java.util.concurrent.Callable;

/**
 * 消费者
 * @author abing
 * @date 2023/9/3
 */
public class TicketConsumer implements Callable<Long> {
    private Ticket ticket;

    private long sum;

    public TicketConsumer(Ticket ticket) {
        this.ticket = ticket;
        this.sum = 0;
    }

    @Override
    public Long call() {

        while (true) {
            Long no = ticket.sell();
            // 售罄后停止买票
            if (no == null) {
                break;
            }
            sum++;
        }
        return sum;
    }
}
