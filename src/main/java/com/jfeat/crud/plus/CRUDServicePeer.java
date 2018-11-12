package com.jfeat.crud.plus;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 */
public interface CRUDServicePeer<M, P, R>{
    @Transactional
    Integer addPeerList(long masterId,  List<Long> ids);
    @Transactional
    Integer addPeerList(long masterId,  List<Long> ids, boolean peer);
    @Transactional
    Integer removePeerList(long masterId, List<Long> ids);
    @Transactional
    Integer removePeerList(long masterId, List<Long> ids, boolean peer);
    @Transactional
    Integer resetPeerList(long masterId);
    @Transactional
    Integer resetPeerList(long masterId, boolean peer);

    List<P> getPeerList(long masterId);
    List<M> getPeerList(long masterId, boolean peer);

    // not empty not allow
    Integer deleteMaster(long masterId);
    Integer deleteMaster(long masterId, boolean peer);

    // relation
    R getRelation(long masterId, long masterPeerId);
    //JSONObject getRelationData(long masterId, long masterPeerId);

    Integer addRelation(R relation);
    Integer removeRelation(long id);
    Integer updateRelation(R relation);

    // bulk
    @Transactional
    Integer bulkAddRelations(List<R> relations);
    @Transactional
    Integer bulkRemoveRelations(List<Long> ids);
}

