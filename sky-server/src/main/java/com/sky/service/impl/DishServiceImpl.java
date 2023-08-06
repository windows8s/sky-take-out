package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.mapper.DishMapper;
import com.sky.vo.DishVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.swagger2.mappers.ModelMapperImpl;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 86731
* @description 针对表【dish(菜品)】的数据库操作Service实现
* @createDate 2023-08-03 20:43:08
*/
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
    implements DishService{
    @Resource
    private DishMapper dishMapper;
    @Resource
    private DishFlavorMapper dishFlavorMapper;
    @Resource
    private SetmealMapper setmealMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        //数据拷贝
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入1条数据
        dishMapper.insert(dish);

        //向口味表插入n条数据
        //获取外键id
        Long dishId = dish.getId();

        //获取到dish的flavors
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();

        //提取出每一个flavor
        for (DishFlavor flavor : dishFlavors) {
            flavor.setDishId(dishId);
            dishFlavorMapper.insert(flavor);
        }



    }

    @Override
    public PageResult queryDish(DishPageQueryDTO dishPageQueryDTO) {
        //构造分页对象
        Page<Dish> page = new Page<>(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        //创建查询条件
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(dishPageQueryDTO.getName())) {
            queryWrapper.like("name",dishPageQueryDTO.getName());
        }

        //执行分页查询
        Page<Dish> dishPage = dishMapper.selectPage(page, queryWrapper);

        //封装结果对象
        PageResult pageResult = new PageResult();
        pageResult.setTotal(dishPage.getTotal());
        pageResult.setRecords(dishPage.getRecords());

        return pageResult;

    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Dish dish = dishMapper.selectById(id);
            //判断菜品是否在起售中
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                //
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            //TODO 判断菜品是否被套餐所关联

            //删除菜品表中的数据
            dishMapper.deleteById(id);
            //删除口味表中的数据
            dishFlavorMapper.deleteByDishId(id);

        });


    }

    @Override
    public DishVO getByIdWithFalvor(Long id) {
        //校验
        if(id == null) {
            throw new IllegalArgumentException("菜品ID不能为空");
        }
        //根据id查询菜品表
        Dish dish = dishMapper.selectById(id);

        if(dish==null) {
            try {
                throw new NotFoundException("未找到菜品信息");
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        //查询关联的口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        //封装成VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;

    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //校验
        if (dishDTO.getId() == null) {
            throw new IllegalArgumentException("菜品ID不能为空");
        }

        //修改菜品表基本信息
        Dish dish = dishMapper.selectById(dishDTO.getId());
        if(dish==null) {
            try {
                throw new NotFoundException("未找到菜品信息");
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        //数据拷贝
        BeanUtils.copyProperties(dishDTO,dish);
        //更新菜品表
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",dish.getId());
        dishMapper.update(dish,queryWrapper);

        //删除当前菜品关联的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //插入最新的口味数据
        //TODO 优化插入效率
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size()>0) {
            for (DishFlavor flavor : flavors){
                flavor.setDishId(dishDTO.getId());
                dishFlavorMapper.insert(flavor);
            }
        }

    }

}




