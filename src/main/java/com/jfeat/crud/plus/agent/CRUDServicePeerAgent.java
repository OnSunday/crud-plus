package com.jfeat.crud.plus.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.*;
import com.jfeat.crud.plus.impl.CRUDServicePeerImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vincenthuang on 2017/8/11.
 * Used to build slave data into master model, for creation, updating, and retrieving
 */
public class CRUDServicePeerAgent<T, M extends T, P,R> extends CRUDServicePeerImpl<T,P,R>
        implements CRUDServiceModelResult<T, M> {

    protected BaseMapper<T> getMasterMapper;
    protected BaseMapper<P> getMasterPeerMapper;
    protected BaseMapper<R> getRelationMapper;

    /// Peer Relation fields
    private String[] relationMatches;
    private String getMasterFieldName(){return relationMatches[0];}
    private String getMasterPeerFieldName(){return relationMatches[1];}

    protected Class<P> peerClassName;
    protected Class<R> relationClassName;

    @Override
    protected BaseMapper<T> getMasterMapper() {
        return getMasterMapper;
    }

    /// from CRUDServicePeer
    @Override
    protected BaseMapper<P> getMasterPeerMapper() {
        return getMasterPeerMapper;
    }

    @Override
    protected BaseMapper<R> getRelationMapper() {
        return getRelationMapper;
    }

    @Override
    protected String[] relationMatches() {
        return relationMatches;
    }

    public CRUDServicePeerAgent(BaseMapper<T> getMasterMapper,
                                BaseMapper<P> getMasterPeerMapper,
                                BaseMapper<R> getRelationMapper,
                                String[] relationMatches,
                                Class<P> peerClassName,
                                Class<R> relationClassName
                                ) {
        this.getMasterMapper = getMasterMapper;
        this.getMasterPeerMapper = getMasterPeerMapper;
        this.getRelationMapper = getRelationMapper;

        this.relationMatches = relationMatches;

        this.peerClassName = peerClassName;
        this.relationClassName = relationClassName;
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

    ///
    private List<P> masterFindItemList(long masterId) {
        return this.getPeerList(masterId);
    }

    @Deprecated
    private Integer masterRemoveItemList(long masterId) {
        /// remove all previous items, not recommended.
        return this.masterRemoveItemList(masterId);
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
                json.put(getMasterFieldName(), masterId);
            }

            /// convert json array into model
            List<P> items = JSON.parseArray(itemsArray.toJSONString(), peerClassName);

            /// attach slave data into new class
            affected = this.bulkAppendPeerList(masterId, items);

            /// save the result into filter
            if(filter!=null)
            {
                JSONArray array = CRUD.toJSONOArray(items);
                filter.result().put(itemsFieldName, array);
            }
        }

        return affected;
    }

    public Integer bulkAppendPeerList(Long masterId, List<P> list) {
        Integer affected = 0;

        List<Long> ids = new ArrayList<>();

        for (P item : list) {
            affected += getMasterPeerMapper().insert(item);

            /// get id
            JSONObject modelObject = CRUD.toJSONObject(item);
            Long id = modelObject.getLong(CRUD.primaryKey);
            ids.add(id);
        }

        /// add relation
        this.addPeerList(masterId, ids);

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
                List<P> items = CRUD.toJavaObjectList(modelItems, peerClassName);

                affected += this.bulkAppendPeerList(masterId, items);
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

                /// get original peer items
                List<P> originalItems = this.getPeerList(masterId);

                List<Long> skips = new ArrayList<>();

                Iterator<Object> it = modelItems.iterator();
                while (it.hasNext()) {
                    JSONObject one = (JSONObject) it.next();

                    /// check new one
                    Long id = one.getLong(CRUD.primaryKey);
                    if (id == null || id == 0) {

                        // fix bug: add master field value
                        one.put(getMasterFieldName(), masterId);

                        /// get here , no id, this is new one
                        P peer = JSON.toJavaObject(one, peerClassName);

                        affected += getMasterPeerMapper().insert(peer);

                        //TODO, add relation


                    } else {

                        /// find the peer item
                        if (getMasterPeerMapper.selectById(id) != null) {
                            /// get here , we are updating
                            P peer = JSON.toJavaObject(one, peerClassName);
                            affected += getMasterPeerMapper.updateAllColumnById(peer);

                            // updated, skip
                            skips.add(id);
                        }
                    }
                }

                /// delete the not exist ones
                List<Long> ids = new ArrayList<>();
                for (P i : originalItems) {
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
                    affected += super.removePeerList(masterId, ids);
                }
            }

            return affected;
        }
    }


    @Override
    public CRUDObject<M> retrieveMaster(long masterId, CRUDFilterResult<T> filter,
                                        String itemsFieldName, CRUDHandler<T, M> handler) {
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
        List<P> items = masterFindItemList(masterId);

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
        List<P> items = masterFindItemList(masterId);

        if (items != null && items.size() > 0) {
            throw new BusinessException(BusinessCode.CRUD_DELETE_NOT_EMPTY_GROUP);
        }

        return items == null ? 0 : items.size();

        /// do not really delete
        //return getMasterMapper.deleteById(masterId);
    }
}
