package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin/workspace")
@RestController
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 查询套餐总览
     * @return
     */
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        log.info("查询套餐总览");
        SetmealOverViewVO setmealOverView = workSpaceService.overviewSetmeals();
        return Result.success(setmealOverView);
    }

    /**
     * 查询菜品总览
     * @return
     */
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes() {
        log.info("查询套餐总览");
        DishOverViewVO overViewVO = workSpaceService.overviewDishes();
        return Result.success(overViewVO);
    }

    /**
     * 订单数据总览
     * @return
     */
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        log.info("今日订单数据总览");
        OrderOverViewVO overViewVO = workSpaceService.overviewOrders();
        return Result.success(overViewVO);
    }

    /**
     * 查询今日运行数据
     * @return
     */
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        log.info("查询今日运行数据");
        BusinessDataVO businessDataVO = workSpaceService.businessData();
        return Result.success(businessDataVO);
    }
}
