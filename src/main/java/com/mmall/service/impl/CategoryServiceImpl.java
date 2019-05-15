package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商品模块的业务层
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.creatByErrorMessage("参数错误");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setId(parentId);
        category.setStatus(true);

        int rowCount=categoryMapper.insert(category);
        if (rowCount>0){
            return ServerResponse.creatBySuccessMessage("添加品类成功");
        }
        return ServerResponse.creatByErrorMessage("添加品类失败");
    }
}
