package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
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
