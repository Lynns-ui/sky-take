package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/admin/order")
@RestController
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> orderList(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理端查询订单:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.adminOrderList(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> orderStatistics() {
        log.info("各个状态订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.adminOrderStatics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查看订单详情
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> findOrdersById(@PathVariable Long id) {
        log.info("查看订单：{} 详情", id);
        OrderVO orderVO = orderService.findById(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result<String> confirmOrder(@RequestBody Orders orders) {
        log.info("商家接单：{}", orders.getId());
        orderService.confirmOrder(orders.getId());
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    public Result<String> rejectionOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("商家拒绝接单：{}, 拒绝原因：{}", ordersRejectionDTO.getId(), ordersRejectionDTO.getRejectionReason());
        orderService.rejectionOrder(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result<String> deliveryOrder(@PathVariable Long id) {
        log.info("商家派送订单：{}", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    public Result<String> completeOrder(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        orderService.completeOrder(id);
        return Result.success();
    }

    /**
     *  取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    public Result<String> cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("商家取消订单：{}，取消原因：{}", ordersCancelDTO.getId(), ordersCancelDTO.getCancelReason());
        orderService.cancelOrderOnAdmin(ordersCancelDTO);
        return Result.success();
    }
}
