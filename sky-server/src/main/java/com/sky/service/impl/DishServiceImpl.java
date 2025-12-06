package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        List<DishVO> dishes = dishMapper.findAll(dishPageQueryDTO);
        Page<DishVO> page = (Page<DishVO>) dishes;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 1. 添加菜品
        dishMapper.insert(dish);
        Long dishId = dish.getId();

        // 2. 向dish_flavor添加相应的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishFlavorMapper.insert(flavors, dishId);
    }

    /**
     * 批量删除菜品 —— 1.起售的菜品不能删除 2.被关联的菜品不能删除 3.关联的口味也要删除
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        // 1. 起售的菜品不能删除
        for (Long id : ids) {
            DishVO dish = dishMapper.getById(id);
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 2. 被关联的菜品不能删除
        // 从表中选dish_id 在ids中，一共有几行数据，如果为0，说明无关联
        Integer counts = setmealDishMapper.count(ids);
        if (!counts.equals(0)) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除dish中的菜
        dishMapper.deleteBatch(ids);
        // 删除dish_flavor中的味道
        dishFlavorMapper.deleteBatch(ids);

    }

    /**
     * 菜品查询回显
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        // 1. 查询基础字段
        DishVO dishVO = dishMapper.getById(id);
        // 2. 查询相应的口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        // 1. 更新dish
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 2. 更新dish_flavor 先删除原有的，再添加新的
        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishFlavorMapper.deleteBatch(Arrays.asList(dish.getId()));
        dishFlavorMapper.insert(flavors, dish.getId());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }
}
