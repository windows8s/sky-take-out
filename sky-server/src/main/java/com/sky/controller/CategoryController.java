package com.sky.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.Query;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增分类接口")
    public Result<Boolean> save(@RequestBody CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        //默认状态为1，启用
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        boolean result = categoryService.save(category);
        return Result.success(result);
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改分类接口")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO) {
        //校验
        if (categoryDTO.getId() == null) {
            return Result.error("分类ID不能为空");
        }

        Category category = categoryService.getById(categoryDTO.getId());
        if (category == null) {
            return Result.error("该分类不存在");
        }
        //拷贝数据
        BeanUtils.copyProperties(categoryDTO,category);

        //设置修改时间、修改人
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());


        boolean update = categoryService.updateById(category);
        if (update){
            return Result.success("修改成功");
        }else{
            return Result.error("修改失败");
        }

    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value = " 分类分页查询接口")
    public Result<PageResult> categoryPageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageResult pageResult = categoryService.queryCategory(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用分类")
    public Result categoryStatus(@PathVariable("status") Integer status,Long id) {
        categoryService.categoryStatus(status,id);
        return Result.success();
    }


    @DeleteMapping
    @ApiOperation(value = "根据id删除分类")
    public Result<String> deleteById(Long id) {
        //校验分类是否存在
        Category category = categoryService.getById(id);
        if (category == null){
            try {
                throw new NotFoundException("分类不存在");
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);

        boolean result = categoryService.remove(queryWrapper);
        if (result) {
            return Result.success("分类删除成功");
        }else{
            return Result.error("分类删除失败");
        }
    }

    @GetMapping("/list")
    @ApiOperation(value = "根据类型查询分类")
    public Result<List<Category>> list(Integer type) {
        List<Category> list = categoryService.listByType(type);
        return Result.success(list);
    }
}
