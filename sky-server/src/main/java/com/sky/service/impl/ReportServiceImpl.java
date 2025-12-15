package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * begin - end 之间的营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.atStartOfDay().plusDays(1);

            Map<Object, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Double turnover = ordersMapper.getTurnOverSum(map);
            turnoverList.add(turnover);
        }

        // stream 流的形式处理
        String dateString = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        String turnoverString = turnoverList.stream().map(turnover -> {
            return turnover == null ? "0.0" : turnover.toString();
        }).collect(Collectors.joining(","));

        return new TurnoverReportVO(dateString, turnoverString);
    }

    /**
     * begin - end 之间的用户统计详情
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.atStartOfDay().plusDays(1);
            // 当天以前的用户总量 [0, endTime]
            Integer userCount = userMapper.getUserCount(null, endTime);
            totalUserList.add(userCount);

            // 当天创建的用户数，那就是用户新增 [begin, endTime]
            Integer newUserCount = userMapper.getUserCount(beginTime, endTime);
            newUserList.add(newUserCount);
        }

        // stream 流的形式处理
        String dateString = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        String totalUserString = totalUserList.stream()
                .map(totalUser -> {
                    return totalUser == null ? "0" : totalUser.toString();
                }).collect(Collectors.joining(","));

        String newUserString = newUserList.stream()
                .map(newUser -> {
                    return newUser == null ? "0" : newUser.toString();
                }).collect(Collectors.joining(","));

        return new UserReportVO(dateString, totalUserString, newUserString);
    }

    @Override
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>(); // 每日有效订单数

        // 每日...
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.atStartOfDay().plusDays(1);

            // 每日的有效订单数
            Integer validOrderCount = ordersMapper.getOrderCount(Orders.COMPLETED, beginTime, endTime);
            validOrderCountList.add(validOrderCount);

            // 每日的订单总数
            Integer orderCount = ordersMapper.getOrderCount(null, beginTime, endTime);
            orderCountList.add(orderCount);
        }

        // 总数
        Integer totalOrderCount = ordersMapper.getOrderCount(null, null, null);
        // 有效订单总数
        Integer validOrderCount = ordersMapper.getOrderCount(Orders.COMPLETED, null, null);
        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount.doubleValue() != 0.0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount.doubleValue();
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,"," ))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * begin - end 前top10的菜品/套餐
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO salesTop10Report(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList =  orderDetailMapper.getTop10(beginTime, endTime);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }
}
