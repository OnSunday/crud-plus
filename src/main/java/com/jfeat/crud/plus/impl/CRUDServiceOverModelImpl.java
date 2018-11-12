package com.jfeat.crud.plus.impl;

import com.alibaba.fastjson.JSONObject;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.Ids;
import com.jfeat.crud.plus.*;
import com.jfeat.crud.plus.agent.CRUDServiceChildAgent;
import com.jfeat.crud.plus.agent.CRUDServiceSlaveAgent;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vincent on 2017/8/11.
 * Used to implement multi slaves and child
 */
public abstract class CRUDServiceOverModelImpl<T, M extends T> extends CRUDServiceOnlyImpl<T>
        implements CRUDServiceOverModel<T, M> {

    private List<CRUDServiceSlaveAgent> slaveModelingAgents = null;
    private List<CRUDServiceChildAgent> childAgents = null;
    private Map<Object, String> slaveKeyNames = new HashMap<>();
    private Map<Object, String> childKeyNames = new HashMap<>();

    protected abstract String[] slaveFieldNames();

    protected abstract String[] childFieldNames();

    protected abstract FIELD onSlaveFieldItem(String field);

    protected abstract FIELD onChildFieldItem(String field);

    protected abstract Class<T> masterClassName();

    protected abstract Class<M> modelClassName();

    public CRUDServiceOverModelImpl() {
    }

    private boolean mInited = false;

    protected void init() {
        if (!mInited) {
            mInited = true;

            String[] fieldNames = slaveFieldNames();
            if (fieldNames != null && fieldNames.length > 0) {
                slaveModelingAgents = new ArrayList<>();

                for (String field : slaveFieldNames()) {
                    FIELD _field = onSlaveFieldItem(field);

                    CRUDServiceSlaveAgent agent = new CRUDServiceSlaveAgent<T, M, Object>(
                            getMasterMapper(), _field.getItemMapper(), _field.getItemFieldName(), _field.getItemClassName()
                    );
                    agent.setMasterClassName(masterClassName());

                    slaveModelingAgents.add(agent);

                    /// record the slaveKeyName by agent
                    slaveKeyNames.put(agent, field);
                }
            }


            /// handle children
            String[] childNames = childFieldNames();
            if (childNames != null && childNames.length > 0) {
                childAgents = new ArrayList<>();

                for (String field : childFieldNames()) {
                    FIELD _field = onChildFieldItem(field);

                    CRUDServiceChildAgent agent = new CRUDServiceChildAgent<T, M, Object>(
                            getMasterMapper(), _field.getItemMapper(), _field.getItemFieldName(), _field.getItemClassName()
                    );
                    agent.setMasterClassName(masterClassName());

                    childAgents.add(agent);

                    /// record the childKeyName by agent
                    childKeyNames.put(agent, field);
                }
            }
        }
    }

    @Override
    @Transactional
    public Integer createMaster(M m, CRUDFilterResult<T> filter, String field, CRUDHandler<T, M> handler) {
        if (field != null && field.length() > 0) {
            throw new RuntimeException("field name has been provided by individual slave agent.");
        }
        if (filter != null) {
            filter.filter(m, true);
        }

        //fix: id for not AUTO_INCREMENT, id=null not allowed
        // change into t, and cast back to m
        T t = CRUD.castObject(m);

        Integer affected = getMasterMapper().insert(t);
        JSONObject insertedJSONObject = CRUD.toJSONObject(t);
        if (affected == 0) {
            throw new BusinessException(BusinessCode.CRUD_INSERT_FAILURE);
        }
        /// copy back
        m = CRUD.copyFrom(m, insertedJSONObject, false);
        // }}}

        if (handler != null) {
            affected += handler.onHandleMaster(m, field, CRUDHandler.CREATE);
            return affected;
        }

        //// create master with slaves and children by tool
        init();
        if (field != null && field.length() > 0) {
            throw new RuntimeException("field name has been provided by individual slave agent.");
        }

        /// foreach slaves
        if (slaveModelingAgents != null) {
            for (CRUDServiceSlaveAgent agent : slaveModelingAgents) {
                affected += agent.createMaster(m, filter, slaveKeyNames.get(agent), null);
            }
        }

        /// append child data
        if (childAgents != null) {
            for (CRUDServiceChildAgent agent : childAgents) {
                affected += agent.createMaster(m, filter, childKeyNames.get(agent), null);
            }
        }

        if (filter != null) {
            filter.result().put(CRUD.primaryKey, CRUD.getPrimaryKey(insertedJSONObject));
        }

        return affected;
    }

    @Override
    @Transactional
    public Integer updateMaster(M m, CRUDFilterResult<T> filter, String field, CRUDHandler<T, M> handler) {
        Integer affected = updateMaster(m, filter);

        if (handler != null) {
            affected += handler.onHandleMaster(m, field, CRUDHandler.UPDATE);
            return affected;
        }

        //// create master with slaves and children by tool
        init();
        if (field != null && field.length() > 0) {
            throw new RuntimeException("field name has been provided by individual slave agent.");
        }


        /**
         * handle update
         */

        /// foreach slaves
        if (slaveModelingAgents != null) {
            for (CRUDServiceSlaveAgent agent : slaveModelingAgents) {
                affected += agent.updateMaster(m, filter, slaveKeyNames.get(agent), null);
            }
        }

        /// append child data
        if (childAgents != null) {
            for (CRUDServiceChildAgent agent : childAgents) {
                affected += agent.updateMaster(m, filter, childKeyNames.get(agent), null);
            }
        }

        return affected;
    }

    @Override
    public CRUDObject<M> retrieveMaster(long masterId, CRUDFilterResult<T> filter, String field, CRUDHandler<T, M> handler) {
        if (field != null && field.length() > 0) {
            throw new RuntimeException("field name has been provided by individual slave agent.");
        }

        /// check masterId is invalid, or return null
        T t = getMasterMapper().selectById(masterId);
        if (t == null) {
            return null;
        }

        if (handler != null) {
            M m = CRUD.castObject(t, modelClassName());

            handler.onHandleMaster(m, field, CRUDHandler.RETRIEVE);
            return new CRUDObject<M>().from(m);
        }

        //// create master with slaves and children by tool
        init();

        CRUDObject<M> crud = new CRUDObject<>();

        /// foreach slaves
        if (slaveModelingAgents != null) {
            for (CRUDServiceSlaveAgent agent : slaveModelingAgents) {
                crud.merge(agent.retrieveMaster(masterId, filter, slaveKeyNames.get(agent), null));
            }
        }

        /// append child data
        if (childAgents != null) {
            for (CRUDServiceChildAgent agent : childAgents) {
                crud.merge(agent.retrieveMaster(masterId, filter, childKeyNames.get(agent), null));
            }
        }

        if (filter != null) {
            crud.ignore(filter.ignore(true));
        }

        return crud;
    }

    @Override
    @Transactional
    public Integer deleteMaster(long masterId, String field) {
        if (field != null && field.length() > 0) {
            throw new RuntimeException("field name has been provided by individual slave agent.");
        }

        init();

        int affected = 0;

        boolean allowDeleted = true;

        /// foreach slaves
        if (slaveModelingAgents != null) {
            for (CRUDServiceSlaveAgent agent : slaveModelingAgents) {
                if (allowDeleted) {
                    if (agent.deleteMaster(masterId, slaveKeyNames.get(agent)) > 0) {
                        allowDeleted = false;
                    }
                }
            }
        }

        if (childAgents != null) {
            /// allow to delete if no slave items

            if (allowDeleted) {
                /// delete child if it has
                for (CRUDServiceChildAgent agent : childAgents) {
                    affected += agent.deleteMaster(masterId, childKeyNames.get(agent));
                }

            }
        }

        /// but: delete master if it allow
        if (allowDeleted) {
            /// delete the master
            affected += getMasterMapper().deleteById(masterId);

        } else {
            /// just throw exception if it is not allow to delete
            throw new BusinessException(BusinessCode.CRUD_DELETE_ASSOCIATED_MASTER);
        }


        return affected;
    }

    @Override
    public CRUDObject<M> retrieveMasterModel(Long id) {
        return retrieveMaster(id, null, null, null);
    }

    @Override
    public CRUDObject<M> retrieveMasterModel(Long id, CRUDFilterResult<T> filter) {
        return retrieveMaster(id, filter, null, null);
    }

    @Deprecated
    @Override
    @Transactional
    public Integer bulkDelete(Ids ids) {
        return getMasterMapper().deleteBatchIds(ids.getIds());
    }
}
