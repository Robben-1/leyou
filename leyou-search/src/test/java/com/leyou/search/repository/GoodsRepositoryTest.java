package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired(required = false)
    private GoodsClient goodsClient;

    @Autowired(required = false)
    private SearchService searchService;

    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page = 1;
        int row = 100;
        int size = 0;
        do {
            // 查询spu信息
            PageResult<SpuBo> result = goodsClient.queryGoodsByPage(null, true, page, row);
            List<SpuBo> spuBoList = result.getItems();
            if (CollectionUtils.isEmpty(spuBoList)){
                break;
            }
            // 构建Goods
            List<Goods> goodsList = spuBoList.stream()
                    .map(searchService::buildGoods).collect(Collectors.toList());

            // 存入索引库
            goodsRepository.saveAll(goodsList);

            //翻页
            page++;
            size = spuBoList.size();
        }while(size == 100);

    }

}