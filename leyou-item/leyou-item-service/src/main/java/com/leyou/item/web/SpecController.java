package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    /**
     * 根据品牌Id查询商品规格组
     * @param cid
     * @return
     */
    //http://api.leyou.com/api/item/spec/group/{cid}
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroups = specService.querySpecGroupByCid(cid);
        if (specGroups == null || specGroups.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(specGroups);
    }

    /**
     * 根据商品规格组ID 查询商品规格参数
     * @param gid
     * @return
     */
    //http://api.leyou.com/api/item/spec/params?gid=1
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParams(@RequestParam(value = "gid",required = false) Long gid,
                                                           @RequestParam(value = "cid",required = false) Long cid,
                                                           @RequestParam(value = "searching",required = false) Boolean searching,
                                                           @RequestParam(value = "generic", required = false) Boolean generic){
        List<SpecParam> specParams = specService.querySpecParams(gid, cid, searching, generic);
        if (specParams == null || specParams.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(specParams);
    }
}
