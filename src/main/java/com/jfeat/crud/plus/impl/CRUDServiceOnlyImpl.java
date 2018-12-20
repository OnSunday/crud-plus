package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.CRUD;
import com.jfeat.crud.plus.CRUDFilter;
import com.jfeat.crud.plus.CRUDServiceOnly;
import com.jfeat.crud.plus.META;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shawshining on 2017/8/11.
 */
public abstract class CRUDServiceOnlyImpl<T> implements CRUDServiceOnly<T> {

    abstract protected BaseMapper<T> getMasterMapper();

    @Override
    @Transactional
    public Integer createMaster(T t) {
        return createMaster(t, null);
    }

    @Override
    @Transactional
    public Integer createMaster(T t, CRUDFilter<T> filter) {
        if (filter != null) {
            filter.filter(t, true);
        }
        return getMasterMapper().insert(t);
    }

    @Override
    public T retrieveMaster(long id) {
        return getMasterMapper().selectById(id);
    }

    @Override
    @Transactional
    public Integer deleteMaster(long id) {
        return getMasterMapper().deleteById(id);
    }

    @Override
    public List<T> retrieveMasterList() {
        return getMasterMapper().selectByMap(new HashMap<>());
    }

    @Override
    @Transactional
    public Integer updateMaster(T t) {
        return getMasterMapper().updateAllColumnById(t);
    }

    @Override
    @Transactional
    public Integer updateMaster(T t, boolean all) {
        if (all) {
            return updateMaster(t);
        } else {
            return getMasterMapper().updateById(t);
        }
    }

    @Override
    @Transactional
    public Integer updateMaster(T t, CRUDFilter<T> filter) {
        if (filter != null) {
            filter.filter(t, false);
        }

        Long masterId = CRUD.getPrimaryKey(t);
        if (masterId == null || masterId == 0) {
            throw new BusinessException(BusinessCode.CRUD_MASTER_KEY_NOT_PROVIDED);
        }

        T originalOne = getMasterMapper().selectById(masterId);

        if (originalOne != null) {

            /// ignore some fields from record
            //meta: should not ignore status
            ArrayList<String> ignoreList = new ArrayList<>();
            if(META.ignoreStatus()){
                ignoreList.add(META.statusKey);
            }
            if(META.ignoreTimestamp()){
                ignoreList.add(META.createTime);
                ignoreList.add(META.lastModifiedTime);
            }
            if(filter!=null){
                String[] ignores = filter.ignore(false);
                if(ignores!=null) {
                    for (String item : ignores) {
                        ignoreList.add(item);
                    }
                }
            }
            String[] ignores = new String[ignoreList.size()];
            ignoreList.toArray(ignores);
            /// end add status

            // auto update lastModifiedTime
            JSONObject extra = null;
            if(META.ignoreTimestamp()){
                extra = new JSONObject();
                extra.put(META.lastModifiedTime, new Date());
            }

            T updatedOne = CRUD.copyFrom(originalOne, t, ignores, extra, true);

            return getMasterMapper().updateAllColumnById(updatedOne);
        }

        return 0;
    }


    @Override
    @Transactional
    public Integer bulkDeleteMasterList(List<Long> ids) {
        return getMasterMapper().deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public Integer bulkAppendMasterList(List<T> list) {
        Integer affected = 0;
        for (T item : list) {
            affected += getMasterMapper().insert(item);
        }
        return affected;
    }
}
