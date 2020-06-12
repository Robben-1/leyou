package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGoupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecService {

    @Autowired(required = false)
    private SpecGoupMapper groupMapper;

    @Autowired(required = false)
    private SpecParamMapper paramMapper;

    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);

        List<SpecGroup> groups = groupMapper.select(specGroup);
        if(CollectionUtils.isEmpty(groups)){
            throw new LyException(ExceptionEnums.CATEGORY_NOT_FOUND);
        }
        return groups;
    }

    public List<SpecParam> querySpecParams(Long gid, Long cid, Boolean searching,Boolean generic) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(gid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);

        List<SpecParam> params = paramMapper.select(specParam);
        if(CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnums.SPEC_PARAM_NOT_FOUND);
        }
        return params;
    }
}
