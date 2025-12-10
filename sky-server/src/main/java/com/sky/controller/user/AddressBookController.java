package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user/addressBook")
@RestController
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增用户地址
     */
    @PostMapping
    public Result<String> save(@RequestBody AddressBook addressBook) {
        log.info("新增用户地址：{}", addressBook);
        addressBookService.save(addressBook);
        return Result.success();
    }

    /**
     * 当前用户的地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list() {
        log.info("当前用户的地址列表");
        List<AddressBook> addressBooks = addressBookService.list();
        return Result.success(addressBooks);
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/default")
    public Result<AddressBook> defaultAddress() {
        log.info("当前用户的默认地址");
        AddressBook addressBook = addressBookService.defaultAddress();
        return Result.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public Result<AddressBook> findById(@PathVariable Long id) {
        log.info("当前地址id:{}",id);
        AddressBook addressBook = addressBookService.findById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     */
    @PutMapping
    public Result<String> update(@RequestBody AddressBook addressBook) {
        log.info("更新地址，{}", addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping
    public Result<String> delete(Long id) {
        log.info("删除地址：{}", id);
        addressBookService.delete(id);
        return Result.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public Result<String> setDefault(@RequestBody AddressBook addressBook) {
        log.info("设置为默认地址：{}",addressBook.getId());
        addressBookService.setDefault(addressBook.getId());
        return Result.success();
    }

}
