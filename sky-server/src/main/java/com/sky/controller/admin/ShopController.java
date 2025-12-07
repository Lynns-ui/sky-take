package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/shop")
@RestController
@Slf4j
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 获取营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        log.info("获取营业状态");
        Integer status = shopService.getStatus();
        return Result.success(status);
    }

    /**
     * 设置营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result<String> setStatus(@PathVariable Integer status) {
        log.info("设置营业状态:{}",status == 1 ? "营业中" : "打烊中");
        shopService.setStatus(status);
        return Result.success();
    }

}
