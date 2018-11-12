package com.jfeat.crud.plus;

/**
 * Created by vincent on 2017/8/25.
 * Only one Slave in Master
 */
public interface CRUDServiceOverModelOne<T, M extends T, I> extends
        CRUDServiceOnly<T>,
        CRUDServiceModelResult<T, M>{
    CRUDObject<M> retrieveMasterModel(Long id);
    CRUDObject<M> retrieveMasterModel(Long id, CRUDFilterResult<T> filter);
}
