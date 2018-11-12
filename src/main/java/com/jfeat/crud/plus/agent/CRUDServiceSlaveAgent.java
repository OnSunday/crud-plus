package com.jfeat.crud.plus.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.*;
import com.jfeat.crud.plus.impl.CRUDServiceOnlyImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vincenthuang on 2017/8/11.
 * Used to build slave data into master model, for creation, updating, and retrieving
 */
public class CRUDServiceSlaveAgent<T, M extends T, I> extends CRUDServiceOnlyImpl<I>
        implements CRUDServiceModelResult<T, M> {

    protected BaseMapper<T> getMasterMapper;
    protected BaseMapper<I> getSlaveItemMapper;

    private String masterFieldName;
    protected Class<I> itemClassName;

    @Override
    protected String masterField() {
        return masterFieldName;
    }

    public CRUDServiceSlaveAgent(BaseMapper<T> getMasterMapper,
                                 BaseMapper<I> getSlaveItemMapper,
                                 String masterFieldName,
                                 Class<I> itemClassName) {
        this.getMasterMapper = getMasterMapper;
        this.getSlaveItemMapper = getSlaveItemMapper;
        this.masterFieldName = masterFieldName;
        this.itemClassName = itemClassName;
    }

    /// Master Class Name
    /// used to newInstance instead of select from database
    private Class<T> masterClassName;

    public Class<T> getMasterClassName() {
        return masterClassName;
    }

    public void setMasterClassName(Class<T> masterClassName) {
        this.masterClassName = masterClassName;
    }

    /// Master Object, used to skip query
    //private JSONObject rawMasterObject;

    private List<I> masterFindItemList(long masterId) {
        return getSlaveItemMapper.selectList(new EntityWrapper<I>().eq(masterField(), masterId));
    }

    private Integer masterRemoveItemList(long masterId) {
        HashMap<String, Object> condition = new HashMap<>();
        condition.put(masterField(), masterId);
        return getSlaveItemMapper.deleteByMap(condition);
    }

    @Override
    public Integer createMaster(M m, CRUDFilterResult<T> filter, String itemsFieldName, CRUDHandler<T, M> handler) {
        if (handler != null) {
            throw new RuntimeException("Must be handled by tool");
        }

        /// DO NOT insert master model here, as multi slaves will be affected.
        /// ONLY affects local slave items

        //Integer affected = getMasterMapper.insert(m);

        Integer affected = 0;

        /// get the new inserted master id
        JSONObject masterObject = CRUD.toJSONObject(m);

        /// handle slave items if contains slave items key
        if (masterObject.containsKey(itemsFieldName)) {

            Long masterId = masterObject.getLong(CRUD.primaryKey);

            JSONArray itemsArray = masterObject.getJSONArray(itemsFieldName);
            JSONObject[] jsonObjects = CRUD.toJSONArray(itemsArray);
            for (JSONObject json : jsonObjects) {
                json.put(masterFieldName, masterId);
            }

            /// convert json array into model
            List<I> items = JSON.parseArray(itemsArray.toJSONString(), itemClassName);

            /// attach slave data into new class
            affected = super.bulkAppendMasterList(items);

            /// save the result into filter
            if(filter!=null)
            {
                JSONArray array = CRUD.toJSONOArray(items);
                filter.result().put(itemsFieldName, array);
            }

        }

        return affected;
    }

    @Override
    public Integer updateMaster(M m, CRUDFilterResult<T> filter, String itemsFieldName, CRUDHandler<T, M> handler) {
        if (handler != null) {
            throw new RuntimeException("Must be handled by tool");
        }

        if (false) {
            /// quick replace
            /// delete all the items previous
            /// and then add the new ones

            Integer affected = 0;

            JSONObject modelObject = CRUD.toJSONObject(m);
            Long masterId = modelObject.getLong(CRUD.primaryKey);

            /// remote the old ones
            affected += masterRemoveItemList(masterId);

            /// add the new ones
            if (modelObject.containsKey(itemsFieldName)) {
                JSONArray modelItems = modelObject.getJSONArray(itemsFieldName);
                List<I> items = CRUD.toJavaObjectList(modelItems, itemClassName);

                affected += super.bulkAppendMasterList(items);
            }

            return affected;

        } else {
            /// 1. remove the old ones
            /// 2. update the exist ones
            /// 3. add the new ones
            Integer affected = 0;

            JSONObject modelObject = CRUD.toJSONObject(m);

            if (modelObject.containsKey(itemsFieldName)) {
                JSONArray modelItems = modelObject.getJSONArray(itemsFieldName);

                Long masterId = modelObject.getLong(CRUD.primaryKey);

                /// get original slave items
                List<I> originalItems = getMasterMapper().selectList(new EntityWrapper<I>().eq(masterField(), masterId));

                List<Long> skips = new ArrayList<>();

                Iterator<Object> it = modelItems.iterator();
                while (it.hasNext()) {
                    JSONObject one = (JSONObject) it.next();

                    /// check new one
                    Long id = one.getLong(CRUD.primaryKey);
                    if (id == null || id == 0) {

                        // bug: add master field value
                        one.put(masterFieldName, masterId);

                        /// get here , no id, this is new one
                        I i = JSON.toJavaObject(one, itemClassName);

                        affected += super.createMaster(i);

                    } else {

                        /// find the slave item
                        if (super.retrieveMaster(id) != null) {
                            /// get here , we are updating
                            I i = JSON.toJavaObject(one, itemClassName);
                            affected += super.updateMaster(i);

                            // updated, skip
                            skips.add(id);
                        }
                    }
                }

                /// delete the not exist ones
                List<Long> ids = new ArrayList<>();
                for (I i : originalItems) {
                    JSONObject one = CRUD.toJSONObject(i);
                    long oid = one.getLong(CRUD.primaryKey);

                    boolean flag = false;
                    for (Long sid : skips) {
                        if (sid.longValue() == oid) {
                            flag = true;
                        }
                    }

                    /// not in skips, need to remove
                    if (!flag) {
                        ids.add(oid);
                    }
                }

                if (ids != null && ids.size() > 0) {
                    affected += super.bulkDeleteMasterList(ids);
                }
            }

            return affected;
        }
    }


    @Override
    public CRUDObject<M> retrieveMaster(long masterId, CRUDFilterResult<T> filter, String itemsFieldName, CRUDHandler<T, M> handler) {
        if (handler != null) {
            throw new RuntimeException("Must be handled by tool");
        }

        JSONObject rawMasterObject = null;
        if (rawMasterObject == null) {
            T t = getMasterMapper.selectById(masterId);
            if(t!=null) {
                rawMasterObject = CRUD.toJSONObject(t);
            }
        }
        if(rawMasterObject==null){
            return null;
        }

        /// append slave items
        List<I> items = masterFindItemList(masterId);

        if (items != null && items.size() > 0) {
            //// copy a new one
            JSONObject masterObject = (JSONObject) rawMasterObject.clone();
            masterObject.put(itemsFieldName, JSON.parseArray(JSON.toJSONString(items)));

            return new CRUDObject<M>().ignore(masterObject, filter==null ? null : filter.ignore(true));
        }

        return new CRUDObject<M>().ignore(rawMasterObject, filter==null? null: filter.ignore(true));
    }

    @Override
    public Integer deleteMaster(long masterId, String itemsFieldName) {
        /// check if master has slave
        List<I> items = masterFindItemList(masterId);

        if (items != null && items.size() > 0) {
            throw new BusinessException(BusinessCode.CRUD_DELETE_NOT_EMPTY_GROUP);
        }

        return items == null ? 0 : items.size();

        /// do not really delete
        //return getMasterMapper.deleteById(masterId);
    }

    @Override
    protected BaseMapper<I> getMasterMapper() {
        return getSlaveItemMapper;
    }
}
