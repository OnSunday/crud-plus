package com.jfeat.crud.plus;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by vincent on 2017/8/25.
 */
public interface CRUDServiceGroupBy<G, T>{

    /**
     * grouped by group/type table
     */
    JSONObject groupBy();

    /**
     * get items of group
     */
    List<T> getGroupItems(Long groupId);

}
