package com.jfeat.crud.plus.impl;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.jfeat.crud.plus.FIELD;

/**
 * Created by vincent on 2017/8/11.
 * Used to implement only one slaves
 * Replace of CRUDServiceOverSlave
 */
public abstract class CRUDServiceOverModelPeerImpl<T, M extends T, P,R> extends CRUDServiceOverModelImpl<T,M> {
    private static final String ITEMS = "items";

    abstract protected BaseMapper<P> getMasterPeerMapper();
    abstract protected BaseMapper<R> getRelationMapper();

    abstract protected Class<P> getPeerClassName();
    abstract protected Class<R> getRelationClassName();

    abstract protected String[] getRelationMatches();
    protected String getMasterFieldName(){
        return getRelationMatches()[0];
    }
    protected String getMasterPeerFieldName(){
        return getRelationMatches()[1];
    }


    /// can be change the field name
    protected String getPeerItemsName(){
        return ITEMS;
    }

    @Override
    protected FIELD onSlaveFieldItem(String field) {
        throw new RuntimeException("Should not use CRUDServiceOverModelPeerImpl");
    }

    @Override
    protected String[] slaveFieldNames() {
        return new String[0];
    }

    @Override
    protected String[] childFieldNames() {
        /// no child
        return new String[0];
    }

    @Override
    protected FIELD onChildFieldItem(String field) {
        /// do not provide child data
        throw new RuntimeException("Should not use CRUDServiceOverModelPeerImpl");
    }


    /**
     * peer
     * @param field
     * @return
     */

    @Override
    protected FIELD onPeerFieldItem(String field) {
        FIELD _field = new FIELD();

        _field.setItemKeyName(field);

        _field.setItemFieldName(getMasterFieldName());
        _field.setItemPeerFieldName(getMasterPeerFieldName());

        _field.setItemClassName(getPeerClassName());
        _field.setRelationClassName(getRelationClassName());

        _field.setItemMapper(getMasterPeerMapper());
        _field.setItemPeerRelationMapper(getRelationMapper());

        return _field;
    }

    @Override
    protected String[] peerFieldNames() {
        return new String[]{getPeerItemsName()};
    }
}
