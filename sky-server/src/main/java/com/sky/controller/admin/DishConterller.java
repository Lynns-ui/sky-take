package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/dish")
@RestController
@Slf4j
public class DishConterller {

    @Autowired
    private DishService dishService;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询参数：{}", dishPageQueryDTO);

        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.save(dishDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}",ids);
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 菜品查询回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("查询菜品: {}", id);
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 设置起售/停售状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("设置菜品：{}，状态：{}",id, status);
        dishService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> getByCategoryId(Long categoryId) {
        log.info("根据分类:{}", categoryId);
        List<Dish> dishes = dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }

}
