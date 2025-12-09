package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        List<SetmealVO> setmeals = setmealMapper.getAll(setmealPageQueryDTO);
        Page<SetmealVO> page = (Page<SetmealVO>) setmeals;

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {
        // 1. 先setmeal表中添加基础字段
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();

        // 2. 将套餐联系的菜品放到setmeal_dish中
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishMapper.insert(setmealDishes, id);
    }

    /**
     * 查询回显
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 1. 基础字段
        SetmealVO setmealVO = setmealMapper.select(id);

        // 2. 套餐联系的菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.select(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 批量删除 —— 起售的套餐不能删
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        // 1. 处于起售状态的不能删
        for (Long id : ids) {
            SetmealVO setmealVO = setmealMapper.select(id);
            if (setmealVO.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        setmealMapper.delete(ids);
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 修改套餐
     * @param setmealVO
     */
    @Transactional
    @Override
    public void update(SetmealVO setmealVO) {
        // 1. 修改setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealVO, setmeal);
        setmealMapper.update(setmeal);

        // 2. 修改setmeal_dish表
        List<SetmealDish> setmealDishes = setmealVO.getSetmealDishes();
        setmealDishMapper.deleteBatch(Arrays.asList(setmealVO.getId()));
        setmealDishMapper.insert(setmealDishes, setmealVO.getId());
    }

    /**
     * 启售/停售套餐 —— 套餐内包含停售的菜品不能起售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        if (status.equals(StatusConstant.ENABLE)) {
            List<Dish> dishes = dishMapper.getBySetmealId(id);
            dishes.forEach(dish -> {
                if (dish.getStatus().equals(StatusConstant.DISABLE)) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }

        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);
    }

    @Override
    public List<DishItemVO> getDishById(Long id) {
        // List<DishItemVO> dishBySetmealId = dishMapper.getDishBySetmealId(id);
        return dishMapper.getDishBySetmealId(id);
    }

    @Override
    public List<Setmeal> getByCategotyId(Long categoryId) {
        List<Setmeal> setmeals = setmealMapper.getByCategotyId(categoryId);
        List<Setmeal> newSetmeals = setmeals.stream()
                .filter(setmeal -> setmeal.getStatus().equals(StatusConstant.ENABLE))
                .collect(Collectors.toList());
        return newSetmeals;
    }
}
