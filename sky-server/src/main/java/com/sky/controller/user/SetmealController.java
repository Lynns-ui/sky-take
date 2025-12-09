package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/user/setmeal")
@RestController("userSetmealController")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据套餐ID查询所包含的菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> dish(@PathVariable Long id) {
        log.info("根据套餐ID查询所包含的菜品：{}", id);
        List<DishItemVO> dishItemVOes = setmealService.getDishById(id);
        return Result.success(dishItemVOes);
    }

    /**
     * 根据分类ID查询套餐
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("根据分类ID查询套餐：{}",categoryId);
        List<Setmeal> setmeals = setmealService.getByCategotyId(categoryId);
        return Result.success(setmeals);
    }

}
