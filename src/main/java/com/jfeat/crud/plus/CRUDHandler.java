package com.jfeat.crud.plus;


/**
 * Created by vincenthuang on 27/08/2017.
 */
public interface CRUDHandler<T, M extends T>{
    final String CREATE = "Create";
    final String UPDATE = "Update";
    final String RETRIEVE = "Retrieve";
    final String DELETE = "Delete";

    /**
     * Handler master by self
     */
    Integer onHandleMaster(M m, String field, String action);
}


