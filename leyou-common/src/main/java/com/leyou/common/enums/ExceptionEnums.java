package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum  ExceptionEnums {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空!"),
    CATEGORY_NOT_FOUND(404,"商品分类没有查到!"),
    GOODS_NOT_FOUND(404,"商品没有查到!"),
    GOODS_SPU_SAVE_ERROR(404,"商品SPU保存失败!"),
    GOODS_SKU_SAVE_ERROR(404,"商品SKU保存失败!"),
    GOODS_SPU_DETAIL_SAVE_ERROR(404,"商品SPU_Detail保存失败!"),
    GOODS_STOCK_SAVE_ERROR(404,"商品库存保存失败!"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组没有查到!"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数没有查到!"),
    BRAND_NOT_FOUND(404,"商品品牌没有查到!"),
    BRAND_SAVE_ERROR(500,"品牌保存失败!"),
    UPLOAD_FILE_ERROR(500,"上传文件失败!"),
    INVALID_FILE_TYPE(400,"无效的图片文件！")
    ;
    private int code;
    private String msg;
}
