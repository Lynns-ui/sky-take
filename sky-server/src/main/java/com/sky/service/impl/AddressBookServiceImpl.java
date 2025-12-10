package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());

        Integer count = addressBookMapper.count(addressBook.getUserId());
        if (count == 0) {
            // 当前用户有地址，将当前的地址设为默认
            addressBook.setIsDefault(1);
        } else {
            addressBook.setIsDefault(0);
        }

        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> list() {
        return addressBookMapper.list(BaseContext.getCurrentId());
    }

    @Override
    public AddressBook defaultAddress() {
        AddressBook addressBook = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .isDefault(1)
                .build();
        return addressBookMapper.find(addressBook);
    }

    @Override
    public AddressBook findById(Long id) {
        AddressBook addressBook = AddressBook.builder().id(id).build();
        return addressBookMapper.find(addressBook);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public void delete(Long id) {
        addressBookMapper.delete(id);
    }

    @Override
    public void setDefault(Long id) {
        // 1. 将所有的地址先设置为 非默认地址
        AddressBook addressBook = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .isDefault(0)
                .build();
        addressBookMapper.update(addressBook);
        // 2. 将当前的地址设置为 默认地址
        addressBook = AddressBook.builder().id(id).isDefault(1).build();
        addressBookMapper.update(addressBook);
    }
}
