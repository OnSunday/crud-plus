package com.jfeat.crud.plus;


/**
 * Created by vincent on 2017/8/25.
 * Common modeling for multi slaves and child
 */
public interface CRUDServiceModelResult<T, M extends T> {
    // there is no way to create master, because master_id is not yet created while
    // slaves are going to be inserted with master_id
    // fix:  master_id is updated after inserted.

    /**
     * create master for overridden of CRUDServiceOnly, and for custom operation
     * @param m
     * @param filter
     * @param field used to specify the field name in JSONObject
     * @return
     */
    Integer createMaster(M m, CRUDFilterResult<T> filter, String field, CRUDHandler<T, M> handler);


    /**
     * update master for overridden of CRUDServiceOnly, and for custom operation
     * @param m
     * @param filter
     * @param field used to specify the field name in JSONObject
     * @return
     */
    Integer updateMaster(M m, CRUDFilterResult<T> filter, String field, CRUDHandler<T, M> handler);


    /**
     * retrieve master with slave items in
     * @param id
     * @param filter
     * @param field used to specify the field name in JSONObject
     * @return
     */
    CRUDObject<M> retrieveMaster(long id, CRUDFilterResult<T> filter, String field, CRUDHandler<T, M> handler);


    /**
     * delete master bundle with child
     * @param id
     * @return
     */
    Integer deleteMaster(long id, String field);

}

