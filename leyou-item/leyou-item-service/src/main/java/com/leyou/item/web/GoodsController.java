package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 商品的分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> queryGoodsByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows){
        PageResult<SpuBo> spuBos = goodsService.queryGoodsByPage(key, saleable, page, rows);
        return ResponseEntity.ok(spuBos);
    }

    /**
     * 新增商品
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 根据SpuID查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuid(@PathVariable("spuId")Long spuId){
        SpuDetail spuDetail = goodsService.querySpuDetailBySpuid(spuId);
        if(spuDetail==null){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 商品修改时的回显
     * 根据SpuId查询Sku作为回显数据
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuid(@RequestParam("id")Long id){
        List<Sku> skus = goodsService.querySkusBySpuid(id);
        if(skus==null || skus.size()==0){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 修改商品->先删除原有的商品sku和stock，再新增，最后修改spu
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 修改商品的上下架状态
     * @param spuId
     * @return
     */
    @PutMapping("spu/{spuId}")
    public ResponseEntity<Void> changeSaleable(@PathVariable("spuId")Long spuId){
        try {
            goodsService.changeSaleable(spuId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除商品
     * @param spuId
     * @return
     */
    @DeleteMapping("spu/{spuId}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("spuId")Long spuId){
        try {
            goodsService.deleteGoods(spuId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
