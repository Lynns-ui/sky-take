package com.sky.mapper;

import com.sky.dto.DishPageQueryDTO;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    @Select("select count(*) from dish where category_id=#{id}")
    Integer count(Long id);

    List<DishVO> findAll(DishPageQueryDTO dishPageQueryDTO);
}
