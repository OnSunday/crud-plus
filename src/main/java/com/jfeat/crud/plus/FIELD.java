package com.jfeat.crud.plus;

import com.baomidou.mybatisplus.mapper.BaseMapper;

/**
 * Created by vincent on 2017/8/25.
 */

public class FIELD {
    /// 在实体中的 ItEMS 键值名，如实体复杂 entities 代替 items
    private String itemKeyName;
    /// Slave field name used to binding master field
    private String itemFieldName;
    /// master field name used by Slave field name to binding
    /// default binding to PRIMARY KEY "id"
    private String foreignItemFieldName;
    /// Slave entity class name
    private Class itemClassName;
    /// Slave entity Mapper
    private BaseMapper itemMapper;

    //for peer
    private String itemPeerFieldName;
    private Class itemRelationClassName;
    private BaseMapper itemPeerRelationMapper;

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

        // check itemFieldNamePair with $_$
        if(itemFieldName!=null && itemFieldName.contains(CRUD.ONE_MANY_LINK_SYMBOL)){

            String[] names = itemFieldName.split(CRUD.ONE_MANY_LINK_SYMBOL);

            this.itemFieldName = names[0];
            if(names.length==1){
                this.foreignItemFieldName = names[0];
            }else {
                this.foreignItemFieldName = names[1];
            }

        }else {
            this.itemFieldName = itemFieldName;
        }
    }

    public String getForeignItemFieldName() {
        return foreignItemFieldName;
    }

    public void setForeignItemFieldName(String fieldName) {
        this.foreignItemFieldName = fieldName;
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


    /**
     * item peer
     * @return
     */

    public String getItemPeerFieldName() {
        return itemPeerFieldName;
    }

    public void setItemPeerFieldName(String itemPeerFieldName) {
        this.itemPeerFieldName = itemPeerFieldName;
    }

    public Class getRelationClassName() {
        return itemRelationClassName;
    }

    public void setRelationClassName(Class itemRelationClassName) {
        this.itemRelationClassName = itemRelationClassName;
    }

    public BaseMapper getItemPeerRelationMapper() {
        return itemPeerRelationMapper;
    }

    public void setItemPeerRelationMapper(BaseMapper itemPeerRelationMapper) {
        this.itemPeerRelationMapper = itemPeerRelationMapper;
    }
}

