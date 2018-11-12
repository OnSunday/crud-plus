package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.plus.CRUDServiceGroupBy;
import com.jfeat.crud.plus.GROUP;

import java.util.List;

/**
 * Created by shawshining on 2017/8/11.
 * Replace with CRUDServiceOverGroup
 */
public abstract class CRUDServiceGroupByImpl<G, T>  implements CRUDServiceGroupBy<G, T> {
    abstract protected BaseMapper<G> getGroupMapper();
    abstract protected BaseMapper<T> getGroupByMapper();

    abstract protected String groupByFieldName();

    @Override
    public JSONObject groupBy() {
        List<G> tuples = getGroupMapper().selectList(new EntityWrapper<>());
        List<T> byTuples = getGroupByMapper().selectList(new EntityWrapper<>());

        return GROUP.groupedBy(tuples, byTuples, groupByFieldName());
    }

    @Override
    public List<T> getGroupItems(Long groupId) {
        return getGroupByMapper().selectList(new EntityWrapper<T>().eq(groupByFieldName(), groupId));
    }
}
