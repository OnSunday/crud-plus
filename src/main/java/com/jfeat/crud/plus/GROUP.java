package com.jfeat.crud.plus;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfeat.crud.base.util.StrKit;

import java.util.*;

/**
 * Created by vincenthuang on 14/09/2017.
 * 处理无限层级类别数据 （常用如 部门，职业 等层级关系）
 */
public class GROUP {
    public static final String ID = "id";
    public static final String PID = "pid";

    public static final String ITEMS = "items";

    /**
     * 取得所有顶层分组
     *
     * @param tuples
     * @param <T>
     * @return
     */
    public static <T> List<T> getRootGroups(List<T> tuples) {
        return getGroupChildren(tuples, null);
    }

    /**
     * 获取指定组的列表
     *
     * @param tuples
     * @param groupId
     * @param <T>
     * @return
     */
    public static <T> List<T> getGroupChildren(List<T> tuples, Long groupId) {

        List<T> children = new ArrayList<>();

        for (T it : tuples) {
            JSONObject tuple = CRUD.toJSONObject(it);

            Long pid = tuple.getLong(PID);
            if(pid==null && groupId==null){
                children.add((T) it);
            }else if(pid==null || groupId==null){
                // not equal
            }else if(pid.longValue()==groupId.longValue()){
                // both not null
                children.add((T) it);
            }
        }

        return children;
    }


    /**
     * 建立树状数据返回
     *
     * @param tuples
     * @param <T>
     * @return
     */
    public static <T> JSONObject toJSONObject(List<T> tuples) {
        JSONObject target = new JSONObject();

        JSONArray rootArray = handleRootGroups(tuples);
        target.put(ITEMS, rootArray);

        return target;
    }

    public static <T, C> JSONObject groupedBy(List<T> tuples, List<C> byTuples, String groupby) {
        JSONObject groups = toJSONObject(tuples);
        groupby = StrKit.toCamelCase(groupby);

        for (C by : byTuples) {
            JSONObject byJson = CRUD.toJSONObject(by);
            Object groupbyValue = byJson.get(groupby);

            JSONObject found = findChildGroup(groups, ITEMS, ID, groupbyValue);

            /// add it to
            if (found != null) {
                JSONArray items = found.containsKey(ITEMS) ? found.getJSONArray(ITEMS) : null;
                if (items == null) {
                    items = new JSONArray();
                    found.put(ITEMS, items);
                }

                items.add(byJson);
            }

        }

        return groups;
    }

    /**
     * 把指定域名作为分组标记，由域的值区分不同分组
     *
     * @param tuples
     * @param fieldName
     * @param <T>
     * @return
     */
    public static <T> Map<Object, List<T>> groupedByField(List<T> tuples, String fieldName) {
        Map<Object, List<T>> groups = new HashMap<>();

        for (T t : tuples) {
            JSONObject tuple = CRUD.toJSONObject(t);
            Object fieldValue = tuple.get(fieldName);
            if (fieldValue != null) {
                if (groups.containsKey(fieldValue)) {
                    List<T> group = groups.get(fieldValue);
                    if (group == null) {
                        group = new ArrayList<>();
                    }
                    group.add(t);
                }
            }
        }

        return groups;
    }

    /**
     * 取得指定域的组信息
     *
     * @param tuples
     * @param fieldName
     * @param fieldValue
     * @param <T>
     * @return
     */
    public static <T> List<T> groupedByField(List<T> tuples, String fieldName, String fieldValue) {
        List<T> group = new ArrayList<>();

        for (T t : tuples) {
            JSONObject tuple = CRUD.toJSONObject(t);
            Object tupleValue = tuple.get(fieldName);

            if (tupleValue != null) {
                if (tupleValue.toString().compareTo(fieldValue) == 0) {
                    group.add(t);
                }
            }
        }
        return group;
    }


    /**
     * iterate the whole json object
     *
     * @param group
     * @param key
     * @param value
     * @return
     */
    public static JSONObject findChildGroup(JSONObject group, String itemsKey, String key, Object value) {
        if(value==null){
            return null;
        }

        if (group.containsKey(key) && group.get(key)!=null && group.get(key).equals(value)) {
            return group;
        }

        /// if no child, we are interesting
        if(!group.containsKey(itemsKey)){

            Object childKeyValue = group.get(key);
            if(childKeyValue!=null) {

                //if (childKeyValue == value) {
                //    return group;
                //}
                //if(childKeyValue.toString().compareTo(value.toString())==0){
                //    return group;
                //}
                if (childKeyValue.equals(value)) {
                    return group;
                }
            }

        }else{
            /// has child, just find all the items

            JSONArray items = group.getJSONArray(itemsKey);

            Iterator<Object> iterator = items.iterator();
            while (iterator.hasNext()) {
                JSONObject item = (JSONObject) iterator.next();
                JSONObject found = findChildGroup(item, itemsKey, key, value);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * findGroupedItems
     * skip group item
     */
    public static List<Long> findGroupedItems(JSONObject group, String itemsKey) {
        List<Long> ids = new ArrayList<>();
        recurGroupedItems(group, itemsKey, ids);
        return ids;
    }

    private static void recurGroupedItems(JSONObject group, String itemsKey, List<Long> ids){

        /// if no child, we are interesting
        if(!group.containsKey(itemsKey)){
            if(group.containsKey(PID)){
                /// no interesting, skip
            }else{
                ids.add(group.getLong(ID));
            }

        }else{
            /// has items, no interesting, just recur

            JSONArray items = group.getJSONArray(itemsKey);

            Iterator<Object> iterator = items.iterator();
            while (iterator.hasNext()) {
                JSONObject item = (JSONObject) iterator.next();
                recurGroupedItems(item, itemsKey, ids);
            }
        }
    }


    /**
     * supported methods
     *
     * @param tuples
     * @param <T>
     * @return
     */

    private static <T> JSONArray handleRootGroups(List<T> tuples) {

        JSONArray rootArray = new JSONArray();

        List<T> roots = getRootGroups(tuples);

        for (T t : roots) {
            JSONObject item = CRUD.toJSONObject(t);

            List<T> children = getGroupChildren(tuples, CRUD.getPrimaryKey(t));

            recurGroupChildren(tuples, item, children);

            rootArray.add(item);
        }

        return rootArray;
    }

    private static <T> void recurGroupChildren(List<T> tuples, JSONObject current, List<T> children) {

        //List<JSONObject> currentChildren = new ArrayList<>();
        JSONArray currentChildren = new JSONArray();

        for (T t : children) {
            JSONObject item = CRUD.toJSONObject(t);

            List<T> nextChildren = getGroupChildren(tuples, CRUD.getPrimaryKey(t));

            currentChildren.add(item);

            if (nextChildren.size() != 0) {
                recurGroupChildren(tuples, item, nextChildren);

            } else {
                continue;
            }
        }

        current.put(ITEMS, currentChildren);
    }
}
