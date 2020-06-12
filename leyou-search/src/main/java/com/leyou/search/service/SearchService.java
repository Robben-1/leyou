package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired(required = false)
    private BrandClient brandClient;

    @Autowired(required = false)
    private CategoryClient categoryClient;

    @Autowired(required = false)
    private GoodsClient goodsClient;

    @Autowired(required = false)
    private SpecificationClient specClient;

    @Autowired
    private ElasticsearchRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public Goods buildGoods(Spu spu){
        // 查询分类
        List<Category> categorys = categoryClient.queryCategoryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categorys)){
            throw new LyException(ExceptionEnums.CATEGORY_NOT_FOUND);
        }
        List<String> names = categorys.stream().map(Category::getName).collect(Collectors.toList());
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null){
            throw new LyException(ExceptionEnums.BRAND_NOT_FOUND);
        }
        // 拼接搜索字段
        String all = spu.getSubTitle() + StringUtils.join(names,",") + brand.getName();

        // 查询sku
        List<Sku> skuList = goodsClient.querySkusBySpuid(spu.getId());
        if (CollectionUtils.isEmpty(skuList)){
            throw new  LyException(ExceptionEnums.GOODS_NOT_FOUND);
        }
        // 处理SkuList(Map)
        List<Map<String,Object>> skus = new ArrayList<>();
        Set<Long> priceList = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("images",StringUtils.substringBefore(sku.getImages(),","));
            skus.add(map);
            // 处理价格
            priceList.add(sku.getPrice());
        }

        // 查询规格参数
        List<SpecParam> params = specClient.querySpecParams(
                                    null, spu.getCid3(), true,false);
        if (CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnums.SPEC_PARAM_NOT_FOUND);
        }
        // 获取商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuid(spu.getId());
        // 获取通用规格参数
        Map<String, Object> genericSpec = JsonUtils.parseMap(
                                    spuDetail.getGenericSpec(), String.class, Object.class);
        // 获取特有规格参数
        Map<String, List<Object>> specialSpec = JsonUtils
                .nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {});

        // 规格参数，Key是规格参数的名字，Value是规格参数的值
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            String key = param.getName();
            Object value = "";
            if (param.getGeneric()){
                value = genericSpec.get(param.getId().toString());
                // 判断是否为数值类型
                if (!param.getNumeric()){
                    // 处理成段
                    value = chooseSegment(value.toString(),param);
                }
            }else{
                value = specialSpec.get(param.getId().toString());
            }
            specs.put(key, value);
        }

        // 构建Goods
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());

        goods.setAll(all);// 搜索字段
        goods.setPrice(priceList);
        goods.setSkus(JsonUtils.serialize(skus));
        goods.setSpecs(specs);

        return goods;
    }


    public PageResult<Goods> search(SearchRequest searchRequest) {
        int page = searchRequest.getPage();
        int size = searchRequest.getSize();
        // 1 创建构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", searchRequest.getKey());

        // 1.1 分页
        queryBuilder.withPageable(PageRequest.of(page,size));

        // 1.2 通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));

        // 1.3 基本查询
        queryBuilder.withQuery(basicQuery);

        // 1.4 排序
        String sortBy = searchRequest.getSortBy();
        Boolean descending = searchRequest.getDescending();
        if (StringUtils.isNotEmpty(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(descending? SortOrder.DESC:SortOrder.ASC));
        }

        String categoryName = "category";
        String brandName = "brand";
        // 商品分类 / 品牌 聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandName).field("brandId"));

        // 查询
        AggregatedPage<Goods> pageInfo  = (AggregatedPage<Goods>)repository.search(queryBuilder.build());

        // 3 解析查询结果
        // 3.1 分页结果
        Long total = pageInfo.getTotalElements();
        int totalPage = pageInfo.getTotalPages();

        //3.2 商品分类 / 品牌 聚合结果
        List<Category> categories = getCategoryAggResult(pageInfo.getAggregation(categoryName));
        List<Brand> brands = getBrandAggResult(pageInfo.getAggregation(brandName));

        // 3.3 获取商品规格参数（只有商品分类数量为1时，才获取规格参数）
        List<Map<String,Object>> specs = new ArrayList<>();
        if (categories.size() == 1){
            specs = getSpec(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(total, (long)totalPage,pageInfo.getContent(),categories,brands);
    }

    private List<Map<String,Object>> getSpec(Long cid, QueryBuilder query) {
        try {
            // 根据cid，搜索规格参数集合
            List<SpecParam> specParams = specClient.querySpecParams(null, cid, true, null);
            List<Map<String,Object>> specs = new ArrayList<>();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(query); // 添加基本查询（查询字段）

            // 根据规格参数字段，添加聚合条件
            for (SpecParam specParam : specParams) {
                String key = specParam.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(key).field("spec."+ key + ".keyword"));
            }

            // 查询，返回多个聚合结果
            AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
            // 解析结果
            Aggregations aggs = result.getAggregations();
            for (SpecParam specParam : specParams) {
                // 规格参数
                String name = specParam.getName();
                StringTerms agg = aggs.get(name); // 获取对应规格参数的聚合

                // 将数据存入Map中
                Map<String,Object> map = new HashMap<>();
                map.put("k",name);
                map.put("options",agg.getBuckets().stream()
                        .map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList()));
                specs.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms brandAgg  = (LongTerms)aggregation;
        List<Long> bids = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()) {
            bids.add(bucket.getKeyAsNumber().longValue());
        }

        List<Brand> brandList = brandClient.queryBrandByIds(bids);
        return brandList;
    }

    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        List<Category> categories = new ArrayList<>();
        LongTerms categoryAgg = (LongTerms)aggregation;
        List<Long> cids = new ArrayList<>();
        for (LongTerms.Bucket bucket: categoryAgg.getBuckets()){
            cids.add(bucket.getKeyAsNumber().longValue());
        }

        // 查询Category 名称
        List<String> names = this.categoryClient.queryCategoryNameByCid(cids);

        for(int i = 0;i < names.size() - 1; i++){
            Category category = new Category();
            category.setName(names.get(i));
            category.setId(cids.get(i));
            categories.add(category);
        }
        return  categories;
    }
}
