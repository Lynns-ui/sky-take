package com.sky.mapper;

import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrdersMapper {

    void insert(Orders orders);

    List<OrderVO> findByUserId(Long currentId, Integer status);

    @Select("select * from orders where id=#{id}")
    OrderVO findById(Long id);

    @Select("select * from orders where number=#{outTradeNo}")
    Orders findByNumber(String outTradeNo);

    void update(Orders orders);

}
