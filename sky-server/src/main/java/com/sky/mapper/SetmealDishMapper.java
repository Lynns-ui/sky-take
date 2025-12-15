package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    Integer count(List<Long> ids);

    void insert(List<SetmealDish> setmealDishes, Long id);

    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> select(Long id);

    void deleteBatch(List<Long> ids);
}
