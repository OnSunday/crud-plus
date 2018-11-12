package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.CRUD;
import com.jfeat.crud.plus.CRUDServiceGroup;
import com.jfeat.crud.plus.GROUP;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Created by shawshining on 2017/8/11.
 */
public abstract class CRUDServiceGroupImpl<T> implements CRUDServiceGroup<T> {
    public static final String PN = "pn";

    /**
     * Get the group mapper
     * @return
     */
    abstract protected BaseMapper<T> getGroupMapper();


    @Override
    @Transactional
    public Integer createGroup(T t) {
        Integer affected = getGroupMapper().insert(t);
        if (affected == 1) {
            // DO nothing
        } else {
            throw new BusinessException(BusinessCode.CRUD_INSERT_FAILURE);
        }

        return affected;
    }

    @Override
    public T retrieveGroup(long id) {
        return getGroupMapper().selectById(id);
    }


    @Override
    @Transactional
    public Integer updateGroup(T t){
        return getGroupMapper().updateById(t);
    }

    @Override
    @Transactional
    public Integer updateGroup(T t, boolean all){
        if(all){
            return getGroupMapper().updateAllColumnById(t);
        }else {
            return getGroupMapper().updateById(t);
        }
    }


    @Override
    @Transactional
    public Integer deleteGroup(long id) {
        // check if children
        List<T> children = this.getGroupChildren(id);

        if(children!=null && children.size()>0){
            /// 尝试删除非空类别
            throw new BusinessException(BusinessCode.CRUD_DELETE_NOT_EMPTY_GROUP);
        }

        return getGroupMapper().deleteById(id);
    }

    @Override
    public List<T> getRootGroups() {
        List<T> records = getGroupMapper().selectList(
                new EntityWrapper<T>().isNull(true, GROUP.PID));

        return records;
    }

    @Override
    public List<T> getRootGroups(String fieldName, String type) {
        List<T> records = getGroupMapper().selectList(
                new EntityWrapper<T>().eq(GROUP.PID, null).eq(fieldName, type));

        return records;
    }

    @Override
    public List<T> getGroupChildren(long groupId) {
        List<T> records = getGroupMapper().selectList(
                new EntityWrapper<T>().eq(GROUP.PID, groupId));

        return records;
    }

    @Override
    public List<T> getGroupTuples() {
        return getGroupMapper().selectByMap(new HashMap<>());
    }

    @Override
    public List<T> getGroupTuples(String fieldName, String type) {
        return getGroupMapper().selectList(new EntityWrapper<T>().eq(fieldName, type));
    }

    /**
     * JSON 相关函数
     * @param groupId
     * @return
     */

    @Override
    public T getParentGroup(long groupId) {
        T t = retrieveGroup(groupId);

        // convert to JSON first, for getting pid
        JSONObject tt = CRUD.toJSONObject(t);
        Long pid = tt.getLong(GROUP.PID);

        if(pid==null){
            return null;
        }

        return retrieveGroup(pid);
    }


    /**
     * 整个分类转为树结构
     * @return
     */

    @Override
    public JSONObject toJSONObject() {
        List<T> rows = getGroupTuples();

        return GROUP.toJSONObject(rows);
    }

    @Override
    public JSONObject toJSONObject(String fieldName, String type) {
        List<T> rows = getGroupTuples(fieldName, type);

        return GROUP.toJSONObject(rows);
    }

    @Override
    public JSONObject toJSONObject(long groupId) {
        T t = retrieveGroup(groupId);
        JSONObject pn = CRUD.toJSONObject(t);

        T p = getParentGroup(groupId);
        if(p!=null) {
            String pname = CRUD.getFieldString(p, "name");
            if (pname != null) {
                pn.put(PN, pname);
            }
        }

        return pn;
    }
}
