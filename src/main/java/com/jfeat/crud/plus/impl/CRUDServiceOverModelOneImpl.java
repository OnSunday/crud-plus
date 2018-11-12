package com.jfeat.crud.plus.impl;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.jfeat.crud.plus.FIELD;

/**
 * Created by vincent on 2017/8/11.
 * Used to implement only one slaves
 * Replace of CRUDServiceOverSlave
 */
public abstract class CRUDServiceOverModelOneImpl<T, M extends T, I> extends CRUDServiceOverModelImpl<T,M> {
    private static final String ITEMS = "items";

    abstract protected BaseMapper<I> getSlaveMapper();
    abstract protected Class<I> getSlaveClassName();
    abstract protected String getSlaveMasterField();

    /// can be change the field name
    protected String getSlaveItemsName(){
        return ITEMS;
    }

    @Override
    protected String[] slaveFieldNames() {
        return new String[]{getSlaveItemsName()};
    }

    @Override
    protected FIELD onSlaveFieldItem(String field) {
        FIELD _field = new FIELD();

        _field.setItemKeyName(field);
        _field.setItemFieldName(getSlaveMasterField());
        _field.setItemClassName(getSlaveClassName());
        _field.setItemMapper(getSlaveMapper());

        return _field;
    }

    @Override
    protected String[] childFieldNames() {
        /// no child
        return new String[0];
    }

    @Override
    protected FIELD onChildFieldItem(String field) {
        /// do not provide child data
        throw new RuntimeException("Should not use CRUDServiceOverModelOneImpl");
    }
}
