package com.leyou.item.api;

import com.leyou.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecificationApi {
    @GetMapping("params")
     List<SpecParam> querySpecParams(@RequestParam(value = "gid",required = false) Long gid,
                                   @RequestParam(value = "cid",required = false) Long cid,
                                   @RequestParam(value = "searching",required = false) Boolean searching,
                                   @RequestParam(value = "generic", required = false) Boolean generic);
}
