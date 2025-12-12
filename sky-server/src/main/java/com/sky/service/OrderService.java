package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    PageResult list(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO findById(Long id);

    void insert(Long id);

    OrderPaymentVO orderPay(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    void update(Long id);
}
