package com.jfeat.crud.plus;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by vincent on 2017/8/25.
 */
public interface CRUDServiceGroup<T> {
    /// CRUD
    Integer createGroup(T t);
    Integer updateGroup(T t);
    Integer updateGroup(T t, boolean all);
    T retrieveGroup(long id);
    Integer deleteGroup(long id);

    /// Group
    List<T> getRootGroups();
    List<T> getRootGroups(String typeField, String type);
    List<T> getGroupChildren(long groupId);
    List<T> getGroupTuples();
    List<T> getGroupTuples(String typeField, String type);

    /// JSON
    T getParentGroup(long groupId);
    JSONObject toJSONObject();
    JSONObject toJSONObject(String typeField, String type);

    // get Group with PN (parent name)
    JSONObject toJSONObject(long groupId);
}
