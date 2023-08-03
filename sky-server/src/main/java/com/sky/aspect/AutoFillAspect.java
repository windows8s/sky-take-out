package com.sky.aspect;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annoation.AutoFill)")
    public void autoFillPointCut(){
    }

    @Before("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.insert(..))")
    public void fillInsertFields(JoinPoint joinPoint) {
        //与视频不同，因为我们是使用mp的tablefield注解，所以不需要获取数据库操作类型

        //获取公共字段的时间、创建人
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //获取当前被拦截的方法的参数--实体对象
        //规定：获取的第一个实体对象均为实体
        Object[] args = joinPoint.getArgs();

        Object entity = args[0];

        try {
            Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
            setCreateTime.invoke(entity,now);
            setUpdateTime.invoke(entity,now);
            setCreateUser.invoke(entity,currentId);
            setUpdateUser.invoke(entity,currentId);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Before("execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper.updateById(..))")
    public void fillUpdateFields(JoinPoint joinPoint) {
        //获取公共字段的时间、创建人
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //获取当前被拦截的方法的参数--实体对象
        //规定：获取的第一个实体对象均为实体
        Object[] args = joinPoint.getArgs();

        Object entity = args[0];

        try {
            Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
