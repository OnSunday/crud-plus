package com.jfeat.crud.plus;

import java.util.List;

/**
 * Created by vincent on 2017/8/25.
 */
public interface CRUDServiceOnly<T>{

    /// CRUD
    Integer createMaster(T t);
    Integer createMaster(T t, CRUDFilter<T> filter);
    Integer updateMaster(T t);
    Integer updateMaster(T t, boolean all);
    Integer updateMaster(T t, CRUDFilter<T> filter);
    T retrieveMaster(long id);
    Integer deleteMaster(long id);
    List<T> retrieveMasterList();

    /// Batch
    Integer bulkDeleteMasterList(List<Long> ids);
    Integer bulkAppendMasterList(List<T> list);
}
