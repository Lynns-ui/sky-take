package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 商品信息
     * @return 订单号ID/订单号/订单金额/下单时间
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 1. 判断用户的地址是否为空，如果为空抛出错误
        AddressBook addressBook = AddressBook.builder().id(ordersSubmitDTO.getAddressBookId()).build();
        addressBook = addressBookMapper.find(addressBook);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 获取用户信息
        User user = userMapper.findById(BaseContext.getCurrentId());

        // 需要往orders插入一条记录
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);   // 待付款
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail()); // 地址是 address_book 细节地址那一栏
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());    // 收货人

        ordersMapper.insert(orders);

        // 并往order_detail插入一条或者多条记录
        // 记录该用户点的具体的菜品
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        List<OrderDetail> orderDetails = shoppingCarts.stream().map(shoppingcart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingcart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailMapper.insert(orderDetails);

        // 清空购物车
        shoppingCartMapper.delete(shoppingCart);

        // 返回结果
        return new OrderSubmitVO(orders.getId(), orders.getNumber(), orders.getAmount(), orders.getOrderTime());
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO orderPay(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 获取登录用户Id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.findById(userId);

        // 调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(ordersPaymentDTO.getOrderNumber(),
                new BigDecimal(0.01),
                "苍穹外卖订单",
                user.getOpenid());*/
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("订单已支付");
        }
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功,修改订单状态
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.findByNumber(outTradeNo);

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)             // 支付状态：支付成功，1
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.update(orders);

        // 通过websocket向客户端推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orders.getId()); // 订单id
        map.put("content", "订单号：" + ordersDB.getNumber());

        String message = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(message);
    }

    /**
     * 查询历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    @Transactional
    public PageResult list(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        List<OrderVO> orderVOS = ordersMapper.findByUserId(BaseContext.getCurrentId(), ordersPageQueryDTO.getStatus());
        orderVOS.forEach(orderVO -> {
            Long orderId = orderVO.getId();
            List<OrderDetail> orderDetails = orderDetailMapper.findByOrderId(orderId);
            orderVO.setOrderDetailList(orderDetails);
        });

        Page<OrderVO> page = (Page<OrderVO>) orderVOS;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据Id查询订单的详细信息
     */
    @Override
    public OrderVO findById(Long id) {
        // 1. 查询订单信息
        OrderVO orderVO = ordersMapper.findById(id);

        // 2. 查询订单的 菜品的详细信息
        List<OrderDetail> orderDetailList = orderDetailMapper.findByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 再来一单 根据已有单子的id
     * 查询订单细节，重新放到购物车中
     * @param id
     */
    @Transactional
    @Override
    public void insert(Long id) {
        // 根据订单id查询 具体的订单细节
        List<OrderDetail> orderDetails = orderDetailMapper.findByOrderId(id);

        // 将订单细节转化为购物车数据
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            return shoppingCart;
        }).collect(Collectors.toList());

        // 购物车添加数据
        for (ShoppingCart shoppingCart : shoppingCartList) {
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 取消订单
     */
    @Override
    public void cancelOrder(Long id) {
        OrderVO orderVO = ordersMapper.findById(id);

        Orders orders = Orders.builder()
                .id(id)
                .cancelReason("用户主动取消订单")
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .build();

        // 1. 如果已经支付，需要退款
        if (orderVO.getPayMethod().equals(Orders.PAID)) {
            orders.setPayStatus(Orders.REFUND);

            // 退款 todo...
        }
        // 2. 没有支付，支付状态设为 未支付

        ordersMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void remindOrder(Long id) {
        OrderVO orderVO = ordersMapper.findById(id);
        Map<String, Object> map = new HashMap();
        map.put("type", 2);
        map.put("orderId", orderVO.getId());
        map.put("content", "订单号：" + orderVO.getNumber());

        // 用户催单
        String message = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(message);
    }

    /* ----------------管理端 -------------- */
    /**
     * 管理端订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult adminOrderList(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        List<OrderVO> orderVOList = ordersMapper.findAllWithAdmin(ordersPageQueryDTO);
        orderVOList.forEach(orderVO -> {
            Long orderId = orderVO.getId();
            List<OrderDetail> orderDetails = orderDetailMapper.findByOrderId(orderId);

            StringBuilder orderDishes = new StringBuilder();
            orderDetails.forEach(orderDetail -> {
                orderDishes.append(orderDetail.getName())
                        .append(" * ")
                        .append(orderDetail.getNumber())
                        .append(";");
            });

            orderVO.setOrderDishes(orderDishes.toString());
        });

        Page<OrderVO> page = (Page<OrderVO>) orderVOList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 管理端各个状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO adminOrderStatics() {
        return ordersMapper.statics();
    }

    /**
     * 接单
     *
     * @param id
     */
    @Override
    public void confirmOrder(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.CONFIRMED).build();
        ordersMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO, orders);

        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND); // 退款
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);

        // TODO 给用户退款

    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void deliveryOrder(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        ordersMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void completeOrder(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        ordersMapper.update(orders);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrderOnAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO, orders);
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);

        // 给用户退款
        orders.setPayStatus(Orders.REFUND);

        ordersMapper.update(orders);
    }
}
