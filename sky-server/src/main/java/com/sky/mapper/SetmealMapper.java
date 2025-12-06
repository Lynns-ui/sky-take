package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {

    List<SetmealVO> getAll(SetmealPageQueryDTO setmealPageQueryDTO);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    @Insert("insert into setmeal (category_id,name,price,status,description,image,create_time,update_time,create_user,update_user)" +
            "values (#{categoryId},#{name},#{price},#{status},#{description},#{image},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Setmeal setmeal);

    @Select("select s.*,c.name categoryName from setmeal s left join category c on s.category_id=c.id where s.id=#{id}")
    SetmealVO select(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    void delete(List<Long> ids);
}
