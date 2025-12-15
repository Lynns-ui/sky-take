package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishOverViewVO;
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
    void insert(Dish dish);

    void deleteBatch(List<Long> ids);

    @Select("select d.*,c.name categoryName from dish d left join category c on d.category_id = c.id where d.id=#{id}")
    DishVO getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);

    @Select("select d.* from dish d WHERE d.id in (select setmeal_dish.dish_id from setmeal_dish where setmeal_id=#{id})")
    List<Dish> getBySetmealId(Long id);

    @Select("SELECT d.*,tmp.copies copies FROM dish d, (select dish_id,copies from setmeal_dish where setmeal_id=#{id}) as tmp " +
            "where d.id=tmp.dish_id;")
    List<DishItemVO> getDishBySetmealId(Long id);

    @Select("select d.*,c.name categoryName from dish d left join category c on d.category_id = c.id where d.category_id=#{categoryId} and d.status=#{status}")
    List<DishVO> getDishByCategoryId(Long categoryId, Integer status);

    @Select("select " +
            "count(case when status=1 then 1 end) as sold," +
            "count(case when status=0 then 1 end) as discontinued " +
            "from dish")
    DishOverViewVO overview();

}
