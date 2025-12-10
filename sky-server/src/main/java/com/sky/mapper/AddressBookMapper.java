package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    @Insert("insert into address_book (user_id,consignee,sex,phone,province_code,province_name,city_code,city_name,district_code,district_name,detail,label,is_default)" +
            "values (#{userId},#{consignee},#{sex},#{phone},#{provinceCode},#{provinceName},#{cityCode},#{cityName},#{districtCode},#{districtName},#{detail},#{label},#{isDefault})")
    void insert(AddressBook addressBook);

    @Select("select * from address_book where user_id=#{currentId}")
    List<AddressBook> list(Long currentId);

    @Select("select count(*) from address_book where user_id=#{id}")
    Integer count(Long id);

    AddressBook find(AddressBook addressBook);

    void update(AddressBook addressBook);

    @Delete("delete from address_book where id=#{id}")
    void delete(Long id);
}
