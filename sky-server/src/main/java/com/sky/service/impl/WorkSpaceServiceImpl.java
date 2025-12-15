package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public SetmealOverViewVO overviewSetmeals() {

        return setmealMapper.overview();
    }

    /**
     * 查询菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        return dishMapper.overview();
    }

    /**
     * 查询今日订单数据总览
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        LocalDate date = LocalDate.now();
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();

        return ordersMapper.overview(startTime, endTime);
    }

    /**
     * 查询今日运行数据
     * @return
     */
    @Override
    public BusinessDataVO businessData() {
        LocalDate date = LocalDate.now();
        LocalDateTime beginTime = date.atStartOfDay();
        LocalDateTime endTime = date.atStartOfDay().plusDays(1);

        // 1. 营业额
        Map<Object, Object> map = new HashMap<>();
        map.put("status", Orders.COMPLETED);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        Double turnOverSum = ordersMapper.getTurnOverSum(map);

        // 2. 有效订单数
        Integer validOrderCount = ordersMapper.getOrderCount(Orders.COMPLETED, beginTime, endTime);

        // 3. 订单完成率
        Integer orderCount = ordersMapper.getOrderCount(null, beginTime, endTime);
        Double orderCompletionRate = 0.0;

        // 4. 平均客单价
        Double uintPrice = 0.0;
        if (!orderCount.equals(0)) {
            orderCompletionRate = validOrderCount.doubleValue() / orderCount.doubleValue();
            uintPrice = turnOverSum / orderCount.doubleValue();
        }

        // 5. 新增用户数
        Integer newUsers = userMapper.getUserCount(beginTime, endTime);

        return new BusinessDataVO(turnOverSum, validOrderCount, orderCompletionRate, uintPrice, newUsers);
    }
}
