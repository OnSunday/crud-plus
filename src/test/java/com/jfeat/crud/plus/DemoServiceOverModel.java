package com.jfeat.crud.plus;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.jfeat.crud.plus.impl.CRUDServiceOverModelImpl;

import javax.annotation.Resource;

/**
 * Created by vincenthuang on 17/10/2017.
 * Sample for master with multi-slaves and multi-children
 */
public class DemoServiceOverModel extends CRUDServiceOverModelImpl<DemoType, DemoTypeModel> {

    @Resource
    BaseMapper<DemoType> masterMapper;

    @Resource
    BaseMapper<DemoItem> slaveItemMapper;

    @Resource
    BaseMapper<DemoItem> childItemMapper;

    @Override
    protected BaseMapper<DemoType> getMasterMapper() {
        return masterMapper;
    }

    @Override
    protected String[] slaveFieldNames() {
        return new String[]{"snapshot", "payment"};
    }

    @Override
    protected String[] childFieldNames() {
        return new String[]{"profile"};
    }


    @Override
    protected FIELD onSlaveFieldItem(String field) {

        if(field.compareTo("snapshot")==0) {
            FIELD _field = new FIELD();

            _field.setItemKeyName(field);
            _field.setItemFieldName("snapshot_field");
            _field.setForeignItemFieldName("snapshot_field");
            _field.setItemClassName(DemoItem.class);
            _field.setItemMapper(slaveItemMapper);

            return _field;
        }else if(field.compareTo("payment")==0){
            FIELD _field = new FIELD();

            _field.setItemKeyName(field);
            _field.setItemFieldName("demo_type_id");
            _field.setForeignItemFieldName(CRUD.primaryKey);  // default "id"
            _field.setItemClassName(DemoItem.class);
            _field.setItemMapper(slaveItemMapper);

            return _field;
        }


        throw new RuntimeException("Should not get here");
    }

    @Override
    protected FIELD onChildFieldItem(String field) {
        if(field.compareTo("profile")==0) {
            FIELD _field = new FIELD();

            _field.setItemKeyName(field);
            _field.setItemFieldName("profile_id");
            _field.setItemClassName(DemoItem.class);
            _field.setItemMapper(childItemMapper);

            return _field;
        }


        throw new RuntimeException("Should not get here");
    }

    @Override
    protected Class<DemoType> masterClassName() {
        return DemoType.class;
    }

    @Override
    protected Class<DemoTypeModel> modelClassName() {
        return DemoTypeModel.class;
    }

}
