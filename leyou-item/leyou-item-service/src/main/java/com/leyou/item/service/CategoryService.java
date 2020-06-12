package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired(required = false) //Autowired Required属性
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long pid){
        Category selectCategory = new Category();
        selectCategory.setParentId(pid);
        List<Category> list = categoryMapper.select(selectCategory);

        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnums.CATEGORY_NOT_FOUND);
        }

        return list;
    }


    public List<String> queryCategoryNameByCid(List<Long> cids) {
        List<Category> categoryList = categoryMapper.selectByIdList(cids);
        if (CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnums.CATEGORY_NOT_FOUND);
        }
        List<String> cname = new ArrayList<>();
        for(Category category: categoryList){
            cname.add(category.getName());
        }
        return cname;
    }

    public List<Category> queryByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnums.CATEGORY_NOT_FOUND);
        }
        return list;
    }
}
