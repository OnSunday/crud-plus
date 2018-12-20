package com.jfeat.crud.plus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;

/**
 * Created by vincent on 2017/8/25.
 * CRUD util
 */
public abstract class CRUD<T> {
    public static final String primaryKey = "id";
    public static final String ONE_MANY_LINK_SYMBOL = "$_$";

    public static <T> JSONObject toJSONObject(T t) {
        return JSON.parseObject(JSONObject.toJSONString(t, SerializerFeature.WriteDateUseDateFormat));
    }
    public static JSONObject[] toJSONArray(JSONArray array) {
        JSONObject[] list = new JSONObject[array.size()];
        array.toArray(list);
        return list;
    }
    public static <T> JSONArray toJSONOArray(List<T> items) {
        JSONArray arr = new JSONArray();
        for (T item : items) {
            JSONObject it = CRUD.toJSONObject(item);
            arr.add(it);
        }
        return arr;
    }
    public static <T> List<T> toJavaObjectList(JSONArray array, Class<T> clazz) {
        List<T> list = new ArrayList<>();

        Iterator<Object> it = array.iterator();
        while (it.hasNext()) {
            JSONObject item = (JSONObject) it.next();
            T t = JSON.parseObject(item.toString(), clazz);
            list.add(t);
        }

        return list;
    }
    /**
     * check whether a JSONObject is simple without child JSONObject/JSONArray
     *
     * @param target
     * @return
     */
    public static boolean checkSimple(JSONObject target) {

        boolean isSimple = true;

        Iterator<String> it = target.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            Object val = target.get(key);

            if (val instanceof JSONObject) {
                isSimple = false;
                break;
            }

            if (val instanceof JSONArray) {
                isSimple = false;
                break;
            }
        }

