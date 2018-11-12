package com.jfeat.crud.plus;

import com.jfeat.crud.base.tips.Ids;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by vincent on 2017/8/25.
 */
public interface CRUDServiceSlave<I>{
    /// CRUD
    Integer addSlaveItem(I item);
    Integer addSlaveItem(I item, CRUDFilter<I> filter);
    Integer updateSlaveItem(I item);
    Integer updateSlaveItem(I item, CRUDFilter<I> filter);
    Integer removeSlaveItem(long itemId);
    I getSlaveItem(long itemId);
    List<I> getSlaveItemList();

    /// of Master
    List<I> masterSelectSlaveItemList(long masterId);
    Integer masterRemoveSlaveItemList(long masterId);
    @Transactional
    Integer masterChangeSlaveItemList(long masterId, List<I> items);
    /// additional
    List<I> masterSelectSlaveItemList(long masterId, String condition, String conditionValue);

    /// Bulk
    @Transactional
    Integer bulkRemoveSlaveItemList(List<Long> ids);
    @Transactional
    Integer bulkAppendSlaveItemList(List<I> items);
    @Transactional
    Integer bulkUpdateSlaveItemList(List<I> items);
    @Transactional
    Integer bulkDelete(Ids ids);
}
