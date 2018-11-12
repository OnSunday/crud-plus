package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.CRUDServiceOverGroup;
import com.jfeat.crud.plus.GROUP;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Created by shawshining on 2017/8/11.
 * Replace with CRUDServiceOverGroup
 */
public abstract  class CRUDServiceOverGroupImpl<G, T>  extends CRUDServiceGroupImpl<G> implements CRUDServiceOverGroup<G, T> {
    abstract protected BaseMapper<T> getGroupByMapper();

    abstract protected String groupByFieldName();

    @Override
    public JSONObject groupBy() {
        List<G> tuples = getGroupMapper().selectByMap(new HashMap<>());
        List<T> byTuples = getGroupByMapper().selectByMap(new HashMap<>());

        return GROUP.groupedBy(tuples, byTuples, groupByFieldName());
    }

    @Override
    public List<T> getGroupItems(Long groupId) {
        return getGroupByMapper().selectList(new EntityWrapper<T>().eq(groupByFieldName(), groupId));
    }

    @Override
    @Transactional
    public Integer deleteGroup(long id) {
        List<T> items = getGroupItems(id);

        if(items!=null && items.size()>0){
            /// group contains items, delete not allow
            throw new BusinessException(BusinessCode.CRUD_DELETE_NOT_EMPTY_GROUP);
        }

        return super.deleteGroup(id);
    }
}
