package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user/shoppingCart")
@RestController
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    public Result<String> shoppingCartAdd(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加到购物车：{}",shoppingCartDTO);
        shoppingCartService.insert(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车");
        List<ShoppingCart> list = shoppingCartService.list();
        return Result.success(list);
    }

    /**
     * 购物车减少商品
     */
    @PostMapping("/sub")
    public Result<String> subShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("从购物车中移除商品：{}", shoppingCartDTO);
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public Result<String> cleanShoppingCart() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }

}
