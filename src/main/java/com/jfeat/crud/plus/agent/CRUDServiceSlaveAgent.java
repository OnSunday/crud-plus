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
import org.springframework.util.StringUtils;

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

    private String masterFieldName;            // the field name associated to master table
    private String foreignMasterFieldName;     // the field name on master table
    protected Class<I> itemClassName;

    public CRUDServiceSlaveAgent(BaseMapper<T> getMasterMapper,
                                 BaseMapper<I> getSlaveItemMapper,
                                 String masterFieldName,
                                 String foreignMasterFieldName,
                                 Class<I> itemClassName) {
        this.getMasterMapper = getMasterMapper;
        this.getSlaveItemMapper = getSlaveItemMapper;

        if(masterFieldName.contains(CRUD.ONE_MANY_LINK_SYMBOL) && (foreignMasterFieldName==null || foreignMasterFieldName.length()==0)){
            this.masterFieldName = masterFieldName.substring(0,masterFieldName.indexOf(CRUD.ONE_MANY_LINK_SYMBOL));
            this.foreignMasterFieldName = masterFieldName.substring(masterFieldName.indexOf(CRUD.ONE_MANY_LINK_SYMBOL)+1,masterFieldName.length());
        }else if(masterFieldName.contains(CRUD.ONE_MANY_LINK_SYMBOL)){
            this.masterFieldName = masterFieldName.substring(0,masterFieldName.indexOf(CRUD.ONE_MANY_LINK_SYMBOL));
        }else {
            this.masterFieldName = masterFieldName;
        }
        if(foreignMasterFieldName!=null) {
            this.foreignMasterFieldName = foreignMasterFieldName;
        }
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

    private List<I> masterFindItemList(String masterPrimaryKey) {
        return getSlaveItemMapper.selectList(new EntityWrapper<I>().eq(masterFieldName, masterPrimaryKey));
    }

    /**
     * master can be instance of Long or String
     * @param masterId
     * @return
     */
    private Integer masterRemoveItemList(Object masterId) {
        HashMap<String, Object> condition = new HashMap<>();
        condition.put(masterFieldName, masterId);
        return getSlaveItemMapper.deleteByMap(condition);
    }


    /**
     *
     * @param m
     * @param filter
     * @param itemsFieldName slave items key name
     * @param handler
     * @return
     **/
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

            String masterKey = foreignMasterFieldName !=null? foreignMasterFieldName : CRUD.primaryKey;
            Object masterId = masterObject.get(masterKey);
//            Long masterId = masterObject.getLong(CRUD.primaryKey);

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
            if(filter!=null);
            {
                JSONArray array = CRUD.toJSONOArray(items);
                filter.result().put(itemsFieldName, array);
            }

        }
        return affected;
    }

    /**
     *
     * @param m
     * @param filter
     * @param itemsFieldName slave JSON items key name
     * @param handler
     * @return
     **/
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

            String masterKey = foreignMasterFieldName !=null? foreignMasterFieldName : CRUD.primaryKey;
            Object masterId = modelObject.get(masterKey);

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

                String masterKey = foreignMasterFieldName !=null? foreignMasterFieldName : CRUD.primaryKey;
                Object masterId = modelObject.get(masterKey);

                /// get original slave items
                List<I> originalItems = getMasterMapper().selectList(new EntityWrapper<I>().eq(masterFieldName, masterId));

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

    /**
     *
     * @param masterId
     * @param filter
     * @param itemsFieldName slave列表属性名
     * @param masterField master下挂slave的参照属性名
     * @param handler
     * @return
     **/
    public CRUDObject<M> retrieveMaster(long masterId, CRUDFilterResult<T> filter, String itemsFieldName, String masterField, CRUDHandler<T, M> handler) {
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
        List<I> items = masterFindItemList(masterField);

        if (items != null && items.size() > 0) {
            //// copy a new one
            JSONObject masterObject = (JSONObject) rawMasterObject.clone();
            masterObject.put(itemsFieldName, JSON.parseArray(JSON.toJSONString(items)));

            return new CRUDObject<M>().ignore(masterObject, filter==null ? null : filter.ignore(true));
        }

        return new CRUDObject<M>().ignore(rawMasterObject, filter==null? null: filter.ignore(true));
    }

    @Override
    public CRUDObject<M> retrieveMaster(long masterId, CRUDFilterResult<T> filter, String itemsFieldName, CRUDHandler<T, M> handler) {
        return retrieveMaster(masterId, filter, itemsFieldName, CRUD.primaryKey, handler);
    }


    @Override
    @Deprecated
    public Integer deleteMaster(long masterId, String itemsFieldName) {
        /// check if master has slave
        List<I> items = masterFindItemList(itemsFieldName);

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
