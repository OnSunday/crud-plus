package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.CRUD;
import com.jfeat.crud.plus.CRUDServicePeer;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vincenthuang on 2017/8/11.
 * Provide slave services for master
 */
public abstract class CRUDServicePeerImpl<M, P, R> implements CRUDServicePeer<M, P, R> {

    abstract protected BaseMapper<M> getMasterMapper();
    abstract protected BaseMapper<P> getMasterPeerMapper();
    abstract protected BaseMapper<R> getRelationMapper();

    abstract protected String[] relationMatches();

    protected Class<R> relationClassName(){
        throw new RuntimeException("Override this method is required.");
    }

    @Override
    @Transactional
    public Integer addPeerList(long masterId, List<Long> ids) {
        Integer affected = 0;
        for (Long id : ids){
            affected += addRelation(masterId, id);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer addPeerList(long masterId, List<Long> ids, boolean peer) {
        if(!peer){
            throw new RuntimeException("Must be true to indicate the peer master");
        }

        Integer affected = 0;
        for (Long id : ids){
            affected += addRelation(id, masterId);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer removePeerList(long masterId, List<Long> ids) {
        Integer affected = 0;

        for (Long id : ids){
            affected += deleteRelation(masterId, id);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer removePeerList(long masterId, List<Long> ids, boolean peer) {
        if(!peer){
            throw new RuntimeException("Must be true to indicate the peer master");
        }

        Integer affected = 0;
        for (Long id : ids){
            affected += deleteRelation(id, masterId);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer resetPeerList(long masterId) {
        Map conditions = new HashMap();
        conditions.put(getMasterFieldName(), masterId);

        return getRelationMapper().deleteByMap(conditions);
    }

    @Override
    @Transactional
    public Integer resetPeerList(long masterId, boolean peer) {
        if(!peer){
            throw new RuntimeException("Must be true to indicate the peer master");
        }
        Map conditions = new HashMap();
        conditions.put(getMasterPeerFieldName(), masterId);

        return getRelationMapper().deleteByMap(conditions);
    }


    @Override
    public List<P> getPeerList(long masterId) {
        List<R> relations = getRelationMapper().selectList(new EntityWrapper<R>().eq(getMasterFieldName(), masterId));

        List<Long> ids = CRUD.getIds(relations, getMasterPeerFieldName());
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return getMasterPeerMapper().selectBatchIds(ids);
    }


    @Override
    public List<M> getPeerList(long masterId, boolean peer) {
        if(!peer){
            throw new RuntimeException("Must be true to indicate the peer master");
        }

        List<R> relations = getRelationMapper().selectList(new EntityWrapper<R>().eq(getMasterFieldName(), masterId));

        List<Long> ids = CRUD.getIds(relations, getMasterFieldName());
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        return getMasterMapper().selectBatchIds(ids);
    }

    @Override
    @Transactional
    public Integer deleteMaster(long masterId) {
        List<R> relations = getRelationMapper().selectList(new EntityWrapper<R>().eq(getMasterFieldName(), masterId));
        if(relations!=null && relations.size()>0){
            throw new BusinessException(BusinessCode.CRUD_DELETE_ASSOCIATED_MASTER);
        }
        return getMasterMapper().deleteById(masterId);
    }

    @Override
    @Transactional
    public Integer deleteMaster(long masterId, boolean peer) {
        if(!peer){
            throw new RuntimeException("Must be true to indicate the peer master");
        }

        List<R> relations = getRelationMapper().selectList(new EntityWrapper<R>().eq(getMasterPeerFieldName(), masterId));
        if(relations!=null && relations.size()>0){
            throw new BusinessException(BusinessCode.CRUD_DELETE_ASSOCIATED_MASTER);
        }
        return getMasterPeerMapper().deleteById(masterId);
    }

    @Override
    public R getRelation(long masterId, long masterPeerId) {
        List<R> relations = getRelationMapper().selectList(new EntityWrapper<R>()
                .eq(getMasterFieldName(), masterId).and()
                .eq(getMasterPeerFieldName(), masterPeerId));

        if(relations==null || relations.size()==0){
            return null;
        }

        if(relations.size()>1){
            throw new BusinessException(BusinessCode.CRUD_PEER_KEY_NOT_UNIQUE);
        }

        return relations.get(0);
    }

    @Override
    @Transactional
    public Integer addRelation(R relation) {
        return getRelationMapper().insert(relation);
    }

    @Override
    @Transactional
    public Integer removeRelation(long id) {
        return getRelationMapper().deleteById(id);
    }

    @Override
    @Transactional
    public Integer updateRelation(R relation) {
        return getRelationMapper().updateById(relation);
    }

    @Override
    @Transactional
    public Integer bulkAddRelations(List<R> relations) {
        Integer affected = 0;
        for(R r : relations){
            affected += getRelationMapper().insert(r);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer bulkRemoveRelations(List<Long> ids) {
        return getRelationMapper().deleteBatchIds(ids);
    }


    /**
     * private methods
     * @param masterId
     * @param masterPeerId
     * @return
     */
    @Transactional
    private Integer addRelation(long masterId, long masterPeerId) {
        JSONObject it = new JSONObject();
        it.put(getMasterFieldName(), masterId);
        it.put(getMasterPeerFieldName(), masterPeerId);

        R r = JSON.toJavaObject(it, relationClassName());

        return getRelationMapper().insert(r);
    }

    @Transactional
    private Integer deleteRelation(long masterId, long masterPeerId) {
        R r = getRelation(masterId, masterPeerId);
        if(r!=null) {
            Map options = new HashMap<String, Object>();
            options.put(getMasterFieldName(), masterId);
            options.put(getMasterPeerFieldName(), masterPeerId);

            return getRelationMapper().deleteByMap(options);
        }
        return 0;
    }

    private String getMasterFieldName(){
        return relationMatches()[0];
    }

    private String getMasterPeerFieldName(){
        return relationMatches()[1];
    }
}
