package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private WeChatPayUtil weChatPayUtil;

    /**
     * 提交订单
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
        return new OrderSubmitVO(orders.getId(),orders.getNumber(),orders.getAmount(),orders.getOrderTime());
    }

    /**
     * 订单支付
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

        paySuccess(ordersPaymentDTO.getOrderNumber());

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
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.update(orders);
    }

    /**
     * 查询历史订单
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
     * @param id
     */
    @Transactional
    @Override
    public void insert(Long id) {
        // 1. 根据已有的单子id 重新再往orders插入同样的数据不过是 id
        OrderVO orderVO = ordersMapper.findById(id);
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderVO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setCancelTime(null);
        orders.setCancelReason(null);
        ordersMapper.insert(orders);

        // 2. 同样的具体的菜品也要插入
        List<OrderDetail> orderDetails = orderDetailMapper.findByOrderId(id);
        orderDetails.forEach(orderDetail -> {
            orderDetail.setOrderId(orders.getId());
        });
        orderDetailMapper.insert(orderDetails);
    }

    /**
     * 取消订单
     */
    @Override
    public void update(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .cancelReason("订单取消")
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .build();

        ordersMapper.update(orders);
    }
}
