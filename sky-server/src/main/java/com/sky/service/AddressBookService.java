package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    void save(AddressBook addressBook);

    List<AddressBook> list();

    AddressBook defaultAddress();

    AddressBook findById(Long id);

    void update(AddressBook addressBook);

    void delete(Long id);

    void setDefault(Long id);
}