        return isSimple;
    }

    /**
     * 取得类型 T 的关键字段 Default: id
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> Long getPrimaryKey(T t) {
        return Long.parseLong(getPrimaryKey(t, primaryKey));
    }

    /**
     * 取得类型 T 的关键字段primaryKey
     * @param t
     * @param primaryKey
     * @return
     **/
    public static <T> String getPrimaryKey(T t, String primaryKey) {
        JSONObject json = CRUD.toJSONObject(t);
        if (!json.containsKey(primaryKey)) {
            throw new RuntimeException("Fail to find primary key:" + primaryKey);
        }

        return json.getString(primaryKey);
    }

    /**
     * @param json
     * @return
     **/
    public static Long getPrimaryKey(JSONObject json) {
        return Long.parseLong(getPrimaryKey(json, primaryKey));
    }

    /**
     * @param json
     * @param primaryKey
     * @return
     **/
    public static String getPrimaryKey(JSONObject json, String primaryKey) {
        if (!json.containsKey(primaryKey)) {
            throw new RuntimeException("Fail to find primary key:" + primaryKey);
        }
        return json.getString(primaryKey);
    }

    public static <T> List<Long> getIds(List<T> items) {
        return getIds(items, CRUD.primaryKey);
    }

    public static <T> List<Long> getIds(List<T> items, String field) {
        List<Long> ids = new ArrayList<>();
        for (T i : items) {
            JSONObject one = CRUD.toJSONObject(i);
            Long id = (Long) one.get(field);
            ids.add(id);
        }
        return ids;
    }

    public static <T> String getFieldString(T t, String field) {
        JSONObject json = CRUD.toJSONObject(t);
        if (json.containsKey(field)) {
            return json.getString(field);
        }
        return null;
    }

    public static <T> Long getFieldLong(T t, String field) {
        JSONObject json = CRUD.toJSONObject(t);
        if (json.containsKey(field)) {
            return json.getLong(field);
        }
        return null;
    }

    /**
     * 转换数据类型D 到新类型 T
     *
     * @param t     数据对象
     * @param clazz
     * @param <M>   新类型
     * @return
     */
    public static <T, M extends T> M castObject(T t, Class<M> clazz) {
        JSONObject clientObject = JSON.parseObject(JSON.toJSONString(t, SerializerFeature.WriteDateUseDateFormat));
        return JSON.parseObject(clientObject.toString(), clazz);
    }

    public static <T, M extends T> T castObject(M m) {
        return JSON.parseObject(JSON.toJSONString(m, SerializerFeature.WriteDateUseDateFormat), (Class<T>) m.getClass().getSuperclass());
    }



    /**
     * 从类型 T 中 忽略字段 ignores，生成新的不包括 ignores 字段的 JSONObject
     *
     * @param t
     * @param ignores
     * @param <T>
     * @return
     */
    public static <T> JSONObject ignoreAs(T t, String[] ignores) {
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(t, SerializerFeature.WriteDateUseDateFormat));

        if (ignores != null && ignores.length > 0) {
            for (String field : ignores) {
                if(field!=null && jsonObject.containsKey(field)) {
                    jsonObject.remove(field);
                }
            }
        }

        return jsonObject;
    }

    /**
     * ignore some field
     * @param t
     * @param ignores
     * @param <T>
     * @return
     */
    public static <T> T ignore(T t, String[] ignores) {
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(t, SerializerFeature.WriteDateUseDateFormat));

        boolean updated = false;
        if (ignores != null && ignores.length > 0) {
            for (String field : ignores) {
                if(field!=null && jsonObject.containsKey(field)) {
                    jsonObject.remove(field);
                    updated = true;
                }
            }
        }
        if(updated) {
            return JSON.toJavaObject(jsonObject, (Class<T>) t.getClass());
        }

        return t;
    }

    public static void ignore(JSONObject jsonObject, String[] fields) {
        if (fields != null && jsonObject != null) {
            for (String field : fields) {
                if(field!=null && jsonObject.containsKey(field)) {
                    jsonObject.remove(field);
                }
            }
        }
    }

    /**
     * 禁止更新某项值，一般用于忽略更新实体状态
     * @param t   要更新的item
     * @param originalOne  原数据库item
     * @param ignoreKey  要忽略的key
     * @param <T>
     * @return
     */
    public static <T> boolean ignoreKeyUpdated(T t, T originalOne, String ignoreKey){

        /// ignore status change directly
        JSONObject jsonObject = CRUD.toJSONObject(t);
        if (jsonObject.containsKey(ignoreKey)) {

            String keyValue = jsonObject.getString(ignoreKey);
            if(keyValue!=null && keyValue.length()>0) {
                // get primaryId for original one
                /*Long masterId = CRUD.getPrimaryKey(t);
                if (masterId == null || masterId == 0) {
                    throw new CRUDException(CRUDCode.CRUD_MASTER_KEY_NOT_PROVIDED);
                }*/
                JSONObject originalObject = CRUD.toJSONObject(originalOne);
                String originalKeyValue = originalObject.getString(ignoreKey);
                if(originalKeyValue==null || keyValue.compareTo(originalKeyValue)!=0){
                    throw new BusinessException(BusinessCode.ErrorStatus.getCode(), String.format("状态错误：[%s] 禁止更新", ignoreKey));
                }
            }
        }

        /// always true
        return true;
    }

    /**
     * 更新类型 T 里的字段 from, 更新值为 fromValue
     *
     * @param t
     * @param from
     * @param fromValue
     * @param <T>
     * @return
     */
    public static <T> T copyFrom(T t, String from, Object fromValue) {

        JSONObject target = toJSONObject(t);

        if (target.containsKey(from)) {
            target.put(from, fromValue);
        }

        return JSON.parseObject(target.toJSONString(), (Class<T>) t.getClass());
    }

    /**
     * 更新 from 里面的所有值到 类型 T
     *
     * @param t
     * @param from
     * @return
     */
    public static <T> T copyFrom(T t, T from, boolean override) {
        JSONObject target = JSON.parseObject(JSON.toJSONString(t, WriteMapNullValue));

        JSONObject fromObject = JSON.parseObject(JSON.toJSONString(from, WriteMapNullValue));

        copyFrom(target, fromObject, override);

        return JSON.parseObject(target.toJSONString(), (Class<T>) t.getClass());
    }

    public static <T> T copyFrom(T t, JSONObject from, boolean override) {
        JSONObject target = JSON.parseObject(JSON.toJSONString(t, WriteMapNullValue));

        copyFrom(target, from, override);

        return JSON.parseObject(target.toJSONString(), (Class<T>) t.getClass());
    }

    /**
     * @param target
     * @param from
     */
    public static void copyFrom(JSONObject target, JSONObject from, boolean override) {
        Iterator<String> it = from.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object copyValue = from.get(key);

            /// just put into
            if (override) {
                target.put(key, copyValue);

            } else {
                /// not override, not contains or null
                if ((!target.containsKey(key))) {
                    target.put(key, copyValue);
                } else if (target.get(key) == null) {
                    target.put(key, copyValue);
                }
            }
        }
    }

    /**
     * 从类型 from 忽略字段 ignoreFields，并复制其他字段的值到类型 t
     *
     * @param t
     * @param from
     * @param ignores
     * @param <T>
     * @return
     */
    public static <T> T copyFrom(T t, T from, String[] ignores, boolean override) {
        JSONObject ignored = ignoreAs(from, ignores);

        T copied = copyFrom(t, ignored, override);

        return copied;
    }

    public static <T> T copyFrom(T t, T from, String[] ignores, JSONObject extra, boolean override) {
        JSONObject ignored = ignoreAs(from, ignores);

        // add extra data
        if(extra!=null) {
            copyFrom(ignored, extra, override);
        }

        T copied = copyFrom(t, ignored, override);

        return copied;
    }


    /**
     * copy from selection, and replace with new key from selectionMapping
     * @param target  the object to be updated
     * @param from  the source object data from
     * @param selection   select some key from the source object
     * @param selectionMapping  replace with new key for the key
     */
    public static void copyFrom(JSONObject target, JSONObject from, String[] selection, String[] selectionMapping) {
        if (selection != null) {

            int i=0;
            for (String key : selection) {
                Object value = from.get(key);
                if (value == null || value instanceof JSONArray) {
                    // skip array
                    i++;
                    continue;
                }

                String newKey = selectionMapping[i];

                if (from.get(key) != null) {
                    target.put(newKey, from.get(key));
                }

                i++;
            }
        }
    }

    /**
     * copy from selection, and replace with new key with prefix
     * @param target the object to be updated
     * @param from the source object data from
     * @param selection select some key from the source object
     * @param prefix  replace with new key with prefix added
     */
    public static void copyFrom(JSONObject target, JSONObject from, String[] selection, String prefix) {
        if (selection != null) {

            for (String key : selection) {
                Object value = from.get(key);
                if (value == null || value instanceof JSONArray) {
                    // skip array
                    continue;
                }

                String newKey = prefix == null ? key : prefix + "_" + key;

                if (from.get(key) != null) {
                    target.put(newKey, from.get(key));
                }
            }
        }
    }

}
