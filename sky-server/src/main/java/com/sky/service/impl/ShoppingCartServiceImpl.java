package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void insert(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 1. 从 shopping_cart 查userId/dish_id/setmeal_id下是否有商品
        // shopping_cart表：用户点一个餐就向表插入数据，可以点菜品/可以点套餐
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        // 如果有商品。更新数量
        if (shoppingCarts != null && shoppingCarts.size() == 1) {
            shoppingCart = shoppingCarts.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1); // 增加商品数量
            // 此时表中是有数量的，并且id是唯一的，直接根据id更新商品数量
            shoppingCartMapper.updateNumberById(shoppingCart);
        } else {
            // 没有商品，直接点餐
            // 点的是菜品，那么name就是商品的菜品
            if (shoppingCart.getDishId() != null) {
                DishVO dishVO = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setName(dishVO.getName());
                shoppingCart.setImage(dishVO.getImage());
                shoppingCart.setAmount(dishVO.getPrice());
            } else if (shoppingCart.getSetmealId() != null) {
                SetmealVO setmealVO = setmealMapper.select(shoppingCart.getSetmealId());
                shoppingCart.setName(setmealVO.getName());
                shoppingCart.setImage(setmealVO.getImage());
                shoppingCart.setAmount(setmealVO.getPrice());
            }
            shoppingCart.setDishFlavor(shoppingCart.getDishFlavor());
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        return shoppingCartMapper.list(null);
    }

    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        // 1. 表中查询是否有数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        shoppingCart = shoppingCarts.get(0);
        if (shoppingCart.getNumber().equals(1)) {
            shoppingCartMapper.delete(shoppingCart);
            return;
        }
        shoppingCart.setNumber(shoppingCart.getNumber() - 1);
        shoppingCartMapper.updateNumberById(shoppingCart);
    }

    @Override
    public void clean() {
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        shoppingCartMapper.delete(shoppingCart);
    }
}
