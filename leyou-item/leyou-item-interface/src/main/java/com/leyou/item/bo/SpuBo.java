package com.leyou.item.bo;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

@Data
public class SpuBo extends Spu {
    @Transient
    private String cname; //商品分类名称

    @Transient
    private String bname;//商品品牌名称

    @Transient
    private List<Sku> skus; //Sku

    @Transient
    private SpuDetail spuDetail; //SpuDetail
}
