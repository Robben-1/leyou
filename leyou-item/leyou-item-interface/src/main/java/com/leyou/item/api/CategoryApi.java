package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    @GetMapping("category/list")
    List<Category> queryCategoryListByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("category/cids")
    List<String> queryCategoryNameByCid(@RequestParam("cids")List<Long> cids);
}
