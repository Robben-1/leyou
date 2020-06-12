package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired(required = false)
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows,
                                              String sortBy, Boolean desc, String key){
        //分页:PageHelper,会拦截Mysql语句，自动在Mysql语句后插入Limit语句；
        PageHelper.startPage(page, rows);

        //过滤
        /**
         * SELECT *
         * FROM tb_Brand
         * WHERE 'name' LIKE "%x%" OR letter == 'x'
         * ORDER BY id DESC
         */
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name" , "%" + key +"%")
                    .orEqualTo("letter", key.toUpperCase());
        }

        //排序
        if (StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc? " DESC": " ASC");
            example.setOrderByClause(orderByClause);
        }

        //查询
        List<Brand> brandList = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnums.BRAND_NOT_FOUND);
        }

        // 转换结果
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brandList);
        return new PageResult<>(brandPageInfo.getTotal(),brandList);
    }


    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1){
            throw new LyException(ExceptionEnums.BRAND_SAVE_ERROR);
        }

        // 保存数据到中间表
        for (Long cid : cids) {
            count = brandMapper.InsertCategoryBrand(cid, brand.getId());
            if (count != 1){
                throw new LyException(ExceptionEnums.BRAND_SAVE_ERROR);
            }
        }
    }

    public String queryBrandNameByBid(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        return brand.getName();
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnums.BRAND_NOT_FOUND);
        }
        return brandList;
    }

    public Brand queryBrandById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        return brand;
    }

    public List<Brand> quertBrandByIds(List<Long> ids) {
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnums.BRAND_NOT_FOUND);
        }
        return brandList;
    }
}
