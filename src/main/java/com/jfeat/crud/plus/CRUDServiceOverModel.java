package com.jfeat.crud.plus;

import com.jfeat.crud.base.tips.Ids;

/**
 * Created by vincent on 2017/8/25.
 */
public interface CRUDServiceOverModel<T, M extends T> extends
        CRUDServiceOnly<T>,
        CRUDServiceModelResult<T, M> {
    CRUDObject<M> retrieveMasterModel(Long id);

    CRUDObject<M> retrieveMasterModel(Long id, CRUDFilterResult<T> filter);

    @Deprecated
    Integer bulkDelete(Ids ids);
}

