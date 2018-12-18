package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.Ids;
import com.jfeat.crud.plus.CRUD;
import com.jfeat.crud.plus.CRUDFilter;
import com.jfeat.crud.plus.CRUDServiceSlave;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vincenthuang on 2017/8/11.
 * Provide slave services for master
 */
public abstract class CRUDServiceSlaveImpl<I> implements CRUDServiceSlave<I> {
    /**
     * only get the ids from items
     * @param items
     * @param <I>
     * @return
     */
    @Deprecated
    public static <I> List<Long> getItemListIds(List<I> items){
        List<Long> ids = new ArrayList<>();

        for(I i : items){
            JSONObject one = CRUD.toJSONObject(i);
            Long id = (Long)one.get(CRUD.primaryKey);
            ids.add(id);
        }

        return ids;
    }




    abstract protected BaseMapper<I> getSlaveItemMapper();
    abstract protected String masterFieldName();

    protected BaseMapper getMasterMapper(){
        return null;
    }

    protected void checkMasterExists(I item){
        /// check master exists first
        if(getMasterMapper()!=null) {
            if (masterFieldName() == null || "".equals(masterFieldName())) {
                throw new BusinessException(BusinessCode.CRUD_MASTER_KEY_NOT_PROVIDED);
            }
            Integer count = getMasterMapper().selectCount(new EntityWrapper().eq(masterFieldName(), masterFieldName()));
            if (count == null || count == 0) {
                throw new BusinessException(BusinessCode.CRUD_MASTER_NOT_EXISTS);
            }
        }
    }

    @Override
    @Transactional
    public Integer addSlaveItem(I item) {
        return addSlaveItem(item, null);
    }

    @Override
    @Transactional
    public Integer addSlaveItem(I item, CRUDFilter<I> filter) {
        if(filter!=null){
            filter.filter(item, true);
        }

        checkMasterExists(item);

        return getSlaveItemMapper().insert(item);
    }

    @Override
    @Transactional
    public Integer updateSlaveItem(I item) {
        checkMasterExists(item);

        return getSlaveItemMapper().updateById(item);
    }

    @Override
    @Transactional
    public Integer updateSlaveItem(I item, CRUDFilter<I> filter) {
        if(filter!=null){
            filter.filter(item, true);
        }

        checkMasterExists(item);

        return getSlaveItemMapper().updateById(item);
    }

    @Override
    @Transactional
    public Integer removeSlaveItem(long itemId) {
        return getSlaveItemMapper().deleteById(itemId);
    }

    @Override
    public I getSlaveItem(long itemId) {
        return getSlaveItemMapper().selectById(itemId);
    }

    @Override
    public List<I> getSlaveItemList() {
        return getSlaveItemMapper().selectByMap(new HashMap<>());
    }

    @Override
    public List<I> masterSelectSlaveItemList(String masterField) {
        List<I> is = getSlaveItemMapper().selectList(
                new EntityWrapper<I>().eq(masterFieldName(), masterField));
        return is;
    }
    @Override
    public List<I> masterSelectSlaveItemList(long masterId) {
        return masterSelectSlaveItemList(String.valueOf(masterId));
    }

    @Override
    @Transactional
    public Integer masterRemoveSlaveItemList(String masterField) {
        return getSlaveItemMapper().delete(new EntityWrapper<I>().eq(masterFieldName(), masterField));
    }
    @Override
    @Transactional
    public Integer masterRemoveSlaveItemList(long masterId) {
        return masterRemoveSlaveItemList(String.valueOf(masterId));
    }

    @Override
    @Transactional
    public Integer masterChangeSlaveItemList(long masterId, List<I> items) {
        return masterChangeSlaveItemList(String.valueOf(masterId), items);
    }

    @Override
    @Transactional
    public Integer masterChangeSlaveItemList(String masterField, List<I> items) {
        Integer affected = 0;

        /// bug: need to skip exist items
        //affected += masterRemoveSlaveItemList(masterId);
        //affected += bulkAppendSlaveItemList(items);

        /// 1. add the new ones
        /// 2. update the exist ones
        /// 3. remove the old ones

        /// get original slave items, delete the not exists one
        List<I> originalItems = masterSelectSlaveItemList(masterField);

        List<Long> skips = new ArrayList<>();

        for(I it : items){
            /// check new one
            JSONObject one = CRUD.toJSONObject(it);

            Long id = one.getLong(CRUD.primaryKey);
            if (id == null || id == 0) {
                /// get here , no id, this is new one

                //bug: add master reference id
                one.put(masterFieldName(), masterField);
                it = JSON.toJavaObject(one, (Class<I>)it.getClass());

                /// add new item
                affected += addSlaveItem(it);

            } else {
                /// find the slave item
                if (getSlaveItem(id) != null) {
                    /// get here , we are updating
                    affected += updateSlaveItem(it);

                    // updated, skip
                    skips.add(id);
                }
            }
        }

        List<Long> ids = new ArrayList<>();
        for (I i : originalItems) {
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

        if(ids!=null && ids.size()>0) {
            affected += getSlaveItemMapper().deleteBatchIds(ids);
        }

        return affected;
    }

    @Override
    public List<I> masterSelectSlaveItemList(String masterField, String condition, String conditionValue) {
        List<I> is = getSlaveItemMapper().selectList(
                new EntityWrapper<I>()
                        .eq(masterFieldName(), masterField)
                        .and()
                        .eq(condition, conditionValue)
        );
        return is;
    }
    @Override
    public List<I> masterSelectSlaveItemList(long masterId, String condition, String conditionValue) {
        return masterSelectSlaveItemList(String.valueOf(masterId), condition, conditionValue);
    }

    @Override
    @Transactional
    public Integer bulkRemoveSlaveItemList(List<Long> ids) {
        int affected = 0;
        for (Long itemId : ids) {
            affected += removeSlaveItem(itemId);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer bulkAppendSlaveItemList(List<I> items) {
        int affected = 0;
        for (I item : items) {
            affected += addSlaveItem(item);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer bulkUpdateSlaveItemList(List<I> items) {
        int affected = 0;
        for (I item : items) {
            affected += updateSlaveItem(item);
        }
        return affected;
    }

    @Override
    @Transactional
    public Integer bulkDelete(Ids ids) {
        Integer affected = 0;
        if (ids.getIds() != null && ids.getIds().size() > 0){
            for (Long id:ids.getIds()){
                affected += getMasterMapper().deleteById(id);
            }
        }
        return affected;
    }
}
