package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 每分钟检测一次是否有超时的订单
     */
    @Scheduled(cron = "0 * * * * *")
    public void processTimeOutOrder() {
        log.info("处理支付超时订单：{}",new Date());

        // 当前时间 - 15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> ordersList = ordersMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                ordersMapper.update(orders);
            });
        }
    }

    /**
     * 每天一点检查派送状态
     * 仍处于派送中的订单，那么该订单默认为已经成功
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder() {
        log.info("处理还处于派送中的订单: {}", new Date());

        // 当前时间 - 1小时  00:00之前的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> ordersList = ordersMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                ordersMapper.update(orders);
            });
        }
    }

}
