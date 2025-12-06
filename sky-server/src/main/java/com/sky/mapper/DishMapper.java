package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    @Select("select count(*) from dish where category_id=#{id}")
    Integer count(Long id);

    List<DishVO> findAll(DishPageQueryDTO dishPageQueryDTO);


    @AutoFill(OperationType.INSERT)
    @Insert("insert into dish (name,category_id,price,image,description,status,create_time,update_time,create_user,update_user)" +
            "values (#{name},#{categoryId},#{price},#{image},#{description},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")   // 主键返回
    Integer insert(Dish dish);

    void deleteBatch(List<Long> ids);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);
}
