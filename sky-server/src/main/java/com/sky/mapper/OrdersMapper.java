package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    void insert(Orders orders);

    List<OrderVO> findByUserId(Long currentId, Integer status);

    @Select("select * from orders where id=#{id}")
    OrderVO findById(Long id);

    @Select("select * from orders where number=#{outTradeNo}")
    Orders findByNumber(String outTradeNo);

    void update(Orders orders);

    List<OrderVO> findAllWithAdmin(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("SELECT" +
            " COUNT(CASE WHEN status = 2 THEN 1 END) as toBeConfirmed, " +
            " COUNT(CASE WHEN status = 3 THEN 1 END) as confirmed, " +
            " COUNT(CASE WHEN status = 4 THEN 1 END) as deliveryInProgress " +
            "FROM orders; ")
    OrderStatisticsVO statics();

    @Select("select * from orders where status=#{pendingPayment} and order_time < #{time}")
    List<Orders> getByStatusAndOrdertimeLT(Integer pendingPayment, LocalDateTime time);

    @Select("SELECT " +
            "COUNT(CASE WHEN status = 2 THEN 1 END) as waitingOrders, " +
            "COUNT(CASE WHEN status = 3 THEN 1 END) as deliveredOrders, " +
            "COUNT(CASE WHEN status = 5 THEN 1 END) as completedOrders," +
            "COUNT(CASE WHEN status = 6 THEN 1 END) as cancelledOrders, " +
            "COUNT(*) as allOrders " +
            "FROM orders " +
            "where order_time between #{startTime} and #{endTime}")
    OrderOverViewVO overview(LocalDateTime startTime, LocalDateTime endTime);

    Double getTurnOverSum(Map map);

    Integer getOrderCount(Integer status, LocalDateTime beginTime, LocalDateTime endTime);
}
