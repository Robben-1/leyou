package com.leyou.item.api;

import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {
    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuid(@PathVariable("spuId")Long spuId);


    @GetMapping("sku/list")
    List<Sku> querySkusBySpuid(@RequestParam("id")Long id);

    @GetMapping("spu/page")
    PageResult<SpuBo> queryGoodsByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows);
}
