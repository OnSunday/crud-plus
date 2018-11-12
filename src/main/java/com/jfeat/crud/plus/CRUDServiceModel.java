package com.jfeat.crud.plus;


import org.springframework.transaction.annotation.Transactional;

/**
 * Created by vincent on 2017/8/25.
 * Common modeling for multi slaves and child
 */
public interface CRUDServiceModel<T, M extends T>{
    /**
     * create master for overridden of CRUDServiceOnly, and for custom operation
     * @param m
     * @param filter
     * @return
     */
    @Transactional
    Integer createModel(M m, CRUDFilter<T> filter);


    /**
     * update master for overridden of CRUDServiceOnly, and for custom operation
     * @param m
     * @param filter
     * @return
     */
    @Transactional
    Integer updateModel(M m, CRUDFilter<T> filter);


    /**
     * retrieve master with slave items in
     * @param id
     * @param filter
     * @return
     */
    CRUDObject<M> retrieveModel(long id, CRUDFilter<T> filter);

    /**
     * delete master bundle with child
     * @param id
     * @return
     */
    @Transactional
    Integer deleteModel(long id);
}

