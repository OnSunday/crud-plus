package com.jfeat.crud.plus;

import com.baomidou.mybatisplus.mapper.BaseMapper;

/**
 * Created by vincent on 2017/8/25.
 */

public class FIELD {
    private String itemKeyName;
    private String itemFieldName;
    private Class itemClassName;
    private BaseMapper itemMapper;

    public BaseMapper getItemMapper() {
        return itemMapper;
    }

    public void setItemMapper(BaseMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public String getItemFieldName() {
        return itemFieldName;
    }

    public void setItemFieldName(String itemFieldName) {
        this.itemFieldName = itemFieldName;
    }

    public Class getItemClassName() {
        return itemClassName;
    }

    public void setItemClassName(Class itemClassName) {
        this.itemClassName = itemClassName;
    }

    public String getItemKeyName() {
        return itemKeyName;
    }

    public void setItemKeyName(String itemKeyName) {
        this.itemKeyName = itemKeyName;
    }
}

