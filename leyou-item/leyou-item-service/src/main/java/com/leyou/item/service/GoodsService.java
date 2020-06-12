package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.GoodsMapper;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired(required = false)
    private SpuDetailMapper spuDetailMapper;

    @Autowired(required = false)
    private SkuMapper skuMapper;

    @Autowired(required = false)
    private GoodsMapper goodsMapper;

    @Autowired(required = false)
    private CategoryService categoryService;

    @Autowired(required = false)
    private BrandService brandService;

    @Autowired(required = false)
    private StockMapper stockMapper;

    public PageResult<SpuBo> queryGoodsByPage(String key, Boolean saleable, Integer page, Integer rows) {
        // 分页
        PageHelper.startPage(page, rows);
        // 过滤--搜索/上下架
        Example example = new Example(Spu.class);
        if (StringUtils.isNotBlank(key)){
            example.createCriteria().andLike("title","%"+ key + "%");
        }
        if(saleable != null){
            example.createCriteria().andEqualTo("saleable", saleable);
        }
        // 查询
        List<Spu> spus = goodsMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnums.GOODS_NOT_FOUND);
        }
        // 根据查询到的品牌/分类ID，获取其实际的分类/品牌名称
        List<SpuBo> dest = new ArrayList<>();
        for (Spu spu: spus) {
            SpuBo spuBo = new SpuBo();
            //属性拷贝，将spu中封装的属性拷贝到spuBo中
            BeanUtils.copyProperties(spu, spuBo);
            //根据spu中的cid和bid去查询分类名称以及品牌名称，并封装到spuBo中
            List<String> cname = categoryService.queryCategoryNameByCid(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(cname,"/"));

            //查询spu对应的品牌名称
            String brandName = brandService.queryBrandNameByBid(spu.getBrandId());
            spuBo.setBname(brandName);
            dest.add(spuBo);
        }
        // 构造数据返回
        PageInfo<Spu> Info = new PageInfo<>(spus);
        return new PageResult<>(Info.getTotal(),dest);
    }

    @Transactional
    public void saveGoods(SpuBo spuBo) {
        // 将数据写入spu中
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        int count = goodsMapper.insert(spuBo);
        if (count != 1){
            throw new LyException(ExceptionEnums.GOODS_SPU_SAVE_ERROR);
        }

        // 写入spu_detail数据
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        count = spuDetailMapper.insert(spuDetail);
        if (count != 1){
            throw new LyException(ExceptionEnums.GOODS_SPU_DETAIL_SAVE_ERROR);
        }

        // 写入sku属性and stock信息
        List<Sku> skus = spuBo.getSkus();
        saveSkuAndStock(skus,spuBo.getId());

    }

    public SpuDetail querySpuDetailBySpuid(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        return spuDetail;
    }

    public List<Sku> querySkusBySpuid(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);

        // 查询库存
        for (Sku s : skus) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
            s.setSeckillStock(stock.getSeckillStock());
            s.setSeckillTotal(stock.getSeckillTotal());
        }
        return  skus;
    }

    private void deleteStockAndSku(Long spuId) {
        // 查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);

        // 删除stock
        if (!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            stockMapper.deleteByExample(example);
        }
        else{
            throw new LyException(ExceptionEnums.GOODS_SKU_SAVE_ERROR);
        }
        // 删除sku
        skuMapper.delete(sku);
    }

    public void saveSkuAndStock(List<Sku> skus,Long spuId){
        List<Stock> stocks = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count = skuMapper.insert(sku);
            if (count != 1){
                throw new LyException(ExceptionEnums.GOODS_SKU_SAVE_ERROR);
            }
            //写入stock信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }
        int count = stockMapper.insertList(stocks);
        if (count != stocks.size()){
            throw new LyException(ExceptionEnums.GOODS_STOCK_SAVE_ERROR);
        }
    }

    @Transactional
    public void updateGoods(SpuBo spuBo) {
        // 删除原有的sku和对应的库存
        deleteStockAndSku(spuBo.getId());
        // 新增当前的sku和库存信息
        saveSkuAndStock(spuBo.getSkus(),spuBo.getId());
        // 修改spu信息
        spuBo.setLastUpdateTime(new Date());
        int count = goodsMapper.updateByPrimaryKey(spuBo);
        if (count != 1){
            throw new LyException(ExceptionEnums.GOODS_SPU_SAVE_ERROR);
        }
        count = spuDetailMapper.updateByPrimaryKey(spuBo.getSpuDetail());
        if (count != 1){
            throw new LyException(ExceptionEnums.GOODS_SPU_DETAIL_SAVE_ERROR);
        }
    }

    public void changeSaleable(Long spuId) {
        Spu spu = goodsMapper.selectByPrimaryKey(spuId);
        spu.setSaleable(!spu.getSaleable());
        /**
         * updateByPrimaryKeySelective 会对字段进行判断再更新(如果为Null就忽略更新)，如果你只想更新某一字段，可以用这个方法
         * updateByPrimaryKey  对你注入的字段全部更新
         */
        goodsMapper.updateByPrimaryKeySelective(spu);
    }

    public void deleteGoods(Long spuId) {
        // 删除原有的sku和对应的库存
        deleteStockAndSku(spuId);
        // 删除spu 和spu_detail信息
        goodsMapper.deleteByPrimaryKey(spuId);
        spuDetailMapper.deleteByPrimaryKey(spuId);
    }
}
