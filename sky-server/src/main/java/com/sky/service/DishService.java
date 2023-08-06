package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
* @author 86731
* @description 针对表【dish(菜品)】的数据库操作Service
* @createDate 2023-08-03 20:43:08
*/
public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDTO dishDTO);

    PageResult queryDish(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getByIdWithFalvor(Long id);

    void updateWithFlavor(DishDTO dishDTO);

}
