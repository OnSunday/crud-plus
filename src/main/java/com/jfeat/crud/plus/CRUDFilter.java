package com.jfeat.crud.plus;


/**
 * Created by vincenthuang on 27/08/2017.
 */
public interface CRUDFilter<T> {

    /**
     *
     * @param t
     * @param insertOrUpdate
     */
    void filter(T t, boolean insertOrUpdate);

    /**
     *
     * @param retrieveOrUpdate
     * @return
     */
    String[] ignore(boolean retrieveOrUpdate);

}


