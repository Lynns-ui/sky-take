package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> findAll(CategoryPageQueryDTO categoryPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    @Insert("insert into category (type,name,sort,status,create_time,update_time,create_user,update_user)" +
            "values (#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Category category);

    @Delete("delete from category where id=#{id}")
    void delete(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    List<Category> list(Integer type, Integer status);
}
