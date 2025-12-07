package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.DuplicateFormatFlagsException;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {

        String oldPassword = passwordEditDTO.getOldPassword();
        String newPassword = passwordEditDTO.getNewPassword();

        // 1. 根据用户ID查询旧密码
        Employee employee = employeeMapper.getById(passwordEditDTO.getEmpId());
        String password = employee.getPassword();
        // MD5加密比对
        oldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!password.equals(oldPassword)) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

        newPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        employee.setPassword(newPassword);
        employeeMapper.update(employee);
    }

    /**
     * 员工状态
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO pageQueryDTO) {
        // 1. 设置pagehelper的起始和每页展示
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 2. 查询结果
        List<Employee> employeeList = employeeMapper.findAll(pageQueryDTO);
        // 3. 转化结果
        Page<Employee> page = (Page<Employee>) employeeList;

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {

        Employee employee = Employee.builder()
                .username(employeeDTO.getUsername())    // 用户名
                .name(employeeDTO.getName())            // 姓名
                .password(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()))// 默认密码 123456 MD5加密
                .phone(employeeDTO.getPhone())          // 手机号码
                .sex(employeeDTO.getSex())              // 性别
                .idNumber(employeeDTO.getIdNumber())    // idCard
                .status(StatusConstant.ENABLE)          // 默认状态为 1
                .build();


        employeeMapper.insert(employee);
    }

    /**
     * 查询回显
     * @param id
     * @return
     */
    @Override
    public Employee findById(Long id) {

        return employeeMapper.getById(id);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {

        Employee employee = Employee.builder()
                .id(employeeDTO.getId())
                .username(employeeDTO.getUsername())    // 用户名
                .name(employeeDTO.getName())            // 姓名
                .phone(employeeDTO.getPhone())          // 手机号码
                .sex(employeeDTO.getSex())              // 性别
                .idNumber(employeeDTO.getIdNumber())    // idCard
                .build();

        employeeMapper.update(employee);
    }
}
