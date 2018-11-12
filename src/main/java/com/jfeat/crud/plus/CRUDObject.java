package com.jfeat.crud.plus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by vincenthuang on 27/08/2017.
 */
public class CRUDObject<T> {
    private JSONObject mJSONObject;
    private Object raw;  /// T.class object

    public CRUDObject(){}

    public CRUDObject merge(CRUDObject crud){
        if(crud!=null) {
            JSONObject jsonObject = crud.mJSONObject;
            if (mJSONObject == null) {
                mJSONObject = crud.mJSONObject;
            } else {
                CRUD.copyFrom(mJSONObject, jsonObject, false);
            }
        }
        return this;
    }

    public CRUDObject<T> ignore(T t, String[] ignores){
        mJSONObject = CRUD.ignoreAs(t, ignores);
        return this;
    }

    public CRUDObject<T> ignore(JSONObject jsonObject, String[] ignores){
        mJSONObject = jsonObject;

        if(ignores!=null) {
            CRUD.ignore(mJSONObject, ignores);
        }
        return this;
    }

    public CRUDObject<T> ignore(String[] ignores){
        if(ignores!=null) {
            CRUD.ignore(mJSONObject, ignores);
        }
        return this;
    }

    public CRUDObject<T> from(JSONObject jsonObject){
        mJSONObject = jsonObject;
        return this;
    }

    public CRUDObject<T> from(T t){
        raw = t;
        mJSONObject = CRUD.toJSONObject(t);
        return this;
    }

    public T toJavaObject(Class<T> cls){
        if(raw!=null){
            return (T)raw;
        }
        return JSON.toJavaObject(mJSONObject, cls);
    }

    public JSONObject toJSONObject(){
        return this.mJSONObject;
    }

    @Override
    public String toString(){
        if(mJSONObject==null){
            return super.toString();
        }
        return this.mJSONObject.toString();
    }
}
