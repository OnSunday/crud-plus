package com.jfeat.crud.plus;

/**
 * Created by vincent on 2017/8/25.
 */
public interface CRUDServiceChild<C>{

    /**
     * get child of master
     * @param masterId
     * @return
     */
    C getChild(long masterId);

    /**
     * update child of master
     * @param masterId
     * @param child
     * @return
     */
    Integer updateChild(long masterId, C child);

    /**
     * delete child
     */
    Integer deleteChild(long masterId);
}


