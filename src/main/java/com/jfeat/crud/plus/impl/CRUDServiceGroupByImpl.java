package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
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

    private static final String GROUP_ID_SYMBOL = ":";

    @Override
    public JSONObject groupBy() {
        List<G> tuples = getGroupMapper().selectList(new EntityWrapper<>());
        List<T> byTuples = getGroupByMapper().selectList(new EntityWrapper<>());

        if(groupByFieldName().contains(GROUP_ID_SYMBOL)){
            String groupByFieldName = groupByFieldName();

            String groupBy = groupByFieldName.substring(0,groupByFieldName.indexOf(GROUP_ID_SYMBOL));
            String groupIdField = groupByFieldName.substring(groupByFieldName.indexOf(GROUP_ID_SYMBOL)+1);
            if(groupIdField==null || groupIdField.length()==0){
                throw new BusinessException(BusinessCode.CRUD_GENERAL_ERROR);
            }

            return GROUP.groupedBy(tuples, byTuples, groupBy, groupIdField);
        }

        return GROUP.groupedBy(tuples, byTuples, groupByFieldName());
    }

    @Override
    public List<T> getGroupItems(Long groupId) {

        String groupByFieldName = groupByFieldName();
        if(groupByFieldName.contains(GROUP_ID_SYMBOL)){
            groupByFieldName = groupByFieldName.substring(0,groupByFieldName.indexOf(GROUP_ID_SYMBOL));
        }

        return getGroupByMapper().selectList(new EntityWrapper<T>().eq(groupByFieldName, groupId));
    }
}
