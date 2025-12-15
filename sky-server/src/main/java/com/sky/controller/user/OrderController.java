package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user/order")
@RestController("userOrderController")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户提交订单信息
     * @param ordersSubmitDTO   地址id/付款方式/备注/预计送达时间/配送状态/餐具数量/打包费/总金额
     * @return 给小程序返回的数据    订单Id/订单号？/订单金额/下单时间
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交的订单信息：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> orderPay(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付，订单号：{}；支付方式：{}", ordersPaymentDTO.getOrderNumber(),ordersPaymentDTO.getPayMethod());
        OrderPaymentVO orderPaymentVO = orderService.orderPay(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);

         // 模拟交易成功，修改数据库订单状态
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> list(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单，页面：{}，每页的记录数：{}，订单的状态：{}", ordersPageQueryDTO.getPage()
                ,ordersPageQueryDTO.getPageSize(), ordersPageQueryDTO.getStatus());
        PageResult pageResult = orderService.list(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情 根据订单ID返回订单的具体信息
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> findById(@PathVariable Long id) {
        log.info("查询订单：{} 的具体详细信息",id);
        OrderVO orderVO = orderService.findById(id);
        return Result.success(orderVO);
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    public Result<String> repetitionOrder(@PathVariable Long id) {
        log.info("再来一单：{}", id);
        orderService.insert(id);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable Long id) {
        log.info("订单：{} 取消", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    public Result<String> remindOrder(@PathVariable Long id) {
        log.info("订单：{}，催单", id);
        orderService.remindOrder(id);
        return Result.success();
    }
}
