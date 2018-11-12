package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.jfeat.crud.plus.CRUDFilterResult;

/**
 * Created by vincent on 2017/10/19.
 */
public abstract class BaseCRUDFilterResult<T> implements CRUDFilterResult<T> {

    private JSONObject result = new JSONObject();

    @Override
    public JSONObject result() {
        return result;
    }
}
