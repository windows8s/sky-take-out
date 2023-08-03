package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.*;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author 86731
 * @description 针对表【employee(员工信息)】的数据库操作Service实现
 * @createDate 2023-07-13 17:18:54
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
        implements EmployeeService {
    @Resource
    private EmployeeMapper employeeMapper;

    private static final String SALT = "gdgd";

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
//        Employee employee = employeeMapper.getByUsername(username);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        Employee employee = employeeMapper.selectOne(queryWrapper);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        System.out.println(password);
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
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象属性拷贝(前面拷到后面)
        BeanUtils.copyProperties(employeeDTO, employee);
        //校验用户名username不能重复
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", employeeDTO.getUsername());
        Long count = employeeMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new AccountExistExcepiton(MessageConstant.ACCOUNT_EXIST);
        }
        //设置账号的状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码，默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex((SALT + PasswordConstant.DEFAULT_PASSWORD).getBytes()));
        //设置当前记录的创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //TODO 后期需要改为当前登陆用户的id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);

    }

    /**
     * 基于MySQL的分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //从第一条开始查第十条
        //select * from employee limit 0,10

        //构造分页对象
        Page<Employee> page = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //构造查询条件
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(employeePageQueryDTO.getName())) {
            queryWrapper.like("name", employeePageQueryDTO.getName());
        }

        //执行分页查询
        Page<Employee> employeePage = employeeMapper.selectPage(page, queryWrapper);

        //封装结果对象
        PageResult pageResult = new PageResult();
        pageResult.setTotal(employeePage.getTotal());
        pageResult.setRecords(employeePage.getRecords());
        return pageResult;
    }

    @Override
    public void employeeStatus(Integer status, Long id) {
        //update employee set status = ? where id = ?

        //根据员工ID查询员工信息
        Employee employee = employeeMapper.selectById(id);
        //校验
        if (employee == null) {
            try {
                throw new NotFoundException("员工不存在");
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        if (employee.getStatus().equals(status)) {
            throw new IllegalArgumentException("员工状态已经是所要设置的状态");
        }
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("无效的员工状态");
        }
        // 更新员工状态
        employee.setStatus(status);
        int updateById = employeeMapper.updateById(employee);
        if (updateById == 0) {
            throw new UpdateFailedException("员工状态更新失败");
        }
    }

    /**
     * 校验员工状态是否有效
     * @param status
     * @return
     */
    private boolean isValidStatus(Integer status) {
        return status != null && (status == 0 || status == 1);
    }

}




