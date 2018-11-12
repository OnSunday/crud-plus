package com.jfeat.crud.plus.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.util.StrKit;
import com.jfeat.crud.plus.*;

/**
 * Created by vincenthuang on 2017/8/11.
 * Provide slave services for master
 */
public class CRUDServiceChildAgent<T, M extends T, C> implements CRUDServiceChild<C>,
        CRUDServiceModelResult<T, M> {
    /**
     * just put child data into master child field
     * @param masterObject
     * @param childReference  field reference in master table
     *                        {
     *                          "name":"",
     *                          "profile_id": 100,  //"this is childReference"
     *                        }
     * @param child
     * @Param result  save some result data
     * @return
     */
    //Integer updateChild(JSONObject masterObject, String childReference, C child);


    /**
     * Get the master field in child mapper/table
     * @return
     */
    protected BaseMapper<T> getMasterMapper;
    protected BaseMapper<C> getChildMapper;

    protected String childReferenceName;    /// child reference field name referred in master table

    /// Child Class Name
    protected Class<C>  childClassName;

    public CRUDServiceChildAgent(BaseMapper<T> masterMapper, BaseMapper<C> childMapper,
                                 String childReferenceName, Class<C> childClassName ) {
        this.getMasterMapper = masterMapper;
        this.getChildMapper = childMapper;
        this.childReferenceName = childReferenceName;
        this.childClassName = childClassName;

        ///
        this.childReferenceNameCamelCase = StrKit.toCamelCase(childReferenceName);
    }

    /// Master Class Name
    /// used to newInstance instead of select from database
    private Class<T> masterClassName;

    public Class<T> getMasterClassName() {
        return masterClassName;
    }

    public void setMasterClassName(Class<T> masterClassName) {
        this.masterClassName = masterClassName;
    }


    /// Master Object, used to skip query
    //private JSONObject rawMasterObject;

    private String childReferenceNameCamelCase;


    @Override
    public C getChild(long masterId) {
        Long childId = getChildReference(masterId);
        return getChildMapper.selectById(childId);
    }

    @Override
    public Integer updateChild(long masterId, C child) {
        T t = getMasterMapper.selectById(masterId);

        //Integer affected = getMasterMapper.insert(m);
        Integer affected = 0;


        /// get child reference
        JSONObject masterObject = CRUD.toJSONObject(t);
        Long childReference = masterObject.getLong(childReferenceNameCamelCase);

        JSONObject childObject = CRUD.toJSONObject(child);
        if(childReference!=null) {
            /// means update

            childObject.put(CRUD.primaryKey, childReference);
            C updatedChild = JSON.parseObject(childObject.toJSONString(), (Class<C>) child.getClass());

            affected += getChildMapper.updateById(updatedChild);

        }else{

            //// means insert
            affected += getChildMapper.insert(child);
            masterObject.put(childReferenceNameCamelCase, CRUD.getPrimaryKey(child));
        }

        return affected;
    }

    @Override
    public Integer deleteChild(long masterId) {
        Integer affected = 0;

        T t = getMasterMapper.selectById(masterId);
        JSONObject jsonObject = CRUD.toJSONObject(t);

        String childReferenceNameCamelCase = StrKit.toCamelCase(childReferenceName);
        Long childId = jsonObject.getLong(childReferenceNameCamelCase);

        if(childId!=null && childId > 0){
            affected += getChildMapper.deleteById(childId);
        }

        /// update master filed
        JSONObject toUpdate = new JSONObject();
        toUpdate.put(childReferenceNameCamelCase, 0);

        T updated = JSON.parseObject(toUpdate.toString(), (Class<T>) t.getClass());
        affected += getMasterMapper.updateById(updated);

        return affected;
    }

    public Integer updateChild(JSONObject masterObject, String childReference, C child) {

        Long childId = masterObject.getLong(childReference);

        /// if no child in childMapper, create one
        Integer affected = 0;
        if(childId==null || childId==0){
            affected += getChildMapper.insert(child);

            if(affected==1){
                /// get here, we success, update master child field
                childId = CRUD.getPrimaryKey(child);

                /// update to master object
                String childReferenceCamelCase = StrKit.toCamelCase(childReference);
                masterObject.put(childReferenceCamelCase, childId);

                /// update to mapper
                try{
                    T t =  masterClassName.newInstance();
                    t = CRUD.copyFrom(t, masterObject, true);
                    int isUpdated = getMasterMapper.updateById(t);

                    if (isUpdated == 0) {
                        throw new BusinessException(BusinessCode.CRUD_UPDATE_FAILURE);
                    }

                }catch (IllegalAccessException e){
                    throw new RuntimeException(e);
                }catch (InstantiationException e){
                    throw new RuntimeException(e);
                }

            }else {

                throw new BusinessException(BusinessCode.CRUD_INSERT_FAILURE);
            }
        }else{
            /// already has child, just update
            affected += getChildMapper.update(child, new EntityWrapper<>(child).eq(CRUD.primaryKey, childId));

        }

        return affected;
    }

    /**
     * belows for child CRUDServiceModel
     */
    @Override
    public Integer createMaster(M m, CRUDFilterResult<T> filter, String field, CRUDHandler<T,M> handler) {
        if(handler!=null){
            throw new RuntimeException("Must be handled by tool");
        }

        if(filter != null){
            filter.filter(m, true);
        }

        //Integer affected = getMasterMapper.insert(m);
        Integer affected = 0;

        /// get the new inserted master id
        JSONObject masterObject = CRUD.toJSONObject(m);

        /// handle child item if contains child item key
        if (masterObject.containsKey(field)) {

            JSONObject childObject = masterObject.getJSONObject(field);

            if(childObject!=null) {

                /// convert json array into model
                C child = JSON.toJavaObject(childObject, childClassName);

                if (child != null) {
                    /// attach slave data into new class
                    affected += updateChild(masterObject, childReferenceName, child);

                    if(filter!=null) {
                        /// get the child id, and remember that in agent
                        filter.result().put(childReferenceNameCamelCase, masterObject.getLong(childReferenceNameCamelCase));

                        /// add primary key
                        if (!filter.result().containsKey(CRUD.primaryKey)) {
                            filter.result().put(CRUD.primaryKey, CRUD.getPrimaryKey(masterObject));
                        }
                    }
                }
            }
        }

        return affected;
    }

    @Override
    public Integer updateMaster(M m, CRUDFilterResult<T> filter, String field, CRUDHandler<T,M> handler) {
        if(handler!=null){
            throw new RuntimeException("Must be handled by tool");
        }

        if(filter != null){
            filter.filter(m, false);
        }

        JSONObject modelObject = CRUD.toJSONObject(m);

        Integer affected = 0;

        /// update child if it has
        if(modelObject.containsKey(field)) {
            JSONObject childObject = modelObject.getJSONObject(field);

            if(childObject!=null) {

                Long childReference = 0L;

                /// check childReference exist first
                if(!childObject.containsKey(CRUD.primaryKey)){
                    childReference = modelObject.getLong(childReferenceNameCamelCase);
                    childObject.put(CRUD.primaryKey, childReference);
                }

                /// update into database
                C c = CRUD.castObject(childObject, childClassName);

                affected += getChildMapper.updateById(c);
            }
        }


        /// do not update master by child agent
        /// do it by master
        //affected += getMasterMapper.updateById(m);

        return affected;
    }

    @Override
    public CRUDObject<M> retrieveMaster(long masterId, CRUDFilterResult<T> filter, String field, CRUDHandler<T,M> handler) {
        if(handler!=null){
            throw new RuntimeException("Must be handled by tool");
        }

        JSONObject rawMasterObject = null;
        if(rawMasterObject==null) {
            T t = getMasterMapper.selectById(masterId);
            rawMasterObject = CRUD.toJSONObject(t);
        }

        /// get child data if it has
        if(rawMasterObject.containsKey(childReferenceNameCamelCase)) {
            Long childReference = rawMasterObject.getLong(childReferenceNameCamelCase);

            if(childReference!=null) {
                C c = getChildMapper.selectById(childReference);

                if(c!=null) {
                    JSONObject childObject = CRUD.toJSONObject(c);

                    JSONObject masterObject = (JSONObject) rawMasterObject.clone();
                    masterObject.put(field, childObject);

                    return new CRUDObject<M>().ignore(masterObject, filter==null? null: filter.ignore(true));
                }
            }

        }else{
            throw new RuntimeException("No child reference field found: "+ field);
        }

        return new CRUDObject<M>().ignore(rawMasterObject, filter==null?null:filter.ignore(true));
    }

    @Override
    public Integer deleteMaster(long masterId, String field) {
        /// delete child first
        Long childReference = getChildReference(masterId);

        Integer affected = 0;

        if(childReference!=null){
            affected += getChildMapper.deleteById(childReference);
        }

        /// delete master
        /// fix: do not delete master, do it by master
        ///affected += getMasterMapper.deleteById(masterId);

        return affected;
    }

    private Long getChildReference(long masterId){
        T t = getMasterMapper.selectById(masterId);

        JSONObject masterObject = CRUD.toJSONObject(t);

        Long childReference = masterObject.getLong(childReferenceNameCamelCase);

        return childReference;
    }

}
