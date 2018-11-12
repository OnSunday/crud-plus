package com.jfeat.crud.plus;


import com.alibaba.fastjson.JSONObject;

/**
 * Created by vincenthuang on 27/08/2017.
 */
public interface CRUDFilterResult<T> extends CRUDFilter<T> {
    /**
     *
     * @return
     */
    JSONObject result();
}


